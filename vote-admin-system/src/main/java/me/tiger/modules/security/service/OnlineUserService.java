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
package me.tiger.modules.security.service;

import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.mp.bean.result.WxMpUser;
import me.tiger.modules.security.security.TokenProvider;
import me.tiger.modules.security.security.WxAuthenticationToken;
import me.tiger.modules.works.domain.WxWorksAuthor;
import me.tiger.modules.works.repository.WxWorksAuthorRepository;
import me.tiger.utils.*;
import me.tiger.modules.security.config.bean.SecurityProperties;
import me.tiger.modules.security.service.dto.JwtUserDto;
import me.tiger.modules.security.service.dto.OnlineUserDto;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

/**
 * @author Zheng Jie
 * @date 2019年10月26日21:56:27
 */
@Service
@Slf4j
public class OnlineUserService {

    private final SecurityProperties properties;
    private final RedisUtils redisUtils;
    private final TokenProvider tokenProvider;
    private final WxWorksAuthorRepository wxWorksAuthorRepository;

    public OnlineUserService(SecurityProperties properties, RedisUtils redisUtils, TokenProvider tokenProvider, WxWorksAuthorRepository wxWorksAuthorRepository) {
        this.properties = properties;
        this.redisUtils = redisUtils;
        this.tokenProvider = tokenProvider;
        this.wxWorksAuthorRepository = wxWorksAuthorRepository;
    }

    /**
     * 保存在线用户信息
     * @param jwtUserDto /
     * @param token /
     * @param request /
     */
    public void save(JwtUserDto jwtUserDto, String token, HttpServletRequest request){
        String dept = jwtUserDto.getUser().getDept().getName();
        String ip = StringUtils.getIp(request);
        String browser = StringUtils.getBrowser(request);
        String address = StringUtils.getCityInfo(ip);
        OnlineUserDto onlineUserDto = null;
        try {
            onlineUserDto = new OnlineUserDto(jwtUserDto.getUsername(), jwtUserDto.getUser().getNickName(), dept, browser , ip, address, EncryptUtils.desEncrypt(token), new Date());
        } catch (Exception e) {
            log.error(e.getMessage(),e);
        }
        redisUtils.set(getOnlienTokenKey(token), onlineUserDto, properties.getTokenValidityInSeconds()/1000);
    }

    /**
     * 查询全部数据
     * @param filter /
     * @param pageable /
     * @return /
     */
    public Map<String,Object> getAll(String filter, Pageable pageable){
        List<OnlineUserDto> onlineUserDtos = getAll(filter);
        return PageUtil.toPage(
                PageUtil.toPage(pageable.getPageNumber(),pageable.getPageSize(), onlineUserDtos),
                onlineUserDtos.size()
        );
    }

    /**
     * 查询全部数据，不分页
     * @param filter /
     * @return /
     */
    public List<OnlineUserDto> getAll(String filter){
        List<String> keys = redisUtils.scan(getOnlienTokenKey("*"));
        Collections.reverse(keys);
        List<OnlineUserDto> onlineUserDtos = new ArrayList<>();
        for (String key : keys) {
            OnlineUserDto onlineUserDto = (OnlineUserDto) redisUtils.get(key);
            if(StringUtils.isNotBlank(filter)){
                if(onlineUserDto.toString().contains(filter)){
                    onlineUserDtos.add(onlineUserDto);
                }
            } else {
                onlineUserDtos.add(onlineUserDto);
            }
        }
        onlineUserDtos.sort((o1, o2) -> o2.getLoginTime().compareTo(o1.getLoginTime()));
        return onlineUserDtos;
    }

    /**
     * 踢出用户
     * @param key /
     */
    public void kickOut(String key){
        key = getOnlienTokenKey(key);
        redisUtils.del(key);
    }

    /**
     * 退出登录
     * @param token /
     */
    public void logout(String token) {
        String key = getOnlienTokenKey(token);
        redisUtils.del(key);
    }

    /**
     * 导出
     * @param all /
     * @param response /
     * @throws IOException /
     */
    public void download(List<OnlineUserDto> all, HttpServletResponse response) throws IOException {
        List<Map<String, Object>> list = new ArrayList<>();
        for (OnlineUserDto user : all) {
            Map<String,Object> map = new LinkedHashMap<>();
            map.put("用户名", user.getUserName());
            map.put("部门", user.getDept());
            map.put("登录IP", user.getIp());
            map.put("登录地点", user.getAddress());
            map.put("浏览器", user.getBrowser());
            map.put("登录日期", user.getLoginTime());
            list.add(map);
        }
        FileUtil.downloadExcel(list, response);
    }

    /**
     * 查询用户
     * @param key /
     * @return /
     */
    public OnlineUserDto getOne(String key) {
        return (OnlineUserDto)redisUtils.get(key);
    }

    /**
     * 检测用户是否在之前已经登录，已经登录踢下线
     * @param userName 用户名
     */
    public void checkLoginOnUser(String userName, String igoreToken){
        List<OnlineUserDto> onlineUserDtos = getAll(userName);
        if(onlineUserDtos ==null || onlineUserDtos.isEmpty()){
            return;
        }
        for(OnlineUserDto onlineUserDto : onlineUserDtos){
            if(onlineUserDto.getUserName().equals(userName)){
                try {
                    String token =EncryptUtils.desDecrypt(onlineUserDto.getKey());
                    if(StringUtils.isNotBlank(igoreToken)&&!igoreToken.equals(token)){
                        this.kickOut(token);
                    }else if(StringUtils.isBlank(igoreToken)){
                        this.kickOut(token);
                    }
                } catch (Exception e) {
                    log.error("checkUser is error",e);
                }
            }
        }
    }

    /**
     * 根据用户名强退用户
     * @param username /
     */
    @Async
    public void kickOutForUsername(String username) throws Exception {
        List<OnlineUserDto> onlineUsers = getAll(username);
        for (OnlineUserDto onlineUser : onlineUsers) {
            if (onlineUser.getUserName().equals(username)) {
                String token =EncryptUtils.desDecrypt(onlineUser.getKey());
                kickOut(token);
            }
        }
    }

    /**
     * 保存微信用户的登录信息
     * @param authentication
     * @param wxMpUser
     * @return*/
    public String saveWxUserInfo(Authentication authentication, WxMpUser wxMpUser, HttpServletRequest request) {
        String openId = (String) authentication.getPrincipal();
        String token = tokenProvider.createToken(authentication);

        String ip = StringUtils.getIp(request);
        String browser = StringUtils.getBrowser(request);
        String address = StringUtils.getCityInfo(ip);
        OnlineUserDto onlineUserDto = null;

        try {
            WxWorksAuthor wxWorksAuthor = WxWorksAuthor.builder().openId(wxMpUser.getOpenId()).headImgUrl(wxMpUser.getHeadImgUrl()).nickName(wxMpUser.getNickname())
                                            .sex(wxMpUser.getSexDesc()).city(wxMpUser.getCity()).province(wxMpUser.getProvince()).country(wxMpUser.getCountry()).build();
            wxWorksAuthorRepository.save(wxWorksAuthor);

            onlineUserDto = new OnlineUserDto(wxMpUser.getOpenId(), wxMpUser.getNickname(), "wxVisitor", browser , ip, address, EncryptUtils.desEncrypt(token), new Date());
        } catch (Exception e) {
            log.error(e.getMessage(),e);
        }
        redisUtils.set(getOnlienTokenKey(token), onlineUserDto, properties.getTokenValidityInSeconds()/1000);
        return token;
    }

    private String getOnlienTokenKey(String token) {
        return properties.getOnlineKey() + token;
    }
}
