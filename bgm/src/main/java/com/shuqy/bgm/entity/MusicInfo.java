package com.shuqy.bgm.entity;


import java.util.Objects;

public class MusicInfo {
    //标识符
    private String identifier;
    //时长
    private double duration;

    private static final MusicInfo EMPTY_INFO = new MusicInfo("", 0.0);

    public MusicInfo(String identifier, double duration) {
        this.identifier = identifier;
        this.duration = duration;
    }

    public MusicInfo() {
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public double getDuration() {
        return duration;
    }

    public void setDuration(double duration) {
        this.duration = duration;
    }

    @Override
    public String toString() {
        return "MusicInfo{" +
                "identifier='" + identifier + '\'' +
                ", duration=" + duration +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MusicInfo musicInfo = (MusicInfo) o;
        return Double.compare(musicInfo.duration, duration) == 0 &&
                identifier.equals(musicInfo.identifier);
    }

    @Override
    public int hashCode() {
        return Objects.hash(identifier, duration);
    }

    public static MusicInfo emptyInfo() {
        return EMPTY_INFO;
    }
}

