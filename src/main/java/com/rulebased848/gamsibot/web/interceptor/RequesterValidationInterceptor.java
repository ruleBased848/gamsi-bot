package com.rulebased848.gamsibot.web.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rulebased848.gamsibot.domain.User;
import com.rulebased848.gamsibot.domain.UserRepository;
import com.rulebased848.gamsibot.service.JwtService;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class RequesterValidationInterceptor implements HandlerInterceptor {
    private final JwtService jwtService;

    private final UserRepository repository;

    private final ObjectMapper objectMapper;

    @Autowired
    public RequesterValidationInterceptor(JwtService jwtService, UserRepository repository, ObjectMapper objectMapper) {
        this.jwtService = jwtService;
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!request.getMethod().equals("POST")) return true;
        String token = request.getHeader("JWT");
        if (token == null) return true;
        String username = jwtService.getAuthUser(token.replaceFirst("Bearer ", ""));
        Optional<User> maybeUser = repository.findByUsername(username);
        if (maybeUser.isPresent()) {
            request.setAttribute("user", maybeUser.get());
            return true;
        }
        response.setStatus(400);
        response.setContentType("application/json");
        Map<String,Object> body = new HashMap<>(1);
        body.put("message", "The JWT is not valid.");
        OutputStream out = response.getOutputStream();
        objectMapper.writeValue(out, body);
        out.close();
        return false;
    }
}