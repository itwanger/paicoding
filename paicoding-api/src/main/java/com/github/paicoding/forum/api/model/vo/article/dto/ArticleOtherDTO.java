package com.github.paicoding.forum.api.model.vo.article.dto;

import com.github.paicoding.forum.api.model.vo.user.dto.UserStatisticInfoDTO;
import lombok.Data;

/**
 * 微信搜索「沉默王二」，回复 Java
 *
 * @author 沉默王二
 * @date 12/8/23
 */
@Data
public class ArticleOtherDTO {
    // 作者信息
    private UserStatisticInfoDTO author;
    // 文章的阅读类型
    private Integer readType;
    // 教程的翻页
    private ColumnArticleFlipDTO flip;
}
