package com.rulebased848.gamsibot.web;

import com.rulebased848.gamsibot.web.interceptor.SimpleRequestValidationInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    private final SimpleRequestValidationInterceptor simpleRequestValidationInterceptor;

    @Autowired
    public WebConfig(SimpleRequestValidationInterceptor simpleRequestValidationInterceptor) {
        this.simpleRequestValidationInterceptor = simpleRequestValidationInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry
            .addInterceptor(simpleRequestValidationInterceptor)
            .addPathPatterns("/requests");
    }
}