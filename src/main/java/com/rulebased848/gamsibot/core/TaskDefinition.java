package com.rulebased848.gamsibot.core;

import com.rulebased848.gamsibot.domain.Request;
import com.rulebased848.gamsibot.domain.RequestRepository;
import com.rulebased848.gamsibot.web.YoutubeChannelInfoFetcher;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import static org.slf4j.LoggerFactory.getLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TaskDefinition {
    private static final Logger logger = getLogger(TaskDefinition.class);

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
            Map<String,Object> info = fetcher.fetchChannelInfo(handle);
            Instant timeStamp = Instant.now();
            if (!(boolean)info.get("isValid")) {
                repository.deleteByHandle(handle);
                return;
            }
            var subscriberCount = (long)info.get("subscriberCount");
            List<Request> requests = repository.findByHandleAndTargetSubscriberCountLessThanEqual(handle, subscriberCount);
            repository.deleteAll(requests);
            for (Request request : requests) {
                boolean success = emailUtil.sendEmail(request.getEmailAddress(), handle, subscriberCount, timeStamp);
                if (!success) {
                    logger.warn("Email not sent.");
                }
            }
        };
    }
}