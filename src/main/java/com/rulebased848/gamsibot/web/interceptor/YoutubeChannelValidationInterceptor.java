package com.rulebased848.gamsibot.web.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rulebased848.gamsibot.domain.RequestPayload;
import com.rulebased848.gamsibot.web.YoutubeChannelInfoFetcher;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class YoutubeChannelValidationInterceptor implements HandlerInterceptor {
    private final YoutubeChannelInfoFetcher fetcher;

    private final ObjectMapper objectMapper;

    @Autowired
    public YoutubeChannelValidationInterceptor(YoutubeChannelInfoFetcher fetcher, ObjectMapper objectMapper) {
        this.fetcher = fetcher;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!request.getMethod().equals("POST")) return true;
        var payload = (RequestPayload)request.getAttribute("payload");
        String handle = payload.getHandle();
        Map<String,Object> info = fetcher.fetchChannelInfo(handle);
        if ((boolean)info.get("isValid")) {
            request.setAttribute("channel.info", info);
            return true;
        }
        response.setStatus(400);
        response.setContentType("application/json");
        Map<String,Object> body = new HashMap<>(1);
        body.put("message", "The handle is not valid.");
        OutputStream out = response.getOutputStream();
        objectMapper.writeValue(out, body);
        out.close();
        return false;
    }
}