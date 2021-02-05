package com.shuqy.bgm.service.lyric;

import com.shuqy.bgm.entity.Lyric;

import java.util.List;
import java.util.ListIterator;

public abstract class AbstractLyricService implements ILyricService {
    /**
     * 需要下一层实现
     *
     * @param rawTextLyric 原生的每一条歌词信息
     * @return 解析后的Lyric
     */
    public abstract Lyric deFormat(String rawTextLyric);

    /**
     * 需要下一层实现
     *
     * @param identifier 歌曲的标识符
     * @return 所有的歌词实现
     */
    public abstract List<Lyric> getCurrentLyrics(String identifier);

    /**
     * 默认实现
     *
     * @param lyricIter lyric迭代器，一般通过List<Lyric>来获取其迭代器
     * @param position  所需要的时间信息
     * @return position位置的Lyric
     */
    public Lyric getLyricByPosition(ListIterator<Lyric> lyricIter, double position) {
        //空的迭代器
        if (!lyricIter.hasNext() && !lyricIter.hasPrevious()) {
            return Lyric.emptyLyric();
        }
        Lyric prev, next;
        //没有下一个，就向上找
        if (!lyricIter.hasNext()) {
            next = lyricIter.previous();
            //只有一个
            if (!lyricIter.hasPrevious()) {
                return next;
            }
            prev = lyricIter.previous();
        } else if (!lyricIter.hasPrevious()) {//没有上一个，就向下找
            prev = lyricIter.next();
            if (!lyricIter.hasNext()) {//只有一个
                return prev;
            }
            next = lyricIter.next();
        } else {
            //两者都有的情况
            prev = lyricIter.previous();
            lyricIter.next();
            next = lyricIter.next();
        }
        //找到position所在的位置
        while (!(prev.getPosition() <= position && position < next.getPosition())) {
            if (!lyricIter.hasNext()) {
                return next;
            }
            if (!lyricIter.hasPrevious()) {
                return prev;
            }
            //向前查找
            if (position < prev.getPosition()) {
                next = prev;
                prev = lyricIter.previous();
                if (prev == next) {
                    prev = lyricIter.previous();
                }
            } else {//向后查找
                prev = next;
                next = lyricIter.next();
                if (prev == next) {
                    next = lyricIter.next();
                }
            }
        }
        return prev;
    }
}
