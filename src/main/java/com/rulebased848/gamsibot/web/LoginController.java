package com.rulebased848.gamsibot.web;

import com.rulebased848.gamsibot.domain.AccountCredentials;
import com.rulebased848.gamsibot.service.JwtService;
import java.util.HashMap;
import java.util.Map;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import static org.springframework.http.HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
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
    public ResponseEntity<?> getToken(@Valid @RequestBody AccountCredentials credentials) {
        Authentication token = new UsernamePasswordAuthenticationToken(
            credentials.getUsername(),
            credentials.getPassword()
        );
        String username = authenticationManager.authenticate(token).getName();
        return ResponseEntity.ok()
            .header(AUTHORIZATION, "Bearer " + jwtService.getToken(username))
            .header(ACCESS_CONTROL_EXPOSE_HEADERS, "Authorization")
            .build();
    }

    @ResponseStatus(BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Map<String,String> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String,String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            var fieldError = (FieldError)error;
            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        });
        return errors;
    }
}