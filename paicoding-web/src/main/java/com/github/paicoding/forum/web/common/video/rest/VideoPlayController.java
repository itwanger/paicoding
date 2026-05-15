package com.github.paicoding.forum.web.common.video.rest;

import com.github.paicoding.forum.service.video.service.VideoService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

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

    @GetMapping(path = "play/proxy")
    public void proxy(@RequestParam("videoId") String videoId, HttpServletRequest request, HttpServletResponse response) throws IOException {
        String playUrl = videoService.queryPlayUrl(videoId);
        HttpURLConnection connection = (HttpURLConnection) new URL(playUrl).openConnection();
        connection.setInstanceFollowRedirects(true);
        String userAgent = request.getHeader("User-Agent");
        if (StringUtils.isNotBlank(userAgent)) {
            connection.setRequestProperty("User-Agent", userAgent);
        }
        String range = request.getHeader(HttpHeaders.RANGE);
        if (StringUtils.isNotBlank(range)) {
            connection.setRequestProperty(HttpHeaders.RANGE, range);
        }

        try {
            int status = connection.getResponseCode();
            if (status == HttpServletResponse.SC_PARTIAL_CONTENT) {
                response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
            } else {
                response.setStatus(HttpServletResponse.SC_OK);
            }

            copyHeader(connection, response, HttpHeaders.CONTENT_TYPE);
            copyHeader(connection, response, HttpHeaders.CONTENT_LENGTH);
            copyHeader(connection, response, HttpHeaders.CONTENT_RANGE);
            response.setHeader(HttpHeaders.ACCEPT_RANGES, "bytes");
            response.setHeader(HttpHeaders.CACHE_CONTROL, "public, max-age=300");

            StreamUtils.copy(connection.getInputStream(), response.getOutputStream());
        } finally {
            connection.disconnect();
        }
    }

    private void copyHeader(HttpURLConnection connection, HttpServletResponse response, String headerName) {
        String value = connection.getHeaderField(headerName);
        if (StringUtils.isNotBlank(value)) {
            response.setHeader(headerName, value);
        }
    }
}
