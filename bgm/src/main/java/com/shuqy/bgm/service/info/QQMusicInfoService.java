package com.shuqy.bgm.service.info;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shuqy.bgm.entity.MusicInfo;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;

@Slf4j
public class QQMusicInfoService implements IMusicInfoService {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String infoURL = "https://c.y.qq.com/soso/fcgi-bin/client_search_cp?p=1&n=1&w={0}&format=json";
    private final HttpClient httpClient = HttpClient.newBuilder().build();

    /**
     * 解析如下json
     * {
     *   "code": 0,
     *   "data": {
     *     "keyword": "My Heart Will Go On 满舒克&#047;MuSik I&#047;廖伟珊",      ---->title
     *     "song": {
     *       "list": [
     *         {
     *           "interval": 262,   ---->时长
     *           "songmid": "002WFWqf1cLI08",   ----->ID
     *           }
     *           ],
     *       "totalnum": 69
     *     },
     * }
     * @param title 当前播放歌曲的title（不是必须的参数）
     *
     * @return MusicInfo
     */
    @Override
    public MusicInfo getMusicInfo(String title) {
        title = URLEncoder.encode(title, StandardCharsets.UTF_8);
        HttpRequest infoRequest = HttpRequest.newBuilder()
                .uri(URI.create(MessageFormat.format(infoURL, title)))
                .build();
        try {
            HttpResponse<String> httpResponse = httpClient.send(infoRequest, HttpResponse.BodyHandlers.ofString());
            JsonNode rootNode = objectMapper.readTree(httpResponse.body());
            JsonNode songList = rootNode.path("data").path("song").path("list");
            JsonNode song = songList.path(0);
            double duration = song.path("interval").asDouble(0.0);
            String identifier = song.path("songmid").asText("");
            return new MusicInfo(identifier, duration);
        } catch (IOException | InterruptedException e) {
            log.error("Get qq music info error: " + e.getMessage());
        }
        return MusicInfo.emptyInfo();
    }
}
