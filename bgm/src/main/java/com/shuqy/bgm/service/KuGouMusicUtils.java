package com.shuqy.bgm.service;

import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.zip.Inflater;

@Slf4j
public class KuGouMusicUtils {
    private static final String KUGOU_LYRIC_PATH;
    private final static char[] encrypt_key = {'@', 'G', 'a', 'w', '^', '2', 't', 'G', 'Q', '6', '1', '-', 'Î', 'Ò', 'n', 'i'};

    static {
        String tmpStr;
        try {
            String location = System.getenv("APPDATA") + "\\KuGou8\\KuGou.ini";
            String key = Files.readAllLines(Paths.get(location), Charset.forName("gb2312"))
                    .stream()
                    .filter(s -> s.startsWith("LyricPath="))
                    .findAny()
                    .orElseThrow();
            tmpStr = key.substring(10);
        } catch (IOException e) {
            log.error("Get KuGou ini file error at location: appdata");
            tmpStr = "E:\\KuGou\\Lyric";
        }
        KUGOU_LYRIC_PATH = tmpStr;
    }

    public static String getKugouLyricPath() {
        return KUGOU_LYRIC_PATH;
    }

    private static String currentTitle = "";
    private static List<String> currentLyric = Collections.emptyList();
    private static double currentDuration = 0.0;
    //读写锁
    private static final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    private KuGouMusicUtils() {
    }

    /*
     * 解压缩
     *
     * @param data 待压缩的数据
     * @return byte[] 解压缩后的数据
     */
    private static byte[] decompress(byte[] data) {
        byte[] output = new byte[0];

        Inflater decompresser = new Inflater();
        decompresser.reset();
        decompresser.setInput(data);

        ByteArrayOutputStream o = new ByteArrayOutputStream(data.length);
        try {
            byte[] buf = new byte[1024];
            while (!decompresser.finished()) {
                int i = decompresser.inflate(buf);
                o.write(buf, 0, i);
            }
            output = o.toByteArray();
        } catch (Exception e) {
            output = data;
            e.printStackTrace();
        } finally {
            try {
                o.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        decompresser.end();
        return output;
    }

    public static boolean parse(String title) {
        if (!title.equals(currentTitle)) {
            byte[] bytes;
            try {
                Path lyricPath = Files.list(Paths.get(KUGOU_LYRIC_PATH))
                        .filter(path -> path.getFileName().toString().startsWith(title))
                        .findAny()
                        .orElseThrow();
                bytes = Files.readAllBytes(lyricPath);
            } catch (Exception e) {
                log.error("Exception " + e.getMessage() + " at getting lyric file as title:" + title);
                return false;
            }
            log.info("Successfully get the " + title + " lyric file as krc version: " + new String(bytes, 0, 4));
            byte[] content = Arrays.copyOfRange(bytes, 4, bytes.length);
            for (int i = 0; i < content.length; i++) {
                content[i] = (byte) (content[i] ^ encrypt_key[i % encrypt_key.length]);
            }
            content = decompress(content);
            String contentString = new String(content);
            String[] contents = contentString.split("\r\n");
            //加写锁更新数据
            readWriteLock.writeLock().lock();
            try {
                currentTitle = title;
                currentLyric = new LinkedList<>();
                for (String s : contents) {
                    if (s.startsWith("[total")) {
                        currentDuration = Double.parseDouble(s.substring(7, s.indexOf(']'))) / 1000.0;
                    }
                    currentLyric.add(s);
                }
            } finally {
                //释放写锁
                readWriteLock.writeLock().unlock();
            }
        }
        return true;
    }

    public static String getCurrentTitle() {
        //加读锁
        readWriteLock.readLock().lock();
        try {
            return currentTitle;
        } finally {
            //释放读锁
            readWriteLock.readLock().unlock();
        }
    }

    public static List<String> getCurrentLyric() {
        //加读锁
        readWriteLock.readLock().lock();
        try {
            return currentLyric;
        } finally {
            //释放读锁
            readWriteLock.readLock().unlock();
        }
    }

    public static double getCurrentDuration() {
        //加读锁
        readWriteLock.readLock();
        try {
            return currentDuration;
        } finally {
            //释放读锁
            readWriteLock.readLock().unlock();
        }
    }

}
