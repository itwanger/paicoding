package com.github.paicoding.forum.web.controller.article.vo;

import com.github.paicoding.forum.api.model.vo.PageListVo;
import com.github.paicoding.forum.api.model.vo.article.dto.ArticleDTO;
import lombok.Data;

/**
 * @author XuYifei
 * @date 2024-07-12
 */
@Data
public class ArticleListVo {
    /**
     * 归档类型
     */
    private String archives;
    /**
     * 归档id
     */
    private Long archiveId;

    private PageListVo<ArticleDTO> articles;
}
