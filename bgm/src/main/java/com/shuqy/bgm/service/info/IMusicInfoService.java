package com.shuqy.bgm.service.info;

import com.shuqy.bgm.entity.MusicInfo;

public interface IMusicInfoService {
    /**
     * 获取当前播放歌曲的信息，由下层实现
     *
     * @param title 当前播放歌曲的title（不是必须的参数）
     * @return 歌曲信息
     */
    MusicInfo getMusicInfo(String title);
}
