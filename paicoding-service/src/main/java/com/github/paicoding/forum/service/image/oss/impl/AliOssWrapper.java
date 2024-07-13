package com.github.paicoding.forum.service.image.oss.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.model.PutObjectRequest;
import com.aliyun.oss.model.PutObjectResult;
import com.github.paicoding.forum.core.autoconf.DynamicConfigContainer;
import com.github.paicoding.forum.core.config.ImageProperties;
import com.github.paicoding.forum.core.util.Md5Util;
import com.github.paicoding.forum.core.util.StopWatchUtil;
import com.github.paicoding.forum.service.image.oss.ImageUploader;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * 阿里云oss文件上传
 *
 * @author XuYifei
 * @date 2024-07-12
 */
@Slf4j
@ConditionalOnExpression(value = "#{'ali'.equals(environment.getProperty('image.oss.type'))}")
@Component
public class AliOssWrapper implements ImageUploader, InitializingBean, DisposableBean {
    private static final int SUCCESS_CODE = 200;
    @Autowired
    @Setter
    @Getter
    private ImageProperties properties;
    private OSS ossClient;

    @Autowired
    private DynamicConfigContainer dynamicConfigContainer;

    @Override
    public String upload(InputStream input, String fileType) {
        try {
            // 创建PutObjectRequest对象。
            byte[] bytes = StreamUtils.copyToByteArray(input);
            return upload(bytes, fileType);
        } catch (OSSException oe) {
            log.error("Oss rejected with an error response! msg:{}, code:{}, reqId:{}, host:{}", oe.getErrorMessage(), oe.getErrorCode(), oe.getRequestId(), oe.getHostId());
            return "";
        } catch (Exception ce) {
            log.error("Caught an ClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with OSS, "
                    + "such as not being able to access the network. {}", ce.getMessage());
            return "";
        }
    }

    public String upload(byte[] bytes, String fileType) {
        StopWatchUtil stopWatchUtil = StopWatchUtil.init("图片上传");
        try {
            // 计算md5作为文件名，避免重复上传
            String fileName = stopWatchUtil.record("md5计算", () -> Md5Util.encode(bytes));
            ByteArrayInputStream input = new ByteArrayInputStream(bytes);
            fileName = properties.getOss().getPrefix() + fileName + "." + getFileType(input, fileType);
            // 创建PutObjectRequest对象。
            PutObjectRequest putObjectRequest = new PutObjectRequest(properties.getOss().getBucket(), fileName, input);
            // 设置该属性可以返回response。如果不设置，则返回的response为空。
            putObjectRequest.setProcess("true");

            // 上传文件
            PutObjectResult result = stopWatchUtil.record("文件上传", () -> ossClient.putObject(putObjectRequest));
            if (SUCCESS_CODE == result.getResponse().getStatusCode()) {
                return properties.getOss().getHost() + fileName;
            } else {
                log.error("upload to oss error! response:{}", result.getResponse().getStatusCode());
                // Guava 不允许回传 null
                return "";
            }
        } catch (OSSException oe) {
            log.error("Oss rejected with an error response! msg:{}, code:{}, reqId:{}, host:{}", oe.getErrorMessage(), oe.getErrorCode(), oe.getRequestId(), oe.getHostId());
            return  "";
        } catch (Exception ce) {
            log.error("Caught an ClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with OSS, "
                    + "such as not being able to access the network. {}", ce.getMessage());
            return  "";
        } finally {
            if (log.isDebugEnabled()) {
                log.debug("upload image size:{} cost: {}", bytes.length, stopWatchUtil.prettyPrint());
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

    @Override
    public void destroy() {
        if (ossClient != null) {
            ossClient.shutdown();
        }
    }

    private void init() {
        // 创建OSSClient实例。
        log.info("init ossClient");
        ossClient = new OSSClientBuilder().build(properties.getOss().getEndpoint(), properties.getOss().getAk(), properties.getOss().getSk());
    }

    @Override
    public void afterPropertiesSet() {
        init();
        // 监听配置变更，然后重新初始化OSSClient实例
        dynamicConfigContainer.registerRefreshCallback(properties, () -> {
            init();
            log.info("ossClient refreshed!");
        });
    }
}
