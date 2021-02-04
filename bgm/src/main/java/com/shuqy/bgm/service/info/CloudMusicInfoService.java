package com.shuqy.bgm.service.info;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shuqy.bgm.entity.MusicInfo;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
public class CloudMusicInfoService implements IMusicInfoService {
    //jackson
    private final ObjectMapper objectMapper = new ObjectMapper();
    //网易云音乐的历史存放文件
    private final Path historyPath = Paths.get(System.getenv("LOCALAPPDATA"), "Netease\\CloudMusic\\webdata\\file\\history");

    /**
     * C:\Users\[userName]\AppData\Local\Netease\CloudMusic\webdata\file\history
     * 上述文件是一个json数据格式，存放的是播放的历史记录，第一条即是最新播放的数据，根据该数据可以找到歌曲id
     * 进而找到歌词所在
     * 该方式的缺点是网易云音乐客户端在播放新的歌曲的时候不会立即刷新该文件，会有个2s~3s左右的延迟，暂时不知道怎么解决
     *
     * @return 当前播放的歌曲信息
     */
    @Override
    public MusicInfo getMusicInfo() {
        try {
            JsonNode rootNode = objectMapper.readTree(Files.newBufferedReader(historyPath));
            //返回的是数组，取第一个即可
            rootNode = rootNode.path(0);
            JsonNode trackNode = rootNode.path("track");
            //"track"-->"id"
            String id = trackNode.path("id").asText("");
            //"track"-->"duration"
            double duration = trackNode.path("duration").asInt(0) / 1000.0;
            return new MusicInfo(id, duration);
        } catch (IOException e) {
            log.error("get music info error: " + e.getMessage());
        }
        return MusicInfo.emptyInfo();
    }
}
