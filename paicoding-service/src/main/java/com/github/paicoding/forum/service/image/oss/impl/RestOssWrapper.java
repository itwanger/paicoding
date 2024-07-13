package com.github.paicoding.forum.service.image.oss.impl;

import com.github.paicoding.forum.core.config.ImageProperties;
import com.github.paicoding.forum.core.net.HttpRequestHelper;
import com.github.paicoding.forum.core.util.JsonUtil;
import com.github.paicoding.forum.core.util.StopWatchUtil;
import com.github.paicoding.forum.service.image.oss.ImageUploader;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * 基于http的文件上传
 * @author XuYifei
 * @date 2024-07-12
 */
@Slf4j
@Component
@ConditionalOnExpression(value = "#{'rest'.equals(environment.getProperty('image.oss.type'))}")
public class RestOssWrapper implements ImageUploader {
    @Autowired
    private ImageProperties properties;

    @Override
    public String upload(InputStream input, String fileType) {
        StopWatchUtil stopWatchUtil = StopWatchUtil.init("图片上传");
        try {
            byte[] bytes = stopWatchUtil.record("转字节", () -> StreamUtils.copyToByteArray(input));
            String res = stopWatchUtil.record("上传", () -> HttpRequestHelper.upload(properties.getOss().getEndpoint(), "image", "img." + fileType, bytes));
            HashMap<?, ?> map = JsonUtil.toObj(res, HashMap.class);
            return (String) ((Map<?, ?>) map.get("result")).get("imagePath");
        } catch (Exception e) {
            log.error("upload image error response! uri:{}", properties.getOss().getEndpoint(), e);
            return null;
        } finally {
            if (log.isDebugEnabled()) {
                log.debug("upload Image cost: {}", stopWatchUtil.prettyPrint());
            }
        }
    }

    @Override
    public boolean uploadIgnore(String fileUrl) {
        if (StringUtils.isNotBlank(properties.getOss().getHost()) && fileUrl.startsWith(properties.getOss().getHost())) {
            return true;
        }
        return !fileUrl.startsWith("http");
    }
}
