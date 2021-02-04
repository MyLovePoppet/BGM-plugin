package com.shuqy.bgm.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BGMInfo {
    //返回code
    private int code;
    //播放器类型
    private int playerType;
    //歌曲title
    private String title;
    //歌曲时长
    private double duration;
    //歌曲当前播放位置
    private double position;
    //歌曲当前歌词
    private String lyric;

    private static final BGMInfo EMPTY_BGM_INFO = new BGMInfo(500, -1, "", 0.0, 0.0, "");

    public static BGMInfo emptyBGMInfo() {
        return EMPTY_BGM_INFO;
    }
}
