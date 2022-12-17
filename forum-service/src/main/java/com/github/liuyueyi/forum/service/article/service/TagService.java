package com.github.liuyueyi.forum.service.article.service;

import com.github.liueyueyi.forum.api.model.vo.PageParam;
import com.github.liueyueyi.forum.api.model.vo.PageVo;
import com.github.liueyueyi.forum.api.model.vo.article.dto.TagDTO;

import java.util.List;

/**
 * 标签Service
 *
 * @author louzai
 * @date 2022-07-20
 */
public interface TagService {

    List<TagDTO> queryAllTags();

    PageVo<TagDTO> queryTags(String key, PageParam pageParam);

    Long queryTagId(String tag);

    PageVo<TagDTO> getTagList(PageParam pageParam);
}
