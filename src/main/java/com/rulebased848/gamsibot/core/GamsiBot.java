package com.rulebased848.gamsibot.core;

import com.rulebased848.gamsibot.domain.ChannelIdAndTargetSubscriberCount;
import com.rulebased848.gamsibot.domain.Request;
import com.rulebased848.gamsibot.domain.RequestRepository;
import com.rulebased848.gamsibot.web.YoutubeChannelInfoFetcher;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class GamsiBot implements CommandLineRunner {
    private static final Map<String,Long> targets = new HashMap<>();

    private static final Map<String,SubscriberCountThread> threads = new HashMap<>();

    private final RequestRepository repository;

    private final YoutubeChannelInfoFetcher fetcher;

    private final EmailUtil emailUtil;

    @Autowired
    public GamsiBot(
        final RequestRepository repository,
        final YoutubeChannelInfoFetcher fetcher,
        final EmailUtil emailUtil
    ) {
        this.repository = repository;
        this.fetcher = fetcher;
        this.emailUtil = emailUtil;
    }

    @Override
    public synchronized void run(String... args) throws Exception {
        for (ChannelIdAndTargetSubscriberCount r : repository.findAllChannelIdWithMinimumTargetSubscriberCount()) {
            updateThisWithNewRequest(r.getChannelId(), r.getTargetSubscriberCount());
        }
    }

    public synchronized Request newRequest(Request request) {
        updateThisWithNewRequest(request.getChannelId(), request.getTargetSubscriberCount());
        return repository.save(request);
    }

    private void updateThisWithNewRequest(String channelId, long targetSubscriberCount) {
        Long currentTarget = targets.get(channelId);
        if (currentTarget == null) {
            targets.put(channelId, targetSubscriberCount);
            var thread = new SubscriberCountThread(channelId);
            thread.start();
            threads.put(channelId, thread);
        } else if (Long.compareUnsigned(targetSubscriberCount, currentTarget) < 0) {
            targets.put(channelId, targetSubscriberCount);
            threads.get(channelId).interrupt();
        }
    }

    public synchronized Map<String,Object> deleteRequest(long id, String username) {
        Map<String,Object> result = new HashMap<>();
        result.put("success", false);
        Optional<Request> maybeRequest = repository.findById(id);
        if (maybeRequest.isEmpty()) {
            result.put("reason", "The request ID is not valid.");
            return result;
        }
        var request = maybeRequest.get();
        var user = request.getRequester();
        if (user != null && user.getUsername() != username) {
            result.put("reason", "You do not have permission.");
            return result;
        }
        repository.delete(request);
        String channelId = request.getChannelId();
        if (Long.compareUnsigned(request.getTargetSubscriberCount(), targets.get(channelId)) == 0) {
            Long target = repository.findMinimumTargetSubscriberCountByChannelId(channelId);
            if (target != null) {
                targets.put(channelId, target);
            } else {
                targets.remove(channelId);
                threads.get(channelId).interrupt();
                threads.remove(channelId);
            }
        }
        result.put("success", true);
        return result;
    }

    private class SubscriberCountThread extends Thread {
        private static final int factor = 60000;

        private final String channelId;

        public SubscriberCountThread(String channelId) {
            this.channelId = channelId;
        }

        @Override
        public void run() {
            while (true) {
                Map<String,Object> info = null;
                try {
                    info = fetcher.fetchChannelInfo(channelId);
                } catch (IOException ioe) {
                    break;
                }
                var timeStamp = Instant.now();
                if (!(boolean)info.get("isValid")) break;
                var subscriberCount = (long)info.get("subscriberCount");
                List<Request> requests = null;
                Long target;
                synchronized (GamsiBot.this) {
                    if (targets.get(channelId) == null) return;
                    if (Long.compareUnsigned(subscriberCount, target = targets.get(channelId)) >= 0) {
                        requests = repository.findByChannelIdAndTargetSubscriberCountLessThanEqual(channelId, subscriberCount);
                        repository.deleteAll(requests);
                        if ((target = repository.findMinimumTargetSubscriberCountByChannelId(channelId)) != null) {
                            targets.put(channelId, target);
                        } else {
                            targets.remove(channelId);
                            threads.remove(channelId);
                        }
                    }
                }
                if (requests != null) {
                    for (var request : requests) {
                        try {
                            emailUtil.sendEmail(request.getEmailAddress(), channelId, subscriberCount, timeStamp);
                        } catch (UnsupportedEncodingException uee) {
                        } catch (MessagingException me) {
                        }
                    }
                }
                if (target == null) return;
                long sleepTimeMillis = (target - subscriberCount) * factor;
                try {
                    sleep(sleepTimeMillis < 0 ? Long.MAX_VALUE : sleepTimeMillis);
                } catch (InterruptedException ie) {}
            }
            synchronized (GamsiBot.this) {
                if (targets.get(channelId) == null) return;
                repository.deleteByChannelId(channelId);
                targets.remove(channelId);
                threads.remove(channelId);
            }
        }
    }
}