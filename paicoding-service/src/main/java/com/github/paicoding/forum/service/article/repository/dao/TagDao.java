package com.github.paicoding.forum.service.article.repository.dao;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.paicoding.forum.api.model.enums.PushStatusEnum;
import com.github.paicoding.forum.api.model.enums.YesOrNoEnum;
import com.github.paicoding.forum.api.model.vo.PageParam;
import com.github.paicoding.forum.api.model.vo.article.dto.TagDTO;
import com.github.paicoding.forum.service.article.conveter.ArticleConverter;
import com.github.paicoding.forum.service.article.repository.entity.TagDO;
import com.github.paicoding.forum.service.article.repository.mapper.TagMapper;
import com.github.paicoding.forum.service.article.repository.params.SearchTagParams;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author XuYifei
 * @date 2024-07-12
 */
@Repository
public class TagDao extends ServiceImpl<TagMapper, TagDO> {

    /**
     * 获取已上线 Tags 列表（分页）
     *
     * @return
     */
    public List<TagDTO> listOnlineTag(String key, PageParam pageParam) {
        LambdaQueryWrapper<TagDO> query = Wrappers.lambdaQuery();
        query.eq(TagDO::getStatus, PushStatusEnum.ONLINE.getCode())
                .eq(TagDO::getDeleted, YesOrNoEnum.NO.getCode())
                .and(StringUtils.isNotBlank(key), v -> v.like(TagDO::getTagName, key))
                .orderByDesc(TagDO::getId);
        if (pageParam != null) {
            query.last(PageParam.getLimitSql(pageParam));
        }
        List<TagDO> list = baseMapper.selectList(query);
        return ArticleConverter.toDtoList(list);
    }

    /**
     * 获取已上线 Tags 总数（分页）
     *
     * @return
     */
    public Integer countOnlineTag(String key) {
        return lambdaQuery()
                .eq(TagDO::getStatus, PushStatusEnum.ONLINE.getCode())
                .eq(TagDO::getDeleted, YesOrNoEnum.NO.getCode())
                .and(!StringUtils.isEmpty(key), v -> v.like(TagDO::getTagName, key))
                .count()
                .intValue();
    }

    private LambdaQueryChainWrapper<TagDO> createTagQuery(SearchTagParams params) {
        return lambdaQuery()
                .eq(TagDO::getDeleted, YesOrNoEnum.NO.getCode())
                .apply(StringUtils.isNotBlank(params.getTag()),
                        "LOWER(tag_name) LIKE {0}",
                        "%" + params.getTag().toLowerCase() + "%");
    }

    /**
     * 获取所有 Tags 列表（分页）
     *
     * @return
     */
    public List<TagDO> listTag(SearchTagParams params) {
        List<TagDO> list = createTagQuery(params)
                .orderByDesc(TagDO::getUpdateTime)
                .last(PageParam.getLimitSql(
                        PageParam.newPageInstance(params.getPageNum(), params.getPageSize())
                ))
                .list();
        return list;
    }



    /**
     * 获取所有 Tags 总数（分页）
     *
     * @return
     */
    public Long countTag(SearchTagParams params) {
        return createTagQuery(params)
                .count();
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
                .eq(TagDO::getTagName, tag)
                .last("limit 1")
                .one();
        return record != null ? record.getId() : null;
    }

    /**
     * 查询tag
     * @param tagId
     * @return
     */
    public TagDTO selectById(Long tagId) {
        TagDO tagDO = lambdaQuery().eq(TagDO::getId, tagId).one();
        return ArticleConverter.toDto(tagDO);
    }


    public List<TagDTO> listAllUndeletedTags(){
        List<TagDO> tagDOS = lambdaQuery().eq(TagDO::getDeleted, YesOrNoEnum.NO.getCode()).list();
        return ArticleConverter.toDtoList(tagDOS);
    }

    public List<TagDO> listTagsCategory(Long categoryId){
        List<TagDO> tagDOS = lambdaQuery().eq(TagDO::getCategoryId, categoryId).eq(TagDO::getDeleted, YesOrNoEnum.NO.getCode()).list();
        return tagDOS;
    }
}
