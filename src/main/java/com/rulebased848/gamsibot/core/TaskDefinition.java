package com.rulebased848.gamsibot.core;

import com.rulebased848.gamsibot.domain.Request;
import com.rulebased848.gamsibot.domain.RequestRepository;
import com.rulebased848.gamsibot.web.YoutubeChannelInfoFetcher;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import javax.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TaskDefinition {
    private final RequestRepository repository;

    private final YoutubeChannelInfoFetcher fetcher;

    private final EmailUtil emailUtil;

    @Autowired
    public TaskDefinition(RequestRepository repository, YoutubeChannelInfoFetcher fetcher, EmailUtil emailUtil) {
        this.repository = repository;
        this.fetcher = fetcher;
        this.emailUtil = emailUtil;
    }

    public Runnable getTask(String handle) {
        return () -> {
            Map<String,Object> info;
            try {
                info = fetcher.fetchChannelInfo(handle);
            } catch (IOException ioe) {
                repository.deleteByHandle(handle);
                return;
            }
            Instant timeStamp = Instant.now();
            if (!(boolean)info.get("isValid")) {
                repository.deleteByHandle(handle);
                return;
            }
            var subscriberCount = (long)info.get("subscriberCount");
            List<Request> requests = repository.findByHandleAndTargetSubscriberCountLessThanEqual(handle, subscriberCount);
            repository.deleteAll(requests);
            for (Request request : requests) {
                try {
                    emailUtil.sendEmail(request.getEmailAddress(), handle, subscriberCount, timeStamp);
                } catch (UnsupportedEncodingException uee) {
                } catch (MessagingException me) {
                }
            }
        };
    }
}