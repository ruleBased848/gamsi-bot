package com.rulebased848.gamsibot.web;

import com.rulebased848.gamsibot.domain.AccountCredentials;
import com.rulebased848.gamsibot.service.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LoginController {
    private final AuthenticationManager authenticationManager;

    private final JwtService jwtService;

    @Autowired
    public LoginController(
        final AuthenticationManager authenticationManager,
        final JwtService jwtService
    ) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> getToken(@RequestBody AccountCredentials credentials) {
        var token = new UsernamePasswordAuthenticationToken(
            credentials.getUsername(),
            credentials.getPassword()
        );
        var username = authenticationManager.authenticate(token).getName();
        return ResponseEntity.ok()
            .header(AUTHORIZATION, "Bearer " + jwtService.getToken(username))
            .build();
    }
}