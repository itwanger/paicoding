package com.github.paicoding.forum.service.article.service;

import com.github.paicoding.forum.api.model.vo.PageVo;
import com.github.paicoding.forum.api.model.vo.article.SearchTagReq;
import com.github.paicoding.forum.api.model.vo.article.TagReq;
import com.github.paicoding.forum.api.model.vo.article.dto.TagDTO;

/**
 * 标签后台接口
 *
 * @author XuYifei
 * @date 2024-07-12
 */
public interface TagSettingService {

    void saveTag(TagReq tagReq);

    void deleteTag(Integer tagId);

    void operateTag(Integer tagId, Integer pushStatus);

    /**
     * 获取tag列表
     *
     * @param pageParam
     * @return
     */
    PageVo<TagDTO> getTagList(SearchTagReq req);

    TagDTO getTagById(Long tagId);
}
