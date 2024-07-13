package com.github.paicoding.forum.service.image.oss.impl;

import com.github.hui.quick.plugin.base.file.FileWriteUtil;
import com.github.paicoding.forum.api.model.exception.ExceptionUtil;
import com.github.paicoding.forum.api.model.vo.constants.StatusEnum;
import com.github.paicoding.forum.core.config.ImageProperties;
import com.github.paicoding.forum.core.util.StopWatchUtil;
import com.github.paicoding.forum.service.image.oss.ImageUploader;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

/**
 * 本地保存上传文件
 *
 * @author XuYifei
 * @date 2024-07-12
 */
@Slf4j
@ConditionalOnExpression(value = "#{'local'.equals(environment.getProperty('image.oss.type'))}")
@Component
public class LocalStorageWrapper implements ImageUploader {
    @Autowired
    private ImageProperties imageProperties;
    private Random random;

    public LocalStorageWrapper() {
        random = new Random();
    }

    @Override
    public String upload(InputStream input, String fileType) {
        // 记录耗时分布
        StopWatchUtil stopWatchUtil = StopWatchUtil.init("图片上传");
        try {
            if (fileType == null) {
                // 根据魔数判断文件类型
                InputStream finalInput = input;
                byte[] bytes = stopWatchUtil.record("流转字节", () -> StreamUtils.copyToByteArray(finalInput));
                input = new ByteArrayInputStream(bytes);
                fileType = getFileType((ByteArrayInputStream) input, fileType);
            }

            String path = imageProperties.getAbsTmpPath() + imageProperties.getWebImgPath();
            String fileName = genTmpFileName();

            InputStream finalInput = input;
            String finalFileType = fileType;
            FileWriteUtil.FileInfo file = stopWatchUtil.record("存储", () -> FileWriteUtil.saveFileByStream(finalInput, path, fileName, finalFileType));
            return imageProperties.buildImgUrl(imageProperties.getWebImgPath() + file.getFilename() + "." + file.getFileType());
        } catch (Exception e) {
            log.error("Parse img from httpRequest to BufferedImage error! e:", e);
            throw ExceptionUtil.of(StatusEnum.UPLOAD_PIC_FAILED);
        } finally {
            log.info("图片上传耗时: {}", stopWatchUtil.prettyPrint());
        }
    }

    /**
     * 获取文件临时名称
     *
     * @return
     */
    private String genTmpFileName() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddhhmmssSSS")) + "_" + random.nextInt(100);
    }

    /**
     * 外网图片转存判定，对于没有转存过的，且是http开头的网络图片时，才需要进行转存
     *
     * @param img
     * @return true 表示不需要转存
     */
    @Override
    public boolean uploadIgnore(String img) {
        if (StringUtils.isNotBlank(imageProperties.getCdnHost()) && img.startsWith(imageProperties.getCdnHost())) {
            return true;
        }

        // 如果是oss的图片，也不需要转存
        if (StringUtils.isNotBlank(imageProperties.getOss().getHost()) && img.startsWith(imageProperties.getOss().getHost())) {
            return true;
        }

        return !img.startsWith("http");
    }
}
