/*
 *  Copyright 2019-2020 Zheng Jie
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package me.tiger.modules.security.rest;

import cn.hutool.core.util.IdUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.wf.captcha.base.Captcha;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.tiger.annotation.rest.AnonymousDeleteMapping;
import me.tiger.annotation.rest.AnonymousGetMapping;
import me.tiger.annotation.rest.AnonymousPostMapping;
import me.tiger.config.RsaProperties;
import me.tiger.config.WXConfig;
import me.tiger.exception.BadRequestException;
import me.tiger.modules.security.config.bean.LoginCodeEnum;
import me.tiger.modules.security.config.bean.LoginProperties;
import me.tiger.modules.security.config.bean.SecurityProperties;
import me.tiger.modules.security.security.TokenProvider;
import me.tiger.modules.security.service.OnlineUserService;
import me.tiger.modules.security.service.dto.AuthUserDto;
import me.tiger.modules.security.service.dto.JwtUserDto;
import me.tiger.util.HttpClientUtil;
import me.tiger.util.HttpUtil;
import me.tiger.util.JSONUtil;
import me.tiger.util.UUIDUtil;
import me.tiger.utils.RedisUtils;
import me.tiger.utils.RsaUtils;
import me.tiger.utils.SecurityUtils;
import me.tiger.utils.StringUtils;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author Zheng Jie
 * @date 2018-11-23
 * 授权、根据token获取用户详细信息
 */
@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Api(tags = "系统：系统授权接口")
public class AuthorizationController {

    private final SecurityProperties properties;
    private final RedisUtils redisUtils;
    private final OnlineUserService onlineUserService;
    private final TokenProvider tokenProvider;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final WXConfig wxConfig;

    @Value("${server.domain:5847f71879aa.ngrok.io}")
    private String serverDomain;

    @Resource
    private LoginProperties loginProperties;


    @ApiOperation("微信登录接口")
    @RequestMapping("/wxLogin")
    public void wxLogin(HttpServletResponse response) throws IOException, IOException {

        //请求获取code的回调地址
        String callBack = String.format("http://%s/%s", serverDomain, "auth/wxCallBack");
        //请求地址
        String url = String.format(WXConfig.WX_OATH_LOGIN_URL, wxConfig.getAppID(), URLEncoder.encode(callBack));
        //重定向
        response.sendRedirect(url);
    }

    @ApiOperation("获取微信JS-SDK签名")
    @RequestMapping("/wxCallback")
    public void wxCallBack(HttpServletRequest request, HttpServletResponse response) throws IOException {

        String code = request.getParameter("code");

        //获取access_token
        String url = String.format(WXConfig.WX_ACCESS_TOKEN_URL, wxConfig.getAppID(), wxConfig.getAppsecret(), code);
        String result = HttpClientUtil.doGet(url);

        System.out.println("请求获取access_token:" + result);
        //返回结果的json对象
        JSONObject resultObject = JSON.parseObject(result);

        //请求获取userInfo
        String infoUrl = String.format(WXConfig.WX_USER_INFO_URL, resultObject.getString("access_token"), resultObject.getString("openid"));

        String resultInfo = HttpClientUtil.doGet(infoUrl);
        //此时已获取到userInfo，再根据业务进行处理
        System.out.println("请求获取userInfo:" + resultInfo);

    }

    @ApiOperation("获取微信JS-SDK签名")
    @AnonymousGetMapping(value = "/getWXJSSDKSignature")
    public ResponseEntity<JSONObject> getJSSDKSignature(String url) {
        String tokenJson = HttpUtil.get("https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=" + wxConfig.getAppID() + "&secret=" + wxConfig.getAppsecret(), null);
        String access_token = JSONUtil.getString(tokenJson, "access_token");  // access_token
        String ticketJson = HttpUtil.get("https://api.weixin.qq.com/cgi-bin/ticket/getticket?access_token=" + access_token + "&type=jsapi", null);
        String ticket = JSONUtil.getString(ticketJson, "ticket");  // ticket
        String noncestr = UUIDUtil.randomUUID8();  // 随机字符串
        long timestamp = new Date().getTime();  // 时间戳
        String str = String.format("jsapi_ticket=%s&noncestr=%s&timestamp=%d&url=%s", ticket, noncestr, timestamp, url);

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("appId", wxConfig.getAppID());
        jsonObject.put("timestamp", timestamp);
        jsonObject.put("nonceStr", noncestr);
        jsonObject.put("signature", DigestUtils.sha1Hex(str));

        return new ResponseEntity<>(jsonObject, HttpStatus.OK);
    }

    @ApiOperation("登录授权")
    @AnonymousPostMapping(value = "/login")
    public ResponseEntity<Object> login(@Validated @RequestBody AuthUserDto authUser, HttpServletRequest request) throws Exception {
        log.info("测试输出日志................");
        // 密码解密
        String password = RsaUtils.decryptByPrivateKey(RsaProperties.privateKey, authUser.getPassword());
        // 查询验证码
        String code = (String) redisUtils.get(authUser.getUuid());
        // 清除验证码
        redisUtils.del(authUser.getUuid());
        if (StringUtils.isBlank(code)) {
            throw new BadRequestException("验证码不存在或已过期");
        }
        if (StringUtils.isBlank(authUser.getCode()) || !authUser.getCode().equalsIgnoreCase(code)) {
            throw new BadRequestException("验证码错误");
        }
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(authUser.getUsername(), password);
        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        // 生成令牌与第三方系统获取令牌方式
        // UserDetails userDetails = userDetailsService.loadUserByUsername(userInfo.getUsername());
        // Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        // SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = tokenProvider.createToken(authentication);
        final JwtUserDto jwtUserDto = (JwtUserDto) authentication.getPrincipal();
        // 保存在线信息
        onlineUserService.save(jwtUserDto, token, request);
        // 返回 token 与 用户信息
        Map<String, Object> authInfo = new HashMap<String, Object>(2) {{
            put("token", properties.getTokenStartWith() + token);
            put("user", jwtUserDto);
        }};
        if (loginProperties.isSingleLogin()) {
            //踢掉之前已经登录的token
            onlineUserService.checkLoginOnUser(authUser.getUsername(), token);
        }
        return ResponseEntity.ok(authInfo);
    }

    @ApiOperation("获取用户信息")
    @GetMapping(value = "/info")
    public ResponseEntity<Object> getUserInfo() {
        return ResponseEntity.ok(SecurityUtils.getCurrentUser());
    }

    @ApiOperation("获取验证码")
    @AnonymousGetMapping(value = "/code")
    public ResponseEntity<Object> getCode() {
        // 获取运算的结果
        Captcha captcha = loginProperties.getCaptcha();
        String uuid = properties.getCodeKey() + IdUtil.simpleUUID();
        //当验证码类型为 arithmetic时且长度 >= 2 时，captcha.text()的结果有几率为浮点型
        String captchaValue = captcha.text();
        if (captcha.getCharType() - 1 == LoginCodeEnum.arithmetic.ordinal() && captchaValue.contains(".")) {
            captchaValue = captchaValue.split("\\.")[0];
        }
        // 保存
        redisUtils.set(uuid, captchaValue, loginProperties.getLoginCode().getExpiration(), TimeUnit.MINUTES);
        // 验证码信息
        Map<String, Object> imgResult = new HashMap<String, Object>(2) {{
            put("img", captcha.toBase64());
            put("uuid", uuid);
        }};
        return ResponseEntity.ok(imgResult);
    }

    @ApiOperation("退出登录")
    @AnonymousDeleteMapping(value = "/logout")
    public ResponseEntity<Object> logout(HttpServletRequest request) {
        onlineUserService.logout(tokenProvider.getToken(request));
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
