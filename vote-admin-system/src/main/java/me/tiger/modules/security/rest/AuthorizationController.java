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
import com.alibaba.fastjson.JSONObject;
import com.wf.captcha.base.Captcha;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.result.WxMpOAuth2AccessToken;
import me.chanjar.weixin.mp.bean.result.WxMpUser;
import me.tiger.annotation.rest.AnonymousDeleteMapping;
import me.tiger.annotation.rest.AnonymousGetMapping;
import me.tiger.annotation.rest.AnonymousPostMapping;
import me.tiger.config.RsaProperties;
import me.tiger.config.WXAccountConfig;
import me.tiger.exception.BadRequestException;
import me.tiger.modules.security.config.bean.LoginCodeEnum;
import me.tiger.modules.security.config.bean.LoginProperties;
import me.tiger.modules.security.config.bean.SecurityProperties;
import me.tiger.modules.security.security.TokenProvider;
import me.tiger.modules.security.security.WxAuthenticationToken;
import me.tiger.modules.security.service.OnlineUserService;
import me.tiger.modules.security.service.dto.AuthUserDto;
import me.tiger.modules.security.service.dto.JwtUserDto;
import me.tiger.modules.system.service.UserService;
import me.tiger.util.SignUtil;
import me.tiger.utils.RedisUtils;
import me.tiger.utils.RsaUtils;
import me.tiger.utils.SecurityUtils;
import me.tiger.utils.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static me.tiger.modules.works.constant.ResponseConstant.*;

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

    public static final String ACCESS_TOKEN = "access_token";
    public static final String TICKET = "ticket";
    private final SecurityProperties properties;
    private final RedisUtils redisUtils;
    private final OnlineUserService onlineUserService;
    private final TokenProvider tokenProvider;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final WXAccountConfig wxConfig;
    private final WxMpService wxMpService;
    private final UserService userService;

    @Value("${server.domain:5847f71879aa.ngrok.io}")
    private String serverDomain;

    @Resource
    private LoginProperties loginProperties;

    @ApiOperation("微信验证服务器所需接口")
    @AnonymousGetMapping(value = "/wxCheckServer")
    public void wxCallback2CheckServer(HttpServletRequest request, HttpServletResponse response,
                                       @RequestParam(value = "signature", required = true) String signature,
                                       @RequestParam(value = "timestamp", required = true) String timestamp,
                                       @RequestParam(value = "nonce", required = true) String nonce,
                                       @RequestParam(value = "echostr", required = true) String echostr) {

        try {
            if (SignUtil.checkSignature(signature, timestamp, nonce)) {
                PrintWriter out = response.getWriter();
                out.print(echostr);
                out.close();
            } else {
                log.info("这里存在非法请求！");
            }
        } catch (Exception e) {
            log.error("给微信回调的的接口发生错误", e);
        }

    }

    /**
     * 初次授权获取用户信息
     *
     * @param code
     * @param returnUrl
     * @return
     */
    @AnonymousGetMapping("/wx/userInfo")
    public ResponseEntity<JSONObject> userInfo(@RequestParam("code") String code, HttpServletRequest request) {
        WxMpOAuth2AccessToken wxMpOAuth2AccessToken;
        WxMpUser wxMpUser;
        try {
            // 使用code换取access_token信息
            wxMpOAuth2AccessToken = wxMpService.oauth2getAccessToken(code);
            wxMpUser = wxMpService.oauth2getUserInfo(wxMpOAuth2AccessToken, null);

            String openId = wxMpOAuth2AccessToken.getOpenId();
            log.info("【微信网页授权】获取openId，openId = {}", openId);

            // 保存在线信息
            // 返回 token 与 用户信息
            WxAuthenticationToken wxAuthenticationToken = new WxAuthenticationToken(code, openId);
            SecurityContextHolder.getContext().setAuthentication(wxAuthenticationToken);
            String token = onlineUserService.saveWxUserInfo(wxAuthenticationToken, wxMpUser, request);

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("user", wxMpUser);
            jsonObject.put("token", String.format("%s%s", properties.getTokenStartWith(), token));

            return new ResponseEntity<>(buildResult(SUCCESS, "微信用户信息获取成功", jsonObject), HttpStatus.CREATED);

        } catch (WxErrorException e) {
            log.error("【微信网页授权】异常，{}", e);
            return new ResponseEntity<>(buildResult(FAIL, e.getMessage(), null), HttpStatus.CREATED);
        }
    }

    @ApiOperation("登录授权")
    @AnonymousPostMapping(value = "/login")
    public ResponseEntity<Object> login(@Validated @RequestBody AuthUserDto authUser, HttpServletRequest request) throws Exception {
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
