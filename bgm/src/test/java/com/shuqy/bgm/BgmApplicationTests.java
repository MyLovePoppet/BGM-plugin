package com.shuqy.bgm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shuqy.bgm.entity.MusicInfo;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.Inflater;

@SpringBootTest
class BgmApplicationTests {

    @Test
    void contextLoads() {

    }

    //CloudMusicLyric
    /*
    //C:\Users\shuqy\AppData\Local\Netease\CloudMusic\webdata\file
    @Test
    public void testGetCurrentMusicID() {
        String historyPath = System.getenv("LOCALAPPDATA") + "\\Netease\\CloudMusic\\webdata\\file\\history";
        System.out.println(historyPath);
        boolean isIdOK = false, isNameOK = false;
        String id = "";
        String name = "";
        try {
            JsonReader jsonReader = gson.newJsonReader(Files.newBufferedReader(Paths.get(System.getenv("LOCALAPPDATA"), "Netease\\CloudMusic\\webdata\\file\\history")));
            jsonReader.beginArray();
            jsonReader.beginObject();
            String jsonName = jsonReader.nextName();
            if (jsonName.equals("track")) {
                jsonReader.beginObject();
                while (jsonReader.hasNext()) {
                    jsonName = jsonReader.nextName();
                    if (jsonName.equals("id")) {
                        id = jsonReader.nextString();
                        isIdOK = true;
                    } else if (jsonName.equals("name")) {
                        name = jsonReader.nextString();
                        isNameOK = true;
                    } else {
                        jsonReader.skipValue();
                    }
                    if (isIdOK && isNameOK) {
                        break;
                    }
                }
            }

            jsonReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(id + "\t" + name);
    }

    //https://music.163.com/api/song/lyric?id=1445556953&lv=1&kv=1&tv=-1
    @Test
    public void generateLyricByMusicID() {
        String id = "1445556953";
        String url = MessageFormat.format("https://music.163.com/api/song/lyric?id={0}&lv=1&kv=1&tv=-1", id);
        HttpClient httpClient = HttpClient.newBuilder().build();
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .build();
        try {
            HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            CloudMusicJsonLyric jsonLyric = gson.fromJson(httpResponse.body(), CloudMusicJsonLyric.class);
            String[] lyricStr = jsonLyric.getLrc().getLyric().split("\n");
            List<Lyric> lyricList = new LinkedList<>();
            for (String s : lyricStr) {
                lyricList.add(Lyric.format(s));
            }
            System.out.println(lyricList);
            System.out.println(getCurrentPositionLyric(lyricList.listIterator(), 166));
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

    }

    public String getCurrentPositionLyric(ListIterator<Lyric> iterator, double position) {
        if (!iterator.hasNext() && !iterator.hasPrevious()) {
            return "";
        }
        if (!iterator.hasNext()) {
            return iterator.previous().getText();
        }
        Lyric prev = iterator.next();
        Lyric next = iterator.next();
        while (!(prev.getPosition() <= position && position < next.getPosition())) {
            if (!iterator.hasNext()) {
                return next.getText();
            }
            if (!iterator.hasPrevious()) {
                return prev.getText();
            }
            if (position < prev.getPosition()) {
                next = prev;
                iterator.previous();
                prev = iterator.previous();
            } else {
                prev = next;
                next = iterator.next();
            }
        }
        return prev.getText();
    }
     */
    @Test
    void testQQMusicLyric() {
        String title = "来生缘 - 刘德华";
        title = URLEncoder.encode(title, StandardCharsets.UTF_8);
        String infoURL = "https://c.y.qq.com/soso/fcgi-bin/client_search_cp?p=1&n=5&w={0}&format=json";
        ObjectMapper objectMapper = new ObjectMapper();
        HttpClient httpClient = HttpClient.newBuilder().build();
        HttpRequest infoRequest = HttpRequest.newBuilder()
                .uri(URI.create(MessageFormat.format(infoURL, title)))
                .build();

        try {
            HttpResponse<String> httpResponse = httpClient.send(infoRequest, HttpResponse.BodyHandlers.ofString());
            System.out.println(httpResponse.body());
            JsonNode rootNode = objectMapper.readTree(httpResponse.body());
            JsonNode songList = rootNode.path("data").path("song").path("list");
            JsonNode song = songList.path(0);
            double duration = song.path("interval").asDouble(0.0);
            String identifier = song.path("songmid").asText("");
            MusicInfo musicInfo = new MusicInfo(identifier, duration);
            System.out.println(musicInfo);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        String lyricURL = "https://c.y.qq.com/lyric/fcgi-bin/fcg_query_lyric_new.fcg?songmid={0}&format=json&nobase64=1";
        String songmid = "002WFWqf1cLI08";
        HttpRequest lyricRequest = HttpRequest.newBuilder()
                .uri(URI.create(MessageFormat.format(lyricURL, songmid)))
                .headers("Referer", "https://y.qq.com/portal/player.html")
                .build();
        try {
            HttpResponse<String> httpResponse = httpClient.send(lyricRequest, HttpResponse.BodyHandlers.ofString());
            System.out.println(httpResponse.body());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

    }

    /**
     * 解压缩
     *
     * @param data 待压缩的数据
     * @return byte[] 解压缩后的数据
     */
    public static byte[] decompress(byte[] data) {
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

    @Test
    void testKuGouMusicLyric() {
        try {
            byte[] bytes = Files.readAllBytes(Paths.get("E:\\KuGou\\Lyric", "谭艳 - 光明-31737d4db6e2de1d80cd260695d23743-47370898-00000000.krc"));
            char[] encrypt_key = {'@', 'G', 'a', 'w', '^', '2', 't', 'G', 'Q', '6', '1', '-', 'Î', 'Ò', 'n', 'i'};
            //version
            byte[] content = Arrays.copyOfRange(bytes, 4, bytes.length);
            for (int i = 0; i < content.length; i++) {
                content[i] = (byte) (content[i] ^ encrypt_key[i % encrypt_key.length]);
            }
            content = decompress(content);
            String contentString = new String(content);

            double total = 0.0;
            String[] contents = contentString.split("\n");
            for (String s : contents) {
                if (s.startsWith("[total")) {
                    total = Double.parseDouble(s.substring(7, s.indexOf(']')));
                    break;
                }
            }

            System.out.println(total);
            Pattern pattern = Pattern.compile("\\[\\d+,\\d+]");
            for (String s : contents) {
                Matcher matcher = pattern.matcher(s);
                if (matcher.find()) {
                    s = s.replaceAll("<\\d+,\\d+,\\d+>", "");
                    System.out.println(s);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
