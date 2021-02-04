package com.shuqy.bgm.service.lyric;

import com.shuqy.bgm.entity.Lyric;

import java.util.List;
import java.util.ListIterator;

public interface ILyricService {
    /**
     * 通过原生的每一条歌词信息解析成Lyric格式
     * 如网易云音乐:  [00:00.001]{歌词} ----> Lyric(0.001, "歌词");
     *
     * @param rawTextLyric 原生的每一条歌词信息
     * @return Lyric格式
     */
    Lyric deFormat(String rawTextLyric);

    /**
     * 通过歌曲的标识符找到该歌的所有歌词
     * 如网易云音乐的标识符是对应的歌曲id, 通过该id访问接口
     * https://music.163.com/api/song/lyric?id={歌曲id}&lv=1&kv=1&tv=-1
     * 即可得到json格式的歌词，最后解析即可
     *
     * @param identifier 歌曲的标识符
     * @return 一个歌曲的所有歌词
     */
    List<Lyric> getCurrentLyrics(String identifier);

    /**
     * 通过lyric迭代器以及Position获取position处的歌词
     *
     * @param lyricIter lyric迭代器，一般通过List<Lyric>来获取其迭代器
     * @param position  所需要的时间信息
     * @return 当前时间下的Lyric
     */
    Lyric getLyricByPosition(ListIterator<Lyric> lyricIter, double position);
}
