package com.rulebased848.gamsibot.core;

import com.rulebased848.gamsibot.domain.HandleAndTargetSubscriberCount;
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
        for (HandleAndTargetSubscriberCount r : repository.findAllHandleWithMinimumTargetSubscriberCount()) {
            updateThisWithNewRequest(r.getHandle(), r.getTargetSubscriberCount());
        }
    }

    public synchronized Request newRequest(Request request) {
        updateThisWithNewRequest(request.getHandle(), request.getTargetSubscriberCount());
        return repository.save(request);
    }

    private void updateThisWithNewRequest(String handle, long targetSubscriberCount) {
        Long currentTarget = targets.get(handle);
        if (currentTarget == null) {
            targets.put(handle, targetSubscriberCount);
            var thread = new SubscriberCountThread(handle);
            thread.start();
            threads.put(handle, thread);
        } else if (Long.compareUnsigned(targetSubscriberCount, currentTarget) < 0) {
            targets.put(handle, targetSubscriberCount);
            threads.get(handle).interrupt();
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
        if (user != null && !user.getUsername().equals(username)) {
            result.put("reason", "You do not have permission.");
            return result;
        }
        repository.delete(request);
        String handle = request.getHandle();
        if (Long.compareUnsigned(request.getTargetSubscriberCount(), targets.get(handle)) == 0) {
            Long target = repository.findMinimumTargetSubscriberCountByHandle(handle);
            if (target != null) {
                targets.put(handle, target);
            } else {
                targets.remove(handle);
                threads.get(handle).interrupt();
                threads.remove(handle);
            }
        }
        result.put("success", true);
        return result;
    }

    private class SubscriberCountThread extends Thread {
        private static final int factor = 60000;

        private final String handle;

        public SubscriberCountThread(String handle) {
            this.handle = handle;
        }

        @Override
        public void run() {
            while (true) {
                Map<String,Object> info = null;
                try {
                    info = fetcher.fetchChannelInfo(handle);
                } catch (IOException ioe) {
                    break;
                }
                var timeStamp = Instant.now();
                if (!(boolean)info.get("isValid")) break;
                var subscriberCount = (long)info.get("subscriberCount");
                List<Request> requests = null;
                Long target;
                synchronized (GamsiBot.this) {
                    if (targets.get(handle) == null) return;
                    if (Long.compareUnsigned(subscriberCount, target = targets.get(handle)) >= 0) {
                        requests = repository.findByHandleAndTargetSubscriberCountLessThanEqual(handle, subscriberCount);
                        repository.deleteAll(requests);
                        if ((target = repository.findMinimumTargetSubscriberCountByHandle(handle)) != null) {
                            targets.put(handle, target);
                        } else {
                            targets.remove(handle);
                            threads.remove(handle);
                        }
                    }
                }
                if (requests != null) {
                    for (var request : requests) {
                        try {
                            emailUtil.sendEmail(request.getEmailAddress(), handle, subscriberCount, timeStamp);
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
                if (targets.get(handle) == null) return;
                repository.deleteByHandle(handle);
                targets.remove(handle);
                threads.remove(handle);
            }
        }
    }
}