package com.github.liuyueyi.forum.service.article.repository.dao;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.liueyueyi.forum.api.model.enums.YesOrNoEnum;
import com.github.liueyueyi.forum.api.model.vo.article.dto.TagDTO;
import com.github.liuyueyi.forum.service.article.conveter.ArticleConverter;
import com.github.liuyueyi.forum.service.article.repository.entity.TagDO;
import com.github.liuyueyi.forum.service.article.repository.mapper.TagMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author YiHui
 * @date 2022/9/2
 */
@Repository
public class TagDao extends ServiceImpl<TagMapper, TagDO> {
    public List<TagDTO> listTagsByCategoryId(Long categoryId) {
        List<TagDO> list = lambdaQuery().eq(TagDO::getDeleted, YesOrNoEnum.NO.getCode()).eq(TagDO::getCategoryId, categoryId).list();
        return ArticleConverter.toDtoList(list);
    }
}
