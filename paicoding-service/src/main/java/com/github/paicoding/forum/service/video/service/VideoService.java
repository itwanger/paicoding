package com.github.paicoding.forum.service.video.service;

import com.github.paicoding.forum.service.video.dto.VodUploadAuthDTO;

public interface VideoService {
    VodUploadAuthDTO createUploadAuth(String fileName, String title);

    VodUploadAuthDTO refreshUploadAuth(String videoId);

    String queryPlayUrl(String videoId);
}
