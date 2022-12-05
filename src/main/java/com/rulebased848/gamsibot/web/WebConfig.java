package com.rulebased848.gamsibot.web;

import com.rulebased848.gamsibot.web.interceptor.SimpleRequestValidationInterceptor;
import com.rulebased848.gamsibot.web.interceptor.YoutubeChannelValidationInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    private final SimpleRequestValidationInterceptor simpleRequestValidationInterceptor;

    private final YoutubeChannelValidationInterceptor youtubeChannelValidationInterceptor;

    @Autowired
    public WebConfig(
        SimpleRequestValidationInterceptor simpleRequestValidationInterceptor,
        YoutubeChannelValidationInterceptor youtubeChannelValidationInterceptor
    ) {
        this.simpleRequestValidationInterceptor = simpleRequestValidationInterceptor;
        this.youtubeChannelValidationInterceptor = youtubeChannelValidationInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry
            .addInterceptor(simpleRequestValidationInterceptor)
            .addPathPatterns("/requests");
        registry
            .addInterceptor(youtubeChannelValidationInterceptor)
            .addPathPatterns("/requests");
    }
}