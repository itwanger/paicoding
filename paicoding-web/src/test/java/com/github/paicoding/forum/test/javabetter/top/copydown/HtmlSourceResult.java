package com.github.paicoding.forum.test.javabetter.top.copydown;

import lombok.Builder;
import lombok.Data;

/**
 * 微信搜索「沉默王二」，回复 Java
 *
 * @author 沉默王二
 * @date 5/27/22
 */
@Data
@Builder
public class HtmlSourceResult {

    // 封面图路径
    private String cover;
    // 标题
    private String title;
    // 作者名
    private String author;
    // 原文链接
    private String sourceLink;
    // MD 内容
    private String markdown;
    // keywords
    private String keywords;
    // description
    private String description;
    private HtmlSourceType htmlSourceType;
    // 文件目录
    private String fileDir;
    // 图片目录
    private String imgDest;
}
