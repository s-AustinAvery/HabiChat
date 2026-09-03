package com.habichat.config;

import com.habichat.security.TokenAuthFilter;
import jakarta.servlet.DispatcherType;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {

    @Bean
    public FilterRegistrationBean<TokenAuthFilter> tokenAuthFilterRegistration(TokenAuthFilter filter) {
        FilterRegistrationBean<TokenAuthFilter> registration = new FilterRegistrationBean<>(filter);
        registration.addUrlPatterns("/api/*");
        registration.setDispatcherTypes(DispatcherType.REQUEST);
        return registration;
    }
}
