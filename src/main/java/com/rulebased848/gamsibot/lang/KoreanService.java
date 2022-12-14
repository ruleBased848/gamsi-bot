package com.rulebased848.gamsibot.lang;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;

@Service
public class KoreanService implements LanguageService {
    private static final Pattern pattern = Pattern.compile("구독자 ([^\"]+)명");

    @Override
    public Optional<Long> findYoutubeSubscriberCount(String text) {
        Matcher matcher = pattern.matcher(text);
        String found = null;
        while (matcher.find()) {
            found = matcher.group(1);
        }
        return found == null ? Optional.empty() : Optional.of(parseLong(found));
    }

    @Override
    public long parseLong(String num) {
        char last = num.charAt(num.length() - 1);
        if (Character.isDigit(last)) return Long.parseLong(num);
        float value = Float.parseFloat(num.substring(0, num.length() - 1));
        if (last == '천') return (long)(value * 1000);
        if (last == '만') return (long)(value * 10000);
        if (last == '억') return (long)(value * 100000000);
        throw new NumberFormatException("For input string: \"" + num + "\"");
    }
}