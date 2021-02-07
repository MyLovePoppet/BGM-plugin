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
public class QQMusicLyricService extends AbstractLyricService{
    //qq音乐的歌词形式       [00:00.001]{歌词}
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
            log.error("QQ music deFormat lyric error: " + rawTextLyric);
            return new Lyric(0.0, rawTextLyric);
        }
    }
    /**
     * 先从qq音乐歌词api获取json如下：
     * {
     *   "retcode": 0,
     *   "code": 0,
     *   "subcode": 0,
     *   "lyric": "[ti:My Heart Will Go On]\n[ar:夏呈青]
     *   "trans": ""
     * }
     * 接着解析其"lyric"字段即可
     *
     * @param identifier 歌曲的标识符，在这里为网易云音乐的歌曲id
     * @return 解析完成的所有的歌词
     */
    @Override
    public List<Lyric> getCurrentLyrics(String identifier) {
        //网易云音乐的歌词URL
        String lyricURL = "https://c.y.qq.com/lyric/fcgi-bin/fcg_query_lyric_new.fcg?songmid={0}&format=json&nobase64=1";
        String url = MessageFormat.format(lyricURL, identifier);
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .headers("Referer", "https://y.qq.com/portal/player.html")
                .build();
        try {
            HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            JsonNode rootNode = objectMapper.readTree(httpResponse.body());
            String allLyrics = rootNode.path("lyric").asText();
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
