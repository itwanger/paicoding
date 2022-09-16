package com.github.liueyueyi.forum.api.model.vo.article.dto;

import com.github.liueyueyi.forum.api.model.vo.comment.dto.TopCommentDTO;
import lombok.Data;

import java.util.List;

/**
 * @author YiHui
 * @date 2022/9/14
 */
@Data
public class ColumnArticlesDTO {
    /**
     * 专栏详情
     */
    private Long column;

    /**
     * 文章详情
     */
    private ArticleDTO article;

    /**
     * 文章评论
     */
    private List<TopCommentDTO> comments;

    /**
     * 文章目录列表
     */
    private List<SimpleArticleDTO> articleList;
}
