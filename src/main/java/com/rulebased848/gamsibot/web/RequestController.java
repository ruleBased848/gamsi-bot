package com.rulebased848.gamsibot.web;

import com.rulebased848.gamsibot.core.GamsiBot;
import com.rulebased848.gamsibot.domain.Request;
import com.rulebased848.gamsibot.domain.RequestPayload;
import com.rulebased848.gamsibot.domain.User;
import com.rulebased848.gamsibot.domain.UserRepository;
import com.rulebased848.gamsibot.service.JwtService;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RequestController {
    private final YoutubeChannelInfoFetcher fetcher;

    private final JwtService jwtService;

    private final UserRepository repository;

    private final GamsiBot bot;

    @Autowired
    public RequestController(
        final YoutubeChannelInfoFetcher fetcher,
        final JwtService jwtService,
        final UserRepository repository,
        final GamsiBot bot
    ) {
        this.fetcher = fetcher;
        this.jwtService = jwtService;
        this.repository = repository;
        this.bot = bot;
    }

    @PostMapping("/requests")
    public ResponseEntity<?> acceptRequest(
        @RequestHeader(value = "JWT", defaultValue = "") String token,
        @RequestBody @Valid RequestPayload payload
    ) throws IOException {
        String channelId = payload.getChannelId();
        Map<String,Object> info = fetcher.fetchChannelInfo(channelId);
        if (!(boolean)info.get("isValid")) {
            var body = new HashMap<String,Object>(1);
            body.put("message", "The channel ID is not valid.");
            return ResponseEntity.badRequest()
                .contentType(APPLICATION_JSON)
                .body(body);
        }
        long targetSubscriberCount = payload.getTargetSubscriberCount();
        if (Long.compareUnsigned((long)info.get("subscriberCount"), targetSubscriberCount) >= 0) {
            var body = new HashMap<String,Object>(1);
            body.put("message", "The target subscriber count is already achieved.");
            return ResponseEntity.badRequest()
                .contentType(APPLICATION_JSON)
                .body(body);
        }
        User user = null;
        if (!token.isEmpty()) {
            String username = jwtService.getAuthUser(token.replaceFirst("Bearer ", ""));
            Optional<User> maybeUser = repository.findByUsername(username);
            if (maybeUser.isEmpty()) {
                var body = new HashMap<String,Object>(1);
                body.put("message", "The JWT is not valid.");
                return ResponseEntity.badRequest()
                    .contentType(APPLICATION_JSON)
                    .body(body);
            }
            user = maybeUser.get();
        }
        var request = new Request();
        request.setChannelId(channelId);
        request.setTargetSubscriberCount(targetSubscriberCount);
        request.setEmailAddress(payload.getEmailAddress());
        request.setRequester(user);
        return ResponseEntity.ok()
            .contentType(APPLICATION_JSON)
            .body(bot.newRequest(request));
    }

    @DeleteMapping("/requests/{id}")
    public ResponseEntity<?> deleteRequest(
        @RequestHeader(value = "JWT", defaultValue = "") String token,
        @PathVariable long id
    ) {
        String username = token.isEmpty() ? null : jwtService.getAuthUser(token.replaceFirst("Bearer ", ""));
        return ResponseEntity.ok()
            .contentType(APPLICATION_JSON)
            .body(bot.deleteRequest(id, username));
    }

    @ResponseStatus(BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Map<String,String> handleValidationExceptions(MethodArgumentNotValidException ex) {
        var errors = new HashMap<String,String>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            var fieldError = (FieldError)error;
            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        });
        return errors;
    }
}