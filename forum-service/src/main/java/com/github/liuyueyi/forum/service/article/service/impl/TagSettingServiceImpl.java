package com.github.liuyueyi.forum.service.article.service.impl;

import com.github.liueyueyi.forum.api.model.vo.PageParam;
import com.github.liueyueyi.forum.api.model.vo.PageVo;
import com.github.liueyueyi.forum.api.model.vo.article.TagReq;
import com.github.liueyueyi.forum.api.model.vo.article.dto.TagDTO;
import com.github.liuyueyi.forum.core.util.NumUtil;
import com.github.liuyueyi.forum.service.article.conveter.ArticleConverter;
import com.github.liuyueyi.forum.service.article.repository.dao.TagDao;
import com.github.liuyueyi.forum.service.article.repository.entity.TagDO;
import com.github.liuyueyi.forum.service.article.service.TagSettingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 标签后台接口
 *
 * @author louzai
 * @date 2022-09-17
 */
@Service
public class TagSettingServiceImpl implements TagSettingService {

    @Autowired
    private TagDao tagDao;

    @Override
    public void saveTag(TagReq tagReq) {
        TagDO tagDO = ArticleConverter.toDO(tagReq);
        if (NumUtil.nullOrZero(tagReq.getTagId())) {
            tagDao.save(tagDO);
        } else {
            tagDO.setId(tagReq.getTagId());
            tagDao.updateById(tagDO);
        }
    }

    @Override
    public void deleteTag(Integer tagId) {
        TagDO tagDO = tagDao.getById(tagId);
        if (tagDO != null){
            tagDao.removeById(tagId);
        }
    }

    @Override
    public void operateTag(Integer tagId, Integer operateType) {
        TagDO tagDO = tagDao.getById(tagId);
        if (tagDO != null){
            tagDO.setStatus(operateType);
            tagDao.updateById(tagDO);
        }
    }

    @Override
    public PageVo<TagDTO> getTagList(PageParam pageParam) {
        List<TagDTO> tagDTOS = tagDao.listTag(pageParam);
        Integer totalCount = tagDao.countTag();
        return PageVo.build(tagDTOS, pageParam.getPageSize(), pageParam.getPageNum(), totalCount);
    }
}
