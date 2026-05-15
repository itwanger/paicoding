package com.github.paicoding.forum.web.common.video.rest;

import com.github.paicoding.forum.api.model.vo.ResVo;
import com.github.paicoding.forum.core.permission.Permission;
import com.github.paicoding.forum.core.permission.UserRole;
import com.github.paicoding.forum.service.video.dto.VodUploadAuthDTO;
import com.github.paicoding.forum.service.video.service.VideoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@Permission(role = UserRole.LOGIN)
@RequestMapping(path = {"video/", "admin/video/", "api/admin/video/"})
@RestController
public class VideoRestController {
    @Resource
    private VideoService videoService;

    @PostMapping(path = "upload/auth")
    public ResVo<VodUploadAuthDTO> createUploadAuth(@RequestParam("fileName") String fileName,
                                                    @RequestParam(value = "title", required = false) String title) {
        return ResVo.ok(videoService.createUploadAuth(fileName, title));
    }

    @PostMapping(path = "upload/refresh")
    public ResVo<VodUploadAuthDTO> refreshUploadAuth(@RequestParam("videoId") String videoId) {
        return ResVo.ok(videoService.refreshUploadAuth(videoId));
    }

    @GetMapping(path = "play/url")
    public ResVo<String> playUrl(@RequestParam("videoId") String videoId) {
        return ResVo.ok(videoService.queryPlayUrl(videoId));
    }
}
