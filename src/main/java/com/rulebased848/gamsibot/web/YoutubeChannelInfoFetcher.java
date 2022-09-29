package com.rulebased848.gamsibot.web;

import com.rulebased848.gamsibot.lang.LanguageService;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.jsoup.HttpStatusException;
import static org.jsoup.Jsoup.connect;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class YoutubeChannelInfoFetcher {
    private static final String youtubeChannelUrl = "https://www.youtube.com/channel/";

    private final LanguageService languageService;

    @Autowired
    public YoutubeChannelInfoFetcher(final LanguageService languageService) {
        this.languageService = languageService;
    }

    public Map<String,Object> fetchChannelInfo(String channelId) throws IOException {
        var info = new HashMap<String,Object>();
        info.put("isValid", false);
        if (channelId == null || channelId.length() == 0) {
            return info;
        }
        Document doc = null;
        try {
            doc = connect(youtubeChannelUrl + channelId).get();
        } catch (HttpStatusException hse) {
            return info;
        }
        var scripts = doc.select("script");
        for (var script : scripts) {
            if (script.html().contains("alerts")) {
                return info;
            }
        }
        info.put("isValid", true);
        for (var script : scripts) {
            var count = languageService.findYoutubeSubscriberCount(script.html());
            if (count.isPresent()) {
                info.put("subscriberCount", count.get());
                return info;
            }
        }
        return info;
    }
}