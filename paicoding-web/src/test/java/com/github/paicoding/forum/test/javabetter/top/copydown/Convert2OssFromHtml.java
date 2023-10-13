package com.github.paicoding.forum.test.javabetter.top.copydown;

import cn.hutool.core.io.file.FileReader;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;

/**
 * 微信搜索「沉默王二」，回复 Java
 *
 * @author 沉默王二
 * @date 5/31/22
 */
@Slf4j
public class Convert2OssFromHtml {
    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {
        // 如果是爬虫
        // 原创
        // 原创需要指定路径
        ImgOption imgOption = ImgOption.builder()
                .imgDownloadDestPrefix(Constants.DESTINATION + "images" )
                .imgOssFolder(Constants.ossFolder)
                .imgCdnPrefix(Constants.ossOrCdnUrls[0])
                .imgNamePrefix("other-conggslxmysqldswmysqljslt")
                .build();


        // 对整个文档里面的图片链接转链
        // 下载到本地，上传到 OSS，替换链接
        // 正则表达式，找到对应的图片
        String [] imgNamePrefixs = imgOption.getImgNamePrefix().split("-");
        String mdPath = Constants.DESTINATION + "docs" + Constants.fileSeparator
                + imgOption.getImgCategory() + Constants.fileSeparator
                + imgNamePrefixs[0] + Constants.fileSeparator
                + imgNamePrefixs[1] + ".md";

        File md = new File(mdPath);
        FileReader fileReader = FileReader.create(md, StandardCharsets.UTF_8);
        // 读取全部内容
        String content = OSSUtil.upload(fileReader.readString(), imgOption);

        FileWriter writer = new FileWriter(md);
        writer.write(content);
        writer.flush();
    }
}
