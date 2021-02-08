package com.shuqy.bgm.service.lyric;

import com.shuqy.bgm.entity.Lyric;
import com.shuqy.bgm.service.KuGouMusicUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
public class KuGouMusicLyricService extends AbstractLyricService {
    private final Pattern pattern = Pattern.compile("\\[\\d+,\\d+]");

    @Override
    public Lyric deFormat(String rawTextLyric) {
        Matcher matcher = pattern.matcher(rawTextLyric);
        if (matcher.find()) {
            double position = Double.parseDouble(rawTextLyric.substring(1, rawTextLyric.indexOf(','))) / 1000.0;
            String text = rawTextLyric.substring(rawTextLyric.indexOf(']') + 1);
            text = text.replaceAll("<\\d+,\\d+,\\d+>", "");
            return new Lyric(position, text);
        } else {
            log.warn("Failed to deFormat KuGou lyric as: " + rawTextLyric);
            return Lyric.emptyLyric();
        }
    }

    @Override
    public List<Lyric> getCurrentLyrics(String identifier) {
        if (KuGouMusicUtils.parse(identifier)) {
            return KuGouMusicUtils.getCurrentLyric().stream()
                    .filter(s -> pattern.matcher(s).find())
                    .map(this::deFormat)
                    .collect(Collectors.toList());
        } else {
            log.error("Get lyric list as: " + identifier + " error!");
            return Collections.emptyList();
        }
    }
}
