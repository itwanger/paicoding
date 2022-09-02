package com.github.liuyueyi.forum.service.article.conveter;

import com.github.liueyueyi.forum.api.model.enums.ArticleTypeEnum;
import com.github.liueyueyi.forum.api.model.enums.SourceTypeEnum;
import com.github.liueyueyi.forum.api.model.enums.YesOrNoEnum;
import com.github.liueyueyi.forum.api.model.vo.article.ArticlePostReq;
import com.github.liueyueyi.forum.api.model.vo.article.dto.ArticleDTO;
import com.github.liueyueyi.forum.api.model.vo.article.dto.CategoryDTO;
import com.github.liueyueyi.forum.api.model.vo.article.dto.TagDTO;
import com.github.liuyueyi.forum.service.article.repository.entity.ArticleDO;
import com.github.liuyueyi.forum.service.article.repository.entity.CategoryDO;
import com.github.liuyueyi.forum.service.article.repository.entity.TagDO;
import org.springframework.beans.BeanUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 文章转换
 * <p>
 *
 * @author louzai
 * @date 2022-07-31
 */
public class ArticleConverter {

    public static ArticleDO toArticleDo(ArticlePostReq req, Long author) {
        ArticleDO article = new ArticleDO();
        // 设置作者ID
        article.setUserId(author);
        article.setId(req.getArticleId());
        article.setTitle(req.getTitle());
        article.setShortTitle(req.getSubTitle());
        article.setArticleType(ArticleTypeEnum.valueOf(req.getArticleType().toUpperCase()).getCode());
        article.setPicture(req.getCover() == null ? "" : req.getCover());
        article.setCategoryId(req.getCategoryId());
        article.setSource(req.getSource());
        article.setSourceUrl(req.getSourceUrl());
        article.setSummary(req.getSummary());
        article.setStatus(req.pushStatus().getCode());
        article.setDeleted(req.deleted() ? YesOrNoEnum.YES.getCode() : YesOrNoEnum.NO.getCode());
        return article;
    }

    public static ArticleDTO toDto(ArticleDO articleDO) {
        if (articleDO == null) {
            return null;
        }
        ArticleDTO articleDTO = new ArticleDTO();
        articleDTO.setAuthor(articleDO.getUserId());
        articleDTO.setArticleId(articleDO.getId());
        articleDTO.setArticleType(articleDO.getArticleType());
        articleDTO.setTitle(articleDO.getTitle());
        articleDTO.setShortTitle(articleDO.getShortTitle());
        articleDTO.setSummary(articleDO.getSummary());
        articleDTO.setCover(articleDO.getPicture());
        articleDTO.setSourceType(SourceTypeEnum.formCode(articleDO.getSource()).getDesc());
        articleDTO.setSourceUrl(articleDO.getSourceUrl());
        articleDTO.setStatus(articleDO.getStatus());
        articleDTO.setCreateTime(articleDO.getCreateTime().getTime());
        articleDTO.setLastUpdateTime(articleDO.getUpdateTime().getTime());

        // 设置类目id
        articleDTO.setCategory(new CategoryDTO(articleDO.getCategoryId(), null));
        return articleDTO;
    }


    /**
     * fixme 知识点：<a href="https://blog.hhui.top/hexblog/2021/04/08/210408-%E5%B8%B8%E8%A7%81Bean%E6%8B%B7%E8%B4%9D%E6%A1%86%E6%9E%B6%E4%BD%BF%E7%94%A8%E5%A7%BF%E5%8A%BF%E5%8F%8A%E6%80%A7%E8%83%BD%E5%AF%B9%E6%AF%94/"/>
     *
     * @param tag
     * @return
     */
    public static TagDTO toDto(TagDO tag) {
        TagDTO dto = new TagDTO();
        // 注意，下面这个不要使用Apache的工具类，性能很差
        BeanUtils.copyProperties(tag, dto);
        return dto;
    }

    public static List<TagDTO> toDtoList(List<TagDO> tags) {
        return tags.stream().map(ArticleConverter::toDto).collect(Collectors.toList());
    }


    public static CategoryDTO toDto(CategoryDO category) {
        CategoryDTO dto = new CategoryDTO();
        dto.setCategory(category.getCategoryName());
        dto.setCategoryId(category.getId());
        dto.setSelected(false);
        return dto;
    }
}
