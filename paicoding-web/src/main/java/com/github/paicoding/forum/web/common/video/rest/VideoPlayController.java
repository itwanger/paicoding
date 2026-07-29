package com.github.paicoding.forum.web.common.video.rest;

import com.github.paicoding.forum.service.video.service.VideoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@Slf4j
@RequestMapping(path = "video/")
@RestController
public class VideoPlayController {
    @Resource
    private VideoService videoService;

    @GetMapping(path = "play/redirect")
    public ResponseEntity<Void> redirect(@RequestParam("videoId") String videoId) {
        String playUrl;
        try {
            playUrl = videoService.queryPlayUrl(videoId);
        } catch (RuntimeException e) {
            // 取不到播放地址说明这个 videoId 已经不存在了，返回 404 而不是 500：
            // 爬虫会长期重试历史地址，5xx 会被记成站点健康问题
            log.warn("query play url failed, videoId:{}", videoId, e);
            return ResponseEntity.notFound().build();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.LOCATION, playUrl);
        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }
}
