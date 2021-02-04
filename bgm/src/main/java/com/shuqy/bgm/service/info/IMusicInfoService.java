package com.shuqy.bgm.service.info;

import com.shuqy.bgm.entity.MusicInfo;

public interface IMusicInfoService {
    /**
     * 获取歌曲的信息，由下层实现
     *
     * @return 歌曲信息
     */
    MusicInfo getMusicInfo();
}
