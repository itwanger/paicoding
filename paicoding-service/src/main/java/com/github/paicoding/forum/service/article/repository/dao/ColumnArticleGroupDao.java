package com.github.paicoding.forum.service.article.repository.dao;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.paicoding.forum.api.model.enums.YesOrNoEnum;
import com.github.paicoding.forum.service.article.repository.entity.ColumnArticleGroupDO;
import com.github.paicoding.forum.service.article.repository.mapper.ColumnArticleGroupMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 专栏文章分组
 *
 * @author yihui
 * @date 24/12/17
 */
@Repository
public class ColumnArticleGroupDao extends ServiceImpl<ColumnArticleGroupMapper, ColumnArticleGroupDO> {

    /**
     * 不同层级的排序间隔
     */
    public static final int SECTION_STEP = 1000;


    /**
     * 获取专栏对应的分组列表
     *
     * @param columnId 专栏
     * @return
     */
    public List<ColumnArticleGroupDO> selectByColumnId(Long columnId) {
        return lambdaQuery()
                .eq(ColumnArticleGroupDO::getColumnId, columnId)
                .eq(ColumnArticleGroupDO::getDeleted, YesOrNoEnum.NO.getCode())
                .orderByAsc(ColumnArticleGroupDO::getSection)
                .list();
    }

    /**
     * 根据父分组进行查询
     *
     * @param parentGroupId
     * @return
     */
    public ColumnArticleGroupDO selectByParentGroupId(Long parentGroupId) {
        return lambdaQuery()
                .eq(ColumnArticleGroupDO::getParentGroupId, parentGroupId)
                .eq(ColumnArticleGroupDO::getDeleted, YesOrNoEnum.NO.getCode())
                .one();
    }

    /**
     * 获取同一父分组下的所有数据
     *
     * @param columnId
     * @param parentGroupId
     * @return
     */
    public List<ColumnArticleGroupDO> selectColumnGroupsBySameParent(Long columnId, Long parentGroupId) {
        return lambdaQuery()
                .eq(ColumnArticleGroupDO::getColumnId, columnId)
                .eq(ColumnArticleGroupDO::getParentGroupId, parentGroupId)
                .eq(ColumnArticleGroupDO::getDeleted, YesOrNoEnum.NO.getCode())
                .orderByAsc(ColumnArticleGroupDO::getSection)
                .list();
    }
}
