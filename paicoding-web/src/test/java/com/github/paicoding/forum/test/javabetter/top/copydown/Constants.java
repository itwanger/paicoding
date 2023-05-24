package com.github.paicoding.forum.test.javabetter.top.copydown;

/**
 * 微信搜索「沉默王二」，回复 Java
 *
 * @author 沉默王二
 * @date 5/28/22
 */
public class Constants {
    // 分隔符
    public static final String fileSeparator = System.getProperty("file.separator");
    // 文章目录
    // 图片目录
    public static final String destination = System.getProperty("user.home")
            +fileSeparator+"Documents" +fileSeparator+
            "GitHub" +fileSeparator+
            "toBeBetterJavaer" + fileSeparator;
    public static final String html2mdCategory = "nice-article";

    // Endpoint以华东1（杭州）为例，其它Region请按实际情况填写。
    public static final String endpoint = "oss-cn-beijing.aliyuncs.com";
    // 阿里云账号AccessKey拥有所有API的访问权限，风险很高。强烈建议您创建并使用RAM用户进行API访问或日常运维，请登录RAM控制台创建RAM用户。
    public static final String accessKeyId = "LTAI5tFgj8xZA6siqiJ3vdRk";
    public static final String accessKeySecret = "yT2gGIdUBCvDkgQenf9L7MYi2LFTOO";
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
}
