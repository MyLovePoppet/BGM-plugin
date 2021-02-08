package com.shuqy.bgm.service;

import com.shuqy.bgm.entity.BGMInfo;
import com.shuqy.bgm.entity.Lyric;
import com.shuqy.bgm.entity.MusicInfo;
import com.shuqy.bgm.service.info.*;
import com.shuqy.bgm.service.lyric.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

@Slf4j
@Service
public class BGMService {
    /**
     * 检查播放器的类型
     *
     * @return 播放器的类型
     * 0        ---->网易云音乐
     * 1        ---->QQ音乐
     * 2        ---->酷狗音乐
     * ...
     * 其他<0   --->无播放器或者不支持的播放器
     */
    private native int checkPlayer();

    /**
     * 初始化DLL内数据
     *
     * @param playerType player的Type
     * @return 是否初始化成功, 0表示初始化成功，其他表示GetLastError()返回的值
     */
    private native int initNativeDll(int playerType);

    /**
     * dll内资源释放，可以不用手动调用，dll内使用智能指针进行释放资源
     */
    private native void nativeGc();

    /**
     * 获取歌曲的title
     *
     * @return 歌曲的title
     */
    private native String getMusicTitle();

    /**
     * 获取当前的播放位置
     *
     * @return double类型的数据，以秒为单位
     */
    private native double getCurrentPosition();


    @Value("${bgm-service.dllPath}")
    private String dllPath;

    //歌词处理部分
    private ILyricService lyricService;
    //歌曲信息部分
    private IMusicInfoService infoService;

    private boolean isServiceInitialized = false;
    private boolean isDllInitialized = false;
    private int currentPlayerType = -1;
    private String currentMusicIdentifier = "";
    private String currentMusicTitle = "";
    private double currentMusicPosition = 0.0;
    private double currentMusicDuration = 0.0;
    private String currentLyric = "";
    private List<Lyric> currentMusicLyricList = Collections.emptyList();
    private ListIterator<Lyric> currentLyricIterator = currentMusicLyricList.listIterator();

    /**
     * 初始化服务
     *
     * @return 是否初始化成功
     */
    public boolean initService() {
        if (!isDllInitialized) {
            try {
                String dllLocation = new File(dllPath).getCanonicalPath();
                System.load(dllLocation);
            } catch (IOException e) {
                log.error("Initialize dll file at: " + dllPath + " error!");
                return false;
            }
            log.info("Initialize dll file at: " + dllPath + " ok!");
            isDllInitialized = true;
        }
        //先检查播放器类型
        int resType = checkPlayer();
        if (resType < 0) {
            log.error("No support player as check player returns: " + resType);
            isServiceInitialized = false;
            return false;
        }
        return initService(resType);
    }

    /**
     * 初始化服务
     *
     * @param playerType player的类型
     * @return 是否初始化成功
     */
    private boolean initService(int playerType) {
        if (playerType < 0) {
            return false;
        }
        currentPlayerType = playerType;
        switch (currentPlayerType) {
            case 0:
                lyricService = new CloudMusicLyricService();
                infoService = new CloudMusicInfoService();
                break;
            case 1:
                lyricService = new QQMusicLyricService();
                infoService = new QQMusicInfoService();
                break;
            case 2:
                lyricService = new KuGouMusicLyricService();
                infoService = new KuGouMusicInfoService();
                break;
            default: {
                isServiceInitialized = false;
                return false;
            }
        }
        //初始化dll
        int initRes = initNativeDll(currentPlayerType);
        if (initRes == 0) {
            log.info("Init native dll data success!");
            isServiceInitialized = true;
            return true;
        } else {
            log.error("Init native dll data failed as GetLastError() returns: " + initRes);
            isServiceInitialized = false;
            return false;
        }
    }

    /**
     * 更新内部数据
     *
     * @return 是否成功
     */
    private boolean updateData() {
        int playerType = checkPlayer();

        //检查类型
        if (playerType < 0) {
            log.warn("No support player type...");
            currentPlayerType = playerType;
            return false;
        }
        if ((currentPlayerType != playerType) //类型变了
                || (!isServiceInitialized)) /*server没有初始化*/ {
            log.info("Player type changed from " + currentPlayerType + " to " + playerType);
            currentPlayerType = playerType;
            isServiceInitialized = false;
            if (!initService(playerType)) {
                return false;
            }
        }
        switch (currentPlayerType) {
            case 0:
                return updateCloudMusicData();
            case 1:
                return updateQQMusicData();
            case 2:
                return updateKuGouMusicData();
            default:
                return false;
        }
    }

    //网易云音乐更新数据方式
    private boolean updateCloudMusicData() {
        MusicInfo musicInfo = infoService.getMusicInfo();
        //如果是空的数据
        //这里使用==是因为MusicInfo.emptyInfo()返回的对象是唯一的，比较地址时会相等
        if (musicInfo == MusicInfo.emptyInfo()) {
            log.warn("Get music info failed...");
            return false;
        }
        String currentID = musicInfo.getIdentifier();
        //根据id来判断，有一定延迟，在2s左右，暂时无法解决
        if (!currentMusicIdentifier.equals(currentID)) {
            //更新id
            currentMusicIdentifier = currentID;
            //更新duration
            currentMusicDuration = musicInfo.getDuration();
            //更新title
            currentMusicTitle = getMusicTitle();
            //更新歌曲的歌词
            currentMusicLyricList = lyricService.getCurrentLyrics(currentMusicIdentifier);
            //更新歌词迭代器
            currentLyricIterator = currentMusicLyricList.listIterator();
        }
        //更新当前position
        currentMusicPosition = getCurrentPosition();
        //更新当前歌词显示
        currentLyric = lyricService.getLyricByPosition(currentLyricIterator, currentMusicPosition).getText();
        return true;
    }

    //QQ音乐更新数据方式
    private boolean updateQQMusicData() {
        String title = getMusicTitle();
        //根据title来判断
        if (!currentMusicTitle.equals(title)) {
            currentMusicTitle = title;
            //音乐信息
            MusicInfo musicInfo = infoService.getMusicInfo(currentMusicTitle);
            //更新数据
            currentMusicDuration = musicInfo.getDuration();
            currentMusicIdentifier = musicInfo.getIdentifier();
            currentMusicLyricList = lyricService.getCurrentLyrics(currentMusicIdentifier);
            currentLyricIterator = currentMusicLyricList.listIterator();
        }
        //更新当前position
        currentMusicPosition = getCurrentPosition();
        //更新当前歌词显示
        currentLyric = lyricService.getLyricByPosition(currentLyricIterator, currentMusicPosition).getText();
        return true;
    }

    //QQ音乐更新数据方式
    private boolean updateKuGouMusicData() {
        String title = getMusicTitle();
        //根据title来判断
        if (!currentMusicTitle.equals(title)) {
            currentMusicTitle = title;
            //音乐信息
            MusicInfo musicInfo = infoService.getMusicInfo(currentMusicTitle);
            //更新数据
            currentMusicDuration = musicInfo.getDuration();
            currentMusicIdentifier = musicInfo.getIdentifier();
            currentMusicLyricList = lyricService.getCurrentLyrics(currentMusicIdentifier);
            currentLyricIterator = currentMusicLyricList.listIterator();
        }
        //更新当前position
        currentMusicPosition = getCurrentPosition();
        //更新当前歌词显示
        currentLyric = lyricService.getLyricByPosition(currentLyricIterator, currentMusicPosition).getText();
        return true;
    }

    /**
     * 获取当前的BGM信息
     *
     * @return BGMINFO
     */
    public BGMInfo getCurrentBGMInfo() {
        if (updateData()) {
            log.info("update BGM info success!");
            return new BGMInfo(200, currentPlayerType, currentMusicTitle, currentMusicDuration, currentMusicPosition, currentLyric);
        } else {
            log.error("update BGM info failed!");
            return BGMInfo.emptyBGMInfo();
        }
    }

    /**
     * 重写finalize，进行nativeGC
     *
     * @throws Throwable Throwable
     */
    @Override
    protected void finalize() throws Throwable {
        nativeGc();
    }
}
