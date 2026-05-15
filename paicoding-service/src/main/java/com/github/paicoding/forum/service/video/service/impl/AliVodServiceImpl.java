package com.github.paicoding.forum.service.video.service.impl;

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.vod.model.v20170321.CreateUploadVideoRequest;
import com.aliyuncs.vod.model.v20170321.CreateUploadVideoResponse;
import com.aliyuncs.vod.model.v20170321.GetPlayInfoRequest;
import com.aliyuncs.vod.model.v20170321.GetPlayInfoResponse;
import com.aliyuncs.vod.model.v20170321.RefreshUploadVideoRequest;
import com.aliyuncs.vod.model.v20170321.RefreshUploadVideoResponse;
import com.github.paicoding.forum.api.model.exception.ExceptionUtil;
import com.github.paicoding.forum.api.model.vo.constants.StatusEnum;
import com.github.paicoding.forum.core.config.AliVodProperties;
import com.github.paicoding.forum.service.video.dto.VodUploadAuthDTO;
import com.github.paicoding.forum.service.video.service.VideoService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@Service
public class AliVodServiceImpl implements VideoService {
    @Resource
    private AliVodProperties properties;

    @Override
    public VodUploadAuthDTO createUploadAuth(String fileName, String title) {
        validateConfig();
        if (StringUtils.isBlank(fileName)) {
            throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "视频文件名不能为空");
        }

        try {
            CreateUploadVideoRequest request = new CreateUploadVideoRequest();
            request.setFileName(fileName);
            request.setTitle(StringUtils.defaultIfBlank(title, normalizeTitle(fileName)));
            if (StringUtils.isNotBlank(properties.getStorageLocation())) {
                request.setStorageLocation(properties.getStorageLocation());
            }
            if (StringUtils.isNumeric(properties.getCateId())) {
                request.setCateId(Long.valueOf(properties.getCateId()));
            }
            if (StringUtils.isNotBlank(properties.getTemplateGroupId())) {
                request.setTemplateGroupId(properties.getTemplateGroupId());
            }
            if (StringUtils.isNotBlank(properties.getWorkflowId())) {
                request.setWorkflowId(properties.getWorkflowId());
            }

            CreateUploadVideoResponse response = client().getAcsResponse(request);
            return buildUploadAuth(response.getVideoId(), response.getUploadAddress(), response.getUploadAuth(), response.getRequestId());
        } catch (Exception e) {
            log.error("create aliyun vod upload auth failed! fileName:{}", fileName, e);
            throw ExceptionUtil.of(StatusEnum.UNEXPECT_ERROR, "创建视频上传凭证失败");
        }
    }

    @Override
    public VodUploadAuthDTO refreshUploadAuth(String videoId) {
        validateConfig();
        if (StringUtils.isBlank(videoId)) {
            throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "videoId不能为空");
        }

        try {
            RefreshUploadVideoRequest request = new RefreshUploadVideoRequest();
            request.setVideoId(videoId);
            RefreshUploadVideoResponse response = client().getAcsResponse(request);
            return buildUploadAuth(response.getVideoId(), response.getUploadAddress(), response.getUploadAuth(), response.getRequestId());
        } catch (Exception e) {
            log.error("refresh aliyun vod upload auth failed! videoId:{}", videoId, e);
            throw ExceptionUtil.of(StatusEnum.UNEXPECT_ERROR, "刷新视频上传凭证失败");
        }
    }

    @Override
    public String queryPlayUrl(String videoId) {
        validateConfig();
        if (StringUtils.isBlank(videoId)) {
            throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "videoId不能为空");
        }

        try {
            GetPlayInfoRequest request = new GetPlayInfoRequest();
            request.setVideoId(videoId);
            GetPlayInfoResponse response = client().getAcsResponse(request);
            List<GetPlayInfoResponse.PlayInfo> playInfos = response.getPlayInfoList();
            if (playInfos == null || playInfos.isEmpty() || StringUtils.isBlank(playInfos.get(0).getPlayURL())) {
                throw ExceptionUtil.of(StatusEnum.RECORDS_NOT_EXISTS, "视频播放地址");
            }
            return playInfos.get(0).getPlayURL();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            log.error("query aliyun vod play url failed! videoId:{}", videoId, e);
            throw ExceptionUtil.of(StatusEnum.UNEXPECT_ERROR, "查询视频播放地址失败");
        }
    }

    private VodUploadAuthDTO buildUploadAuth(String videoId, String uploadAddress, String uploadAuth, String requestId) {
        VodUploadAuthDTO dto = new VodUploadAuthDTO();
        dto.setVideoId(videoId);
        dto.setUploadAddress(uploadAddress);
        dto.setUploadAuth(uploadAuth);
        dto.setRequestId(requestId);
        dto.setRegion(properties.getRegionId());
        dto.setUserId(properties.getUserId());
        return dto;
    }

    private IAcsClient client() {
        DefaultProfile profile = DefaultProfile.getProfile(properties.getRegionId(), properties.getAccessKeyId(), properties.getAccessKeySecret());
        return new DefaultAcsClient(profile);
    }

    private void validateConfig() {
        if (!Boolean.TRUE.equals(properties.getEnabled())) {
            throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "阿里云VOD未启用");
        }
        if (StringUtils.isAnyBlank(properties.getRegionId(), properties.getAccessKeyId(), properties.getAccessKeySecret())) {
            throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "阿里云VOD配置不完整");
        }
    }

    private String normalizeTitle(String fileName) {
        String name = StringUtils.substringAfterLast(fileName, "/");
        int dot = name.lastIndexOf('.');
        return dot > 0 ? name.substring(0, dot) : name;
    }
}
