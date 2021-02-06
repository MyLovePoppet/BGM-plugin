package com.shuqy.bgm.service.lyric;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shuqy.bgm.entity.Lyric;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class CloudMusicLyricService extends AbstractLyricService {
    //网易云音乐的歌词形式       [00:00.001]{歌词}
    private final Pattern lyricPattern = Pattern.compile("\\[\\d+:[0-5][0-9].\\d+]");
    //HttpClient
    private final HttpClient httpClient = HttpClient.newBuilder().build();
    //jackson解析
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Lyric deFormat(String rawTextLyric) {
        //正则表达式匹配
        Matcher matcher = lyricPattern.matcher(rawTextLyric);
        if (matcher.find()) {
            double position = 0.0;
            int leftBracket = rawTextLyric.indexOf('[');
            int colon = rawTextLyric.indexOf(':');
            int dot = rawTextLyric.indexOf('.');
            int rightBracket = rawTextLyric.indexOf(']');
            //计算position
            position += Integer.parseInt(rawTextLyric.substring(leftBracket + 1, colon)) * 60.0;
            position += Integer.parseInt(rawTextLyric.substring(colon + 1, dot));
            position += Integer.parseInt(rawTextLyric.substring(dot + 1, rightBracket)) * 0.001;
            //歌词字符串
            String text = rawTextLyric.substring(rightBracket + 1);
            return new Lyric(position, text);
        } else {
            log.error("Cloud music deFormat lyric error: " + rawTextLyric);
            return new Lyric(0.0, rawTextLyric);
        }
    }

    /**
     * 先从网易云音乐歌词api获取json如下：
     * {
     * "sgc": true,
     * "sfy": true,
     * "qfy": true,
     * "lrc": {
     * "version": 7,
     * "lyric": "[00:00.000] 作词 : Youngior\n[00:00.998] 作曲 : Youngior\n
     * },
     * "klyric": {
     * "version": 0,
     * "lyric": ""
     * },
     * "tlyric": {
     * "version": 0,
     * "lyric": ""
     * },
     * "code": 200
     * }
     * 接着解析其"lrc"->"lyric"字段即可
     *
     * @param identifier 歌曲的标识符，在这里为网易云音乐的歌曲id
     * @return 解析完成的所有的歌词
     */
    @Override
    public List<Lyric> getCurrentLyrics(String identifier) {
        //网易云音乐的歌词URL
        String lyricURL = "https://music.163.com/api/song/lyric?id={0}&lv=1&kv=1&tv=-1";
        String url = MessageFormat.format(lyricURL, identifier);
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .build();
        try {
            HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            JsonNode rootNode = objectMapper.readTree(httpResponse.body());
            //如果是没有歌词的情况
            if (rootNode.has("nolyric")) {
                if (rootNode.path("nolyric").asBoolean()) {
                    return Collections.emptyList();
                }
            }
            //找到歌词部分
            JsonNode lrcNode = rootNode.path("lrc");
            String allLyrics = lrcNode.path("lyric").asText();
            //转化为list
            String[] lyricStr = allLyrics.split("\n");
            List<Lyric> lyricList = new LinkedList<>();
            for (String rawLyricText : lyricStr) {
                lyricList.add(deFormat(rawLyricText));
            }
            //排序一下以防万一
            lyricList.sort(Comparator.comparingDouble(Lyric::getPosition));
            return lyricList;
        } catch (IOException | InterruptedException e) {
            log.error("Lyric format error: " + e.getMessage());
        }
        return Collections.emptyList();
    }
}
