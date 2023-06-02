package com.github.paicoding.forum.test.javabetter.top.image;

import cn.hutool.core.io.file.FileReader;
import com.github.paicoding.forum.test.javabetter.top.copydown.Constants;
import com.github.paicoding.forum.test.javabetter.top.copydown.ImgOption;
import com.github.paicoding.forum.test.javabetter.top.copydown.OSSUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.concurrent.ExecutionException;

/**
 * 微信搜索「沉默王二」，回复 Java
 *
 * @author 沉默王二
 * @date 5/5/22
 */
@Slf4j
public class Convert2OSS {

    // 网络上的图片
    // 下载到本地一份（备份）
    // 上传到 OSS 一份（CDN）
    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {
        // 如果是爬虫
        // 原创
        // 原创需要指定路径

        final String category = "cityselect";
        final String filename = "wuhan";

        ImgOption imgOption = ImgOption.builder()
                // 本地的都用分隔符
                .imgDownloadDestPrefix(Paths.get(Constants.DESTINATION ,"images").toString())
                .imgOssFolder(Constants.ossFolder)
                .imgCdnPrefix(Constants.ossOrCdnUrls[0])
                .imgCategory(category)
                .imgNamePrefix(filename)
                .build();

            // 对整个文档里面的图片链接转链
            // 下载到本地，上传到 OSS，替换链接
            // 正则表达式，找到对应的图片
            String mdPath = Paths.get(Constants.DESTINATION,"docs" ,imgOption.getImgCategory()).toString()+ filename + ".md";
            File md = new File(mdPath);
            FileReader fileReader = FileReader.create(md, StandardCharsets.UTF_8);
            // 读取全部内容
            String content = OSSUtil.upload(fileReader.readString(), imgOption);

            FileWriter writer = new FileWriter(md);
            writer.write(content);
            writer.flush();


    }
}
