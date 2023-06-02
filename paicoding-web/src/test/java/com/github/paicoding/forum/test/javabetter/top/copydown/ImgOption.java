package com.github.paicoding.forum.test.javabetter.top.copydown;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.Tolerate;

/**
 * 微信搜索「沉默王二」，回复 Java
 *
 * @author 沉默王二
 * @date 5/31/22
 */
@Data
@Builder
public class ImgOption {
    // imgName one-01
    // imgNameWithExt one-01.jpg
    // imgDest itwanger/Documents/GitHub/toBeBetterJavaer/images/nice-article/
    // objectName nice-article/one-01.jpg
    // ossFolder tobebetterjavaer/images/

    // 图片的源链接 https://img2018.cnblogs.com/blog/31085/201906/31085-20190607182832666-1215142380.png
    private String imgOriginUrl;
    // 图片名前缀 文件名
    private String imgNamePrefix;
    // 图片名 one-01
    private String imgName;
    // 图片后缀 .jpg
    private String imgExt;
    // 图片的分类 nice-article/
    private String imgCategory;
    // 图片保存到本地的路径前缀 itwanger/Documents/GitHub/toBeBetterJavaer/images/
    private String imgDownloadDestPrefix;

    // 图片在 OSS 中的目录 tobebetterjavaer/images/
    private String imgOssFolder;
    // 图片的描述 ![xxxx]()
    private String imgDescription;
    // 是否需要上传到 OSS
    private boolean needUploadOss;
    // CDN pre
    private String imgCdnPrefix;

    @Tolerate
    public ImgOption() {}

    /**
     * @return itwanger/Documents/GitHub/toBeBetterJavaer/images/nice-article/one-01.jpg
     */
    public String getImgDownloadDestComplete() {
        return this.getImgDownloadDestPrefix() + this.getImgCategory() + Constants.fileSeparator + this.getImgName() + this.getImgExt();
    }

    /**
     * @return itwanger/Documents/GitHub/toBeBetterJavaer/images/nice-article/
     */
    public String getImgDownloadCategoryComplete() {
        return this.getImgDownloadDestPrefix() + this.getImgCategory();
    }

    public void setImgOriginUrl(String imgOriginUrl) {
        this.imgOriginUrl = imgOriginUrl;

        // 是否需要上传到 OSS
        this.setNeedUploadOss(OSSUtil.needUploadOss(this.getImgOriginUrl()));

        // 后缀
        this.setImgExt(ImageUtil.getImgExt(this.getImgOriginUrl()));
    }

    // 图片在 OSS 中的 name nice-article/one-01.jpg
    public String getImgOssObjectName() {
        return this.getImgOssFolder() + this.getImgCategory() + "/"+ this.getImgName() + this.getImgExt();
    }

    // 图片的 CDN 链接 http://cdn.tobebetterjavaer.com/tobebetterjavaer/images/overview/one-01.png
    public String getImgCndUrl() {
        return this.getImgCdnPrefix() + this.getImgCategory() +"/" + this.getImgName() + this.getImgExt();
    }
}
