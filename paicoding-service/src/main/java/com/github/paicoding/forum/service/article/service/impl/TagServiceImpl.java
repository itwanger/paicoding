package com.github.paicoding.forum.service.article.service.impl;

import com.github.paicoding.forum.api.model.vo.PageParam;
import com.github.paicoding.forum.api.model.vo.PageVo;
import com.github.paicoding.forum.api.model.vo.article.dto.TagDTO;
import com.github.paicoding.forum.service.article.conveter.ArticleConverter;
import com.github.paicoding.forum.service.article.repository.dao.TagDao;
import com.github.paicoding.forum.service.article.service.TagService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 标签Service
 *
 * @author XuYifei
 * @date 2024-07-12
 */
@Service
public class TagServiceImpl implements TagService {
    private final TagDao tagDao;

    public TagServiceImpl(TagDao tagDao) {
        this.tagDao = tagDao;
    }

    @Override
    public PageVo<TagDTO> queryTags(String key, PageParam pageParam) {
        List<TagDTO> tagDTOS = tagDao.listOnlineTag(key, pageParam);
        Integer totalCount = tagDao.countOnlineTag(key);
        return PageVo.build(tagDTOS, pageParam.getPageSize(), pageParam.getPageNum(), totalCount);
    }

    @Override
    public Long queryTagId(String tag) {
        return tagDao.selectTagIdByTag(tag);
    }

    /**
     * 查询出所有没有被逻辑删除的tags
     * @return
     */
    @Override
    public List<TagDTO> listAllUndeletedTags() {

        return tagDao.listAllUndeletedTags();
    }

    /**
     * 查询出所有没有被逻辑删除的且分类属于categoryId的tags
     * @param categoryId
     * @return
     */
    @Override
    public List<TagDTO> listTagsCategory(Long categoryId) {
        return ArticleConverter.toDtoList(tagDao.listTagsCategory(categoryId));
    }
}
