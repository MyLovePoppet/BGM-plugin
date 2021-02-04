package com.shuqy.bgm.entity;

import java.util.Objects;

public class Lyric {
    private double position;
    private String text;
    private static final Lyric EMPTY_LYRIC = new Lyric(0.0, "");

    public Lyric(double position, String text) {
        this.position = position;
        this.text = text;
    }

    public double getPosition() {
        return position;
    }

    public void setPosition(double position) {
        this.position = position;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return "Lyric{" +
                "position=" + position +
                ", text='" + text + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Lyric lyric = (Lyric) o;
        return Double.compare(lyric.position, position) == 0 &&
                text.equals(lyric.text);
    }

    @Override
    public int hashCode() {
        return Objects.hash(position, text);
    }
    /**
     * 默认的空的歌词显示
     *
     * @return 默认的空的歌词
     */
    public static Lyric emptyLyric() {
        return EMPTY_LYRIC;
    }
}

