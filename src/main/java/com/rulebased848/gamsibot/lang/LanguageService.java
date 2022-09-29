package com.rulebased848.gamsibot.lang;

import java.util.Optional;

public interface LanguageService {
    Optional<Long> findYoutubeSubscriberCount(String text);

    long parseLong(String num);
}