package com.github.paicoding.forum.api.model.vo.article.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.github.paicoding.forum.api.model.util.cdn.CdnImgSerializer;
import com.github.paicoding.forum.api.model.util.cdn.CdnUtil;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * 文章推荐
 *
 * @author YiHui
 * @date 2022/9/6
 */
@Data
@Accessors(chain = true)
public class ColumnArticleDTO implements Serializable {
    private static final long serialVersionUID = 3646376715620165839L;

    /**
     * 唯一ID
     */
    private Long id;

    /**
     * 文章ID
     */
    private Long articleId;

    /**
     * 文章标题
     */
    private String title;

    /**
     * 文章URL标识
     */
    private String urlSlug;

    /**
     * 阅读模式
     *
     * @see com.github.paicoding.forum.api.model.enums.column.ColumnArticleReadEnum#getRead()
     */
    private Integer readType;

    /**
     * 未解锁试看比例；0 表示使用全局试看字数配置
     */
    private Integer previewPercent;

    /**
     * 教程名称
     */
    private String shortTitle;

    /**
     * 教程ID
     */
    private Long columnId;

    /**
     * 教程标题
     */
    private String column;

    /**
     * 分组id
     */
    private Long groupId;

    /**
     * 分组名
     */
    private String groupName;

    /**
     * 教程封面
     */
    @JsonSerialize(using = CdnImgSerializer.class)
    private String columnCover;

    /**
     * 文章排序
     */
    private Integer sort;

    /**
     * 创建时间
     */
    private Timestamp createTime;

    public ColumnArticleDTO setColumnCover(String columnCover) {
        this.columnCover = CdnUtil.autoTransCdn(columnCover);
        return this;
    }
}
