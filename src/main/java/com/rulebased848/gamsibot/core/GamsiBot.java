package com.rulebased848.gamsibot.core;

import com.rulebased848.gamsibot.domain.Request;
import com.rulebased848.gamsibot.domain.RequestRepository;
import com.rulebased848.gamsibot.web.YoutubeChannelInfoFetcher;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
        for (var r : repository.findAllChannelIdWithMinimumTargetSubscriberCount()) {
            updateThisWithNewRequest(r.getChannelId(), r.getTargetSubscriberCount());
        }
    }

    public synchronized void newRequest(Request request) {
        repository.save(request);
        updateThisWithNewRequest(request.getChannelId(), request.getTargetSubscriberCount());
    }

    private void updateThisWithNewRequest(String channelId, long targetSubscriberCount) {
        var currentTarget = targets.get(channelId);
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
                if (!(boolean)info.get("isValid")) {
                    break;
                }
                var subscriberCount = (long)info.get("subscriberCount");
                List<Request> requests = null;
                Long target;
                synchronized (GamsiBot.this) {
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
                if (target == null) {
                    return;
                }
                long sleepTimeMillis = (target - subscriberCount) * factor;
                try {
                    sleep(sleepTimeMillis < 0 ? Long.MAX_VALUE : sleepTimeMillis);
                } catch (InterruptedException ie) {}
            }
            synchronized (GamsiBot.this) {
                repository.deleteByChannelId(channelId);
                targets.remove(channelId);
                threads.remove(channelId);
            }
        }
    }
}