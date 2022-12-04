package com.rulebased848.gamsibot.web.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rulebased848.gamsibot.domain.RequestPayload;
import java.io.BufferedReader;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolation;
import javax.validation.Path;
import javax.validation.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class SimpleRequestValidationInterceptor implements HandlerInterceptor {
    private final ObjectMapper objectMapper;

    private final Validator validator;

    @Autowired
    public SimpleRequestValidationInterceptor(ObjectMapper objectMapper, Validator validator) {
        this.objectMapper = objectMapper;
        this.validator = validator;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!request.getMethod().equals("POST")) return true;
        BufferedReader reader = request.getReader();
        String body = reader.readLine();
        reader.close();
        RequestPayload payload = objectMapper.readValue(body, RequestPayload.class);
        Set<ConstraintViolation<RequestPayload>> violations = validator.validate(payload);
        if (violations.isEmpty()) {
            request.setAttribute("payload", payload);
            return true;
        }
        response.setStatus(400);
        Map<String,String> errors = new HashMap<>();
        for (ConstraintViolation<RequestPayload> violation : violations) {
            errors.put(violation.getPropertyPath().iterator().next().getName(), violation.getMessage());
        }
        OutputStream out = response.getOutputStream();
        objectMapper.writeValue(out, errors);
        out.close();
        return false;
    }
}