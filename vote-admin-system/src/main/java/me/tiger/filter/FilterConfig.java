package me.tiger.filter;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Component
public class FilterConfig {

    @Bean
    public FilterRegistrationBean filterRegistration() {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        // 注册vue过滤器
        registration.setFilter(new AdminWebFilter());
        registration.addUrlPatterns("/adminWeb/**");
        registration.setName("AdminWebFilter");
        return registration;
    }
}
