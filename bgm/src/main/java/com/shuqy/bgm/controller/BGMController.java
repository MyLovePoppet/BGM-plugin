package com.shuqy.bgm.controller;

import com.shuqy.bgm.entity.BGMInfo;
import com.shuqy.bgm.service.BGMService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BGMController {
    private final BGMService bgmService;

    public BGMController(BGMService bgmService) {
        this.bgmService = bgmService;
        bgmService.initService();
    }

    @RequestMapping("/BGM")
    public BGMInfo getCurrentBGMInfo() {
        return bgmService.getCurrentBGMInfo();
    }
}
