package com.github.paicoding.forum.web.controller.article.vo;

import com.github.paicoding.forum.api.model.vo.article.dto.ArticleDTO;
import com.github.paicoding.forum.api.model.vo.article.dto.CategoryDTO;
import com.github.paicoding.forum.api.model.vo.article.dto.TagDTO;
import lombok.Data;

import java.util.List;

/**
 * @author XuYifei
 * @date 2024-07-12
 */
@Data
public class ArticleEditVo {

    private ArticleDTO article;

    private List<CategoryDTO> categories;

    private List<TagDTO> tags;

}
