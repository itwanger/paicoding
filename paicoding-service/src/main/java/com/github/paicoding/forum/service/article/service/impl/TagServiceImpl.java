package com.github.paicoding.forum.service.article.service.impl;

import com.github.paicoding.forum.api.model.vo.PageParam;
import com.github.paicoding.forum.api.model.vo.PageVo;
import com.github.paicoding.forum.api.model.vo.article.dto.TagDTO;
import com.github.paicoding.forum.service.article.repository.dao.TagDao;
import com.github.paicoding.forum.service.article.service.TagService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 标签Service
 *
 * @author louzai
 * @date 2022-07-20
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
}
