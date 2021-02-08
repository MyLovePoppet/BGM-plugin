package com.shuqy.bgm.service.info;

import com.shuqy.bgm.entity.MusicInfo;
import com.shuqy.bgm.service.KuGouMusicUtils;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class KuGouMusicInfoService implements IMusicInfoService {

    @Override
    public MusicInfo getMusicInfo(String... args) {
        if (args.length < 1) {
            return MusicInfo.emptyInfo();
        }
        String title = args[0];
        if (KuGouMusicUtils.parse(title)) {
            return new MusicInfo(title, KuGouMusicUtils.getCurrentDuration());
        } else {
            log.error("Get MusicInfo as: " + title + " error!");
            return MusicInfo.emptyInfo();
        }
    }
}
