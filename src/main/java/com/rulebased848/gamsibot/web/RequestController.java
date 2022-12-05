package com.rulebased848.gamsibot.web;

import com.rulebased848.gamsibot.core.RequestManager;
import com.rulebased848.gamsibot.domain.Request;
import com.rulebased848.gamsibot.domain.RequestPayload;
import com.rulebased848.gamsibot.domain.User;
import com.rulebased848.gamsibot.domain.UserRepository;
import com.rulebased848.gamsibot.service.JwtService;
import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RequestController {
    private final JwtService jwtService;

    private final UserRepository repository;

    private final RequestManager requestManager;

    @Autowired
    public RequestController(
        final JwtService jwtService,
        final UserRepository repository,
        final RequestManager requestManager
    ) {
        this.jwtService = jwtService;
        this.repository = repository;
        this.requestManager = requestManager;
    }

    @GetMapping("/requests")
    public ResponseEntity<?> getPersonalRequests() {
        var username = (String)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Optional<User> maybeUser = repository.findByUsername(username);
        if (maybeUser.isEmpty()) {
            var body = new HashMap<String,Object>(1);
            body.put("message", "The user does not exist.");
            return ResponseEntity.badRequest()
                .contentType(APPLICATION_JSON)
                .body(body);
        }
        var user = maybeUser.get();
        return ResponseEntity.ok()
            .contentType(APPLICATION_JSON)
            .body(user.getRequests());
    }

    @PostMapping("/requests")
    public ResponseEntity<?> acceptRequest(
        @RequestAttribute(name = "user", required = false) Optional<User> maybeUser,
        @RequestAttribute("payload") RequestPayload payload
    ) throws IOException {
        var request = new Request();
        request.setHandle(payload.getHandle());
        request.setTargetSubscriberCount(payload.getTargetSubscriberCount());
        request.setEmailAddress(payload.getEmailAddress());
        request.setCreatedAt(Instant.now());
        if (maybeUser.isPresent()) {
            request.setRequester(maybeUser.get());
        }
        return ResponseEntity.ok()
            .contentType(APPLICATION_JSON)
            .body(requestManager.createRequest(request));
    }

    @DeleteMapping("/requests/{id}")
    public ResponseEntity<?> deleteRequest(
        @RequestHeader(value = "JWT", defaultValue = "") String token,
        @PathVariable long id
    ) {
        String username = token.isEmpty() ? null : jwtService.getAuthUser(token.replaceFirst("Bearer ", ""));
        return ResponseEntity.ok()
            .contentType(APPLICATION_JSON)
            .body(requestManager.deleteRequest(id, username));
    }
}