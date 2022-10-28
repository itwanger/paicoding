package com.github.liuyueyi.forum.service.article.repository.dao;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.liueyueyi.forum.api.model.enums.PushStatusEnum;
import com.github.liueyueyi.forum.api.model.enums.YesOrNoEnum;
import com.github.liueyueyi.forum.api.model.vo.PageParam;
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
        List<TagDO> list = lambdaQuery()
                .eq(TagDO::getDeleted, YesOrNoEnum.NO.getCode())
                .eq(TagDO::getCategoryId, categoryId)
                .eq(TagDO::getStatus, PushStatusEnum.ONLINE.getCode())
                .list();
        return ArticleConverter.toDtoList(list);
    }

    /**
     * 获取所有 Tags 列表（分页）
     *
     * @return
     */
    public List<TagDTO> listTag(PageParam pageParam) {
        List<TagDO> list = lambdaQuery()
                .eq(TagDO::getDeleted, YesOrNoEnum.NO.getCode())
                .last(PageParam.getLimitSql(pageParam))
                .list();
        return ArticleConverter.toDtoList(list);
    }

    /**
     * 获取所有 Tags 总数（分页）
     *
     * @return
     */
    public Integer countTag() {
        return lambdaQuery()
                .eq(TagDO::getDeleted, YesOrNoEnum.NO.getCode())
                .count()
                .intValue();
    }

    /**
     * 查询tagId
     *
     * @param tag
     * @return
     */
    public Long selectTagIdByTag(String tag) {
        TagDO record = lambdaQuery().select(TagDO::getId)
                .eq(TagDO::getDeleted, YesOrNoEnum.NO.getCode())
                .eq(TagDO::getTagName, tag).one();
        return record != null ? record.getId() : null;
    }
}
