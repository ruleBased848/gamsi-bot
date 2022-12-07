package com.rulebased848.gamsibot.web;

import com.rulebased848.gamsibot.domain.AccountCredentials;
import com.rulebased848.gamsibot.domain.User;
import com.rulebased848.gamsibot.domain.UserRepository;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SignupController {
    private final PasswordEncoder passwordEncoder;

    private final UserRepository repository;

    @Autowired
    public SignupController(PasswordEncoder passwordEncoder, UserRepository repository) {
        this.passwordEncoder = passwordEncoder;
        this.repository = repository;
    }

    @PostMapping("/signup")
    public ResponseEntity<?> handleSignup(@Valid @RequestBody AccountCredentials credentials) {
        String username = credentials.getUsername();
        String password = credentials.getPassword();
        synchronized (this) {
            Optional<User> maybeUser = repository.findByUsername(username);
            if (maybeUser.isPresent()) {
                Map<String,Object> body = new HashMap<>(1);
                body.put("message", "The username is already used.");
                return ResponseEntity.badRequest()
                    .contentType(APPLICATION_JSON)
                    .body(body);
            }
            var user = new User();
            user.setUsername(username);
            user.setPassword(passwordEncoder.encode(password));
            user.setRole("USER");
            repository.save(user);
        }
        return ResponseEntity.ok(username);
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