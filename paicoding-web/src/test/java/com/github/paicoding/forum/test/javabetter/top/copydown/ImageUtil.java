package com.github.paicoding.forum.test.javabetter.top.copydown;

/**
 * 微信搜索「沉默王二」，回复 Java
 *
 * @author 沉默王二
 * @date 5/31/22
 */
public class ImageUtil {
    public static String getImgExt(String url) {
        for (String extItem : Constants.imgExtension) {
            if (url.indexOf(extItem) != -1) {
                return extItem;
            }
        }
        return Constants.imgExtension[0];
    }
}
