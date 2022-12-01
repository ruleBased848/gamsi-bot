package com.rulebased848.gamsibot.web;

import com.rulebased848.gamsibot.lang.LanguageService;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import static org.jsoup.Jsoup.connect;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class YoutubeChannelInfoFetcher {
    private static final String youtubeChannelUrl = "https://www.youtube.com/@";

    private final LanguageService languageService;

    @Autowired
    public YoutubeChannelInfoFetcher(final LanguageService languageService) {
        this.languageService = languageService;
    }

    public Map<String,Object> fetchChannelInfo(String handle) {
        Map<String,Object> info = new HashMap<>();
        info.put("isValid", false);
        if (handle == null || handle.length() == 0) return info;
        Connection connection = connect(youtubeChannelUrl + handle);
        Document doc;
        while (true) {
            try {
                doc = connection.get();
            } catch (HttpStatusException hse) {
                return info;
            } catch (IOException ioe) {
                continue;
            }
            break;
        }
        Elements scripts = doc.select("script");
        for (Element script : scripts) {
            if (script.html().contains("alerts")) return info;
        }
        info.put("isValid", true);
        for (Element script : scripts) {
            Optional<Long> count = languageService.findYoutubeSubscriberCount(script.html());
            if (count.isPresent()) {
                info.put("subscriberCount", count.get());
                return info;
            }
        }
        return info;
    }
}