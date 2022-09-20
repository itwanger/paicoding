package com.github.liueyueyi.forum.api.model.vo.article.dto;

import com.github.liueyueyi.forum.api.model.vo.user.dto.ArticleFootCountDTO;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 文章信息
 * <p>
 * DTO 定义返回给web前端的实体类 (VO)
 *
 * @author YiHui
 * @date 2022/7/24
 */
@Data
public class ArticleDTO implements Serializable {
    private static final long serialVersionUID = -793906904770296838L;

    private Long articleId;

    /**
     * 文章类型：1-博文，2-问答
     */
    private Integer articleType;

    /**
     * 作者uid
     */
    private Long author;

    /**
     * 作者名
     */
    private String authorName;

    /**
     * 文章标题
     */
    private String title;

    /**
     * 短标题
     */
    private String shortTitle;

    /**
     * 简介
     */
    private String summary;

    /**
     * 封面
     */
    private String cover;

    /**
     * 正文
     */
    private String content;

    /**
     * 文章来源
     *
     * @see com.github.liueyueyi.forum.api.model.enums.SourceTypeEnum
     */
    private String sourceType;

    /**
     * 原文地址
     */
    private String sourceUrl;

    /**
     * 0 未发布 1 已发布
     */
    private Integer status;

    /**
     * 标记位（二进制）
     */
    private Integer flagBit;

    /**
     * 是否官方
     */
    private Boolean isOffical;

    /**
     * 是否置顶
     */
    private Boolean isTopping;

    /**
     * 是否加精
     */
    private Boolean isCream;

    /**
     * 创建时间
     */
    private Long createTime;

    /**
     * 最后更新时间
     */
    private Long lastUpdateTime;

    /**
     * 分类
     */
    private CategoryDTO category;

    /**
     * 标签
     */
    private List<TagDTO> tags;

    /**
     * 表示当前查看的用户是否已经点赞过
     */
    private Boolean praised;

    /**
     * 表示当用户是否评论过
     */
    private Boolean commented;

    /**
     * 表示当前用户是否收藏过
     */
    private Boolean collected;

    /**
     * 文章对应的统计计数
     */
    private ArticleFootCountDTO count;
}
