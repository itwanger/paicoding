package com.github.paicoding.forum.test.javabetter.top.copydown;

import java.nio.file.Paths;

/**
 * 微信搜索「沉默王二」，回复 Java
 *
 * @author 沉默王二
 * @date 5/28/22
 */
public class Constants {
    // 文章目录
    // 图片目录
    public static final String DESTINATION = Paths.get(System.getProperty("user.home"),
            "Documents", "GitHub", "toBeBetterJavaer").toString();
    // 默认作者名
    public static final String DEFAULT_AUTHOR = "佚名";
    // 填写Bucket名称，例如examplebucket。
    public static final String bucketName = "itwanger-oss";
    // OSS 的前缀文件夹
    public static final String ossFolder = "tobebetterjavaer/images/";

    // 不需要转链的路径
    public final static String ossOrCdnUrls [] = {
            "http://cdn.tobebetterjavaer.com/tobebetterjavaer/images/",
            "https://cdn.tobebetterjavaer.com/tobebetterjavaer/images/",
            "https://itwanger-oss.oss-cn-beijing.aliyuncs.com/tobebetterjavaer/images/" };

    // 匹配图片的 markdown 语法
    // ![](hhhx.png)
    // ![xx](hhhx.png?ax)
    public static final String mdImgPattern = "\\!\\[(.*)\\]\\((.*)\\)";

    // 图片后缀
    public static final String[] imgExtension = {".jpg", ".jpeg", ".png", ".gif"};
    public static final String fileSeparator = System.getProperty("file.separator");
}
