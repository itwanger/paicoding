package com.github.liuyueyi.forum.service.article.conveter;

import com.github.liueyueyi.forum.api.model.enums.SourceTypeEnum;
import com.github.liuyueyi.forum.service.article.dto.ArticleDTO;
import com.github.liuyueyi.forum.service.article.dto.CategoryDTO;
import com.github.liuyueyi.forum.service.article.repository.entity.ArticleDO;
import org.springframework.stereotype.Service;

/**
 * 文章转换
 *
 * @author louzai
 * @date 2022-07-31
 */
@Service
public class ArticleConverter {

    public ArticleDTO toDTO(ArticleDO articleDO) {
        if (articleDO == null) {
            return null;
        }
        ArticleDTO articleDTO = new ArticleDTO();
        articleDTO.setAuthor(articleDO.getUserId());
        articleDTO.setArticleId(articleDO.getId());
        articleDTO.setTitle(articleDO.getTitle());
        articleDTO.setShortTitle(articleDO.getShortTitle());
        articleDTO.setSummary(articleDO.getSummary());
        articleDTO.setCover(articleDO.getPicture());
        articleDTO.setSourceType(SourceTypeEnum.formCode(articleDO.getSource()).getDesc());
        articleDTO.setSourceUrl(articleDO.getSourceUrl());
        articleDTO.setStatus(articleDO.getStatus());
        articleDTO.setLastUpdateTime(articleDO.getUpdateTime().getTime());

        // 设置类目id
        articleDTO.setCategory(new CategoryDTO(articleDO.getCategoryId(), null));
        return articleDTO;
    }
}
