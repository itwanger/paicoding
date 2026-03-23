package com.github.paicoding.forum.web.javabetter.top.copydown;

import cn.hutool.core.lang.UUID;
import cn.hutool.http.HttpUtil;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.github.paicoding.forum.core.config.ImageProperties;
import com.github.paicoding.forum.core.config.OssProperties;
import com.github.paicoding.forum.service.image.oss.impl.AliOssWrapper;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
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

    private static AliOssWrapper initOss() throws Exception {
        // Endpoint以华东1（杭州）为例，其它Region请按实际情况填写。
        String endpoint = requireEnv("PAICODING_OSS_ENDPOINT");
        // 阿里云账号AccessKey拥有所有API的访问权限，风险很高。强烈建议您创建并使用RAM用户进行API访问或日常运维，请登录RAM控制台创建RAM用户。
        String accessKeyId = requireEnv("PAICODING_OSS_AK");
        String accessKeySecret = requireEnv("PAICODING_OSS_SK");
        String bucketName = requireEnv("PAICODING_OSS_BUCKET");
        String host = requireEnv("PAICODING_OSS_HOST");

        AliOssWrapper aliOss = new AliOssWrapper();
        ImageProperties properties = new ImageProperties();
        OssProperties oss = new OssProperties();
        oss.setAk(accessKeyId);
        oss.setSk(accessKeySecret);
        oss.setBucket(bucketName);
        oss.setEndpoint(endpoint);
        oss.setBucket(bucketName);
        oss.setHost(host);
        oss.setPrefix("javabetter/");
        properties.setOss(oss);

        aliOss.setProperties(properties);
        return aliOss;
    }

    private static String requireEnv(String name) {
        String value = System.getenv(name);
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalStateException("Missing required environment variable: " + name);
        }
        return value;
    }

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

    public static String upload(String md, ImgOption imgOption) throws Exception {
        Pattern p = Pattern.compile(Constants.mdImgPattern, Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(md);

        OssProperties properties = initOss().getProperties().getOss();

        OSS ossClient = new OSSClientBuilder().build(properties.getEndpoint(), properties.getAk(), properties.getSk());

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

//                File downloadImage = HttpUtil.downloadFileFromUrl(temp, imgOption.getImgDownloadDestComplete());

                // objectName nice-article/one-01.jpg
                // 目录+分类
                // ossFolder tobebetterjavaer/images/
                // 直接通过 URL 上传到 OSS，不用下载到本地
                URL imageUrl = new URL(temp); // imgOption 是你的图片选项对象
                InputStream inputStream = imageUrl.openStream();
                ossClient.putObject(Constants.bucketName, imgOption.getImgOssObjectName(), inputStream);

            }
        }
        return md;
    }
}
