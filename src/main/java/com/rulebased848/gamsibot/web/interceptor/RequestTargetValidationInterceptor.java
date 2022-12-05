package com.rulebased848.gamsibot.web.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rulebased848.gamsibot.domain.RequestPayload;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class RequestTargetValidationInterceptor implements HandlerInterceptor {
    private final ObjectMapper objectMapper;

    @Autowired
    public RequestTargetValidationInterceptor(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!request.getMethod().equals("POST")) return true;
        var info = (Map<?,?>)request.getAttribute("channel.info");
        var payload = (RequestPayload)request.getAttribute("payload");
        if (Long.compareUnsigned((long)info.get("subscriberCount"), payload.getTargetSubscriberCount()) < 0) return true;
        response.setStatus(400);
        response.setContentType("application/json");
        Map<String,Object> body = new HashMap<>(1);
        body.put("message", "The target subscriber count is already achieved.");
        OutputStream out = response.getOutputStream();
        objectMapper.writeValue(out, body);
        out.close();
        return false;
    }
}