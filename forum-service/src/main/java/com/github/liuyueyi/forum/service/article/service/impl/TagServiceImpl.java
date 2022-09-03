package com.github.liuyueyi.forum.service.article.service.impl;

import com.github.liueyueyi.forum.api.model.vo.article.dto.TagDTO;
import com.github.liuyueyi.forum.service.article.repository.dao.TagDao;
import com.github.liuyueyi.forum.service.article.service.TagService;
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
    public List<TagDTO> queryTagsByCategoryId(Long categoryId) {
        return tagDao.listTagsByCategoryId(categoryId);
    }
}
