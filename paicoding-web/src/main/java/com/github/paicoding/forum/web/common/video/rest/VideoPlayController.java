package com.github.paicoding.forum.web.common.video.rest;

import com.github.paicoding.forum.service.video.service.VideoService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RequestMapping(path = "video/")
@RestController
public class VideoPlayController {
    @Resource
    private VideoService videoService;

    @GetMapping(path = "play/redirect")
    public ResponseEntity<Void> redirect(@RequestParam("videoId") String videoId) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.LOCATION, videoService.queryPlayUrl(videoId));
        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }
}
