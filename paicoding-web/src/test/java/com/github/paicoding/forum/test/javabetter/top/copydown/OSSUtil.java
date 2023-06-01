package com.github.paicoding.forum.test.javabetter.top.copydown;

import cn.hutool.core.lang.UUID;
import cn.hutool.http.HttpUtil;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.github.paicoding.forum.core.config.ImageProperties;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 微信搜索「沉默王二」，回复 Java
 *
 * @author 沉默王二
 * @date 5/31/22
 */
@Slf4j
public class OSSUtil {
    @Autowired
    @Setter
    @Getter
    private static ImageProperties properties;

    public static boolean needUploadOss(String imageUrl) {
        boolean flag = true;
        for (String url : Constants.ossOrCdnUrls) {
            if (imageUrl.indexOf(url) != -1) {
                flag = false;
                break;
            }
        }
        return flag;
    }

    public static String upload(String md, ImgOption imgOption) {
        Pattern p = Pattern.compile(Constants.mdImgPattern, Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(md);



        OSS ossClient = new OSSClientBuilder().build(properties.getOss().getEndpoint(), properties.getOss().getAk(), properties.getOss().getSk());

        while (m.find()) {
            // 图片描述
            imgOption.setImgDescription(m.group(1));
            // 图片路径
            imgOption.setImgOriginUrl(m.group(2));
            // 图片名
            imgOption.setImgName(imgOption.getImgNamePrefix() + "-" + UUID.fastUUID());
            log.info("图片参数{}", imgOption);

            // 需要处理的图片链接
            // 设置路径的时候设置过了
            if (imgOption.isNeedUploadOss()) {
                // imgName one-01
                // imgNameWithExt one-01.jpg
                // 先下载到本地
                // imgDest itwanger/Documents/GitHub/toBeBetterJavaer/images/nice-article/one-01.jpg

                String temp = imgOption.getImgOriginUrl();
                if (imgOption.getImgNamePrefix().indexOf(HtmlSourceType.segmentfault.getName()) != -1
                        && imgOption.getImgOriginUrl().indexOf(HtmlSourceType.segmentfault.getName()) == -1) {
                    temp = "https://segmentfault.com" + imgOption.getImgOriginUrl();
                    log.info("思否{}", temp);
                }

                if (imgOption.getImgNamePrefix().indexOf(HtmlSourceType.github.getName()) != -1
                        && imgOption.getImgOriginUrl().indexOf(HtmlSourceType.github.getName()) == -1) {
                    temp = "https://github.com" + imgOption.getImgOriginUrl();
                    log.info("GitHub{}", temp);
                }

                // 替换链接
                md = md.replace(imgOption.getImgOriginUrl(), imgOption.getImgCndUrl());

                File downloadImage = HttpUtil.downloadFileFromUrl(temp, imgOption.getImgDownloadDestComplete());

                // objectName nice-article/one-01.jpg
                // 目录+分类
                // ossFolder tobebetterjavaer/images/
                ossClient.putObject(Constants.bucketName, imgOption.getImgOssObjectName(), downloadImage);

            }
        }
        return md;
    }
}
