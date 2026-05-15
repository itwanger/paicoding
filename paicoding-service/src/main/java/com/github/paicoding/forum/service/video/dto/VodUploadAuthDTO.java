package com.github.paicoding.forum.service.video.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class VodUploadAuthDTO implements Serializable {
    private static final long serialVersionUID = -8478737632846945146L;

    private String videoId;
    private String uploadAddress;
    private String uploadAuth;
    private String requestId;
    private String region;
    private String userId;
}
