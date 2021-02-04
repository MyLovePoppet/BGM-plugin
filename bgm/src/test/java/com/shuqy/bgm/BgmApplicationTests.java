package com.shuqy.bgm;

import com.shuqy.bgm.service.BGMService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class BgmApplicationTests {
    @Autowired
    BGMService bgmService;

    @Test
    void contextLoads() {
        bgmService.initService();
    }

}
