package com.github.liueyueyi.forum.api.model.vo.article.dto;

import lombok.Data;

/**
 * @author YiHui
 * @date 2022/9/14
 */
@Data
public class ColumnDTO {

    /**
     * 专栏id
     */
    private Long columnId;

    /**
     * 专栏名
     */
    private String column;

    /**
     * 说明
     */
    private String desc;

    /**
     * 封面
     */
    private String cover;

    /**
     * 发布时间
     */
    private Long publishTime;

    /**
     * 作者
     */
    private Long author;

    /**
     * 作者名
     */
    private Long authorName;

    /**
     * 作者头像
     */
    private String authorAvatar;
}
