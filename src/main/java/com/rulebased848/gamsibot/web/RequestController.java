package com.rulebased848.gamsibot.web;

import com.rulebased848.gamsibot.core.GamsiBot;
import com.rulebased848.gamsibot.domain.Request;
import com.rulebased848.gamsibot.domain.RequestPayload;
import com.rulebased848.gamsibot.domain.User;
import com.rulebased848.gamsibot.domain.UserRepository;
import com.rulebased848.gamsibot.service.JwtService;
import java.io.IOException;
import java.util.HashMap;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
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
        var channelId = payload.getChannelId();
        var info = fetcher.fetchChannelInfo(channelId);
        if (!(boolean)info.get("isValid")) {
            var body = new HashMap<String,Object>(1);
            body.put("message", "The channel ID is not valid.");
            return ResponseEntity.badRequest()
                .contentType(APPLICATION_JSON)
                .body(body);
        }
        var targetSubscriberCount = payload.getTargetSubscriberCount();
        if (Long.compareUnsigned((long)info.get("subscriberCount"), targetSubscriberCount) >= 0) {
            var body = new HashMap<String,Object>(1);
            body.put("message", "The target subscriber count is already achieved.");
            return ResponseEntity.badRequest()
                .contentType(APPLICATION_JSON)
                .body(body);
        }
        User user = null;
        if (!token.isEmpty()) {
            var username = jwtService.getAuthUser(token.replaceFirst("Bearer ", ""));
            var maybeUser = repository.findByUsername(username);
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
        bot.newRequest(request);
        return ResponseEntity.ok().build();
    }
}