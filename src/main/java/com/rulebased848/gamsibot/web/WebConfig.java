package com.rulebased848.gamsibot.web;

import com.rulebased848.gamsibot.web.interceptor.RequesterValidationInterceptor;
import com.rulebased848.gamsibot.web.interceptor.RequestTargetValidationInterceptor;
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

    private final RequestTargetValidationInterceptor requestTargetValidationInterceptor;

    private final RequesterValidationInterceptor requesterValidationInterceptor;

    @Autowired
    public WebConfig(
        SimpleRequestValidationInterceptor simpleRequestValidationInterceptor,
        YoutubeChannelValidationInterceptor youtubeChannelValidationInterceptor,
        RequestTargetValidationInterceptor requestTargetValidationInterceptor,
        RequesterValidationInterceptor requesterValidationInterceptor
    ) {
        this.simpleRequestValidationInterceptor = simpleRequestValidationInterceptor;
        this.youtubeChannelValidationInterceptor = youtubeChannelValidationInterceptor;
        this.requestTargetValidationInterceptor = requestTargetValidationInterceptor;
        this.requesterValidationInterceptor = requesterValidationInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry
            .addInterceptor(simpleRequestValidationInterceptor)
            .addPathPatterns("/requests");
        registry
            .addInterceptor(youtubeChannelValidationInterceptor)
            .addPathPatterns("/requests");
        registry
            .addInterceptor(requestTargetValidationInterceptor)
            .addPathPatterns("/requests");
        registry
            .addInterceptor(requesterValidationInterceptor)
            .addPathPatterns("/requests");
    }
}