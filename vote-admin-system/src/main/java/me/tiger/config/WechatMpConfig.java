package me.tiger.config;

import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.api.impl.WxMpServiceImpl;
import me.chanjar.weixin.mp.config.WxMpConfigStorage;
import me.chanjar.weixin.mp.config.impl.WxMpDefaultConfigImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class WechatMpConfig {

    private final WXAccountConfig wxAccountConfig;


    public WechatMpConfig(WXAccountConfig wxAccountConfig) {
        this.wxAccountConfig = wxAccountConfig;
    }

    @Bean
    public WxMpService wxMpService(){
        WxMpService wxMpService = new WxMpServiceImpl();
        wxMpService.setWxMpConfigStorage(wxAccountConfigStorage());
        return wxMpService;
    }

    public WxMpConfigStorage wxAccountConfigStorage() {

        WxMpDefaultConfigImpl wxMpDefaultConfig =   new WxMpDefaultConfigImpl();
        wxMpDefaultConfig.setAppId(wxAccountConfig.getAppID());
        wxMpDefaultConfig.setSecret(wxAccountConfig.getAppsecret());
        return wxMpDefaultConfig;
    }

}
