package com.github.liuyueyi.forum.service.article.repository.dao;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.liueyueyi.forum.api.model.enums.ColumnStatusEnum;
import com.github.liueyueyi.forum.api.model.vo.PageParam;
import com.github.liuyueyi.forum.service.article.repository.entity.ColumnArticleDO;
import com.github.liuyueyi.forum.service.article.repository.entity.ColumnInfoDO;
import com.github.liuyueyi.forum.service.article.repository.mapper.ColumnArticleMapper;
import com.github.liuyueyi.forum.service.article.repository.mapper.ColumnInfoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author YiHui
 * @date 2022/9/14
 */
@Repository
public class ColumnDao extends ServiceImpl<ColumnInfoMapper, ColumnInfoDO> {
    @Autowired
    private ColumnArticleMapper columnArticleMapper;

    /**
     * 分页查询专辑列表
     *
     * @param pageParam
     * @return
     */
    public List<ColumnInfoDO> listOnlineColumns(PageParam pageParam) {
        LambdaQueryWrapper<ColumnInfoDO> query = Wrappers.lambdaQuery();
        query.gt(ColumnInfoDO::getState, ColumnStatusEnum.OFFLINE.getCode())
                .last(PageParam.getLimitSql(pageParam))
                .orderByDesc(ColumnInfoDO::getId);
        return baseMapper.selectList(query);
    }

    /**
     * 统计专栏的文章数
     *
     * @return
     */
    public int countColumnArticles(Long columnId) {
        LambdaQueryWrapper<ColumnArticleDO> query = Wrappers.lambdaQuery();
        query.eq(ColumnArticleDO::getColumnId, columnId);
        return columnArticleMapper.selectCount(query).intValue();
    }

    /**
     * 获取文章列表
     *
     * @param columnId
     * @return
     */
    public List<Long> listColumnArticles(Long columnId) {
        LambdaQueryWrapper<ColumnArticleDO> query = Wrappers.lambdaQuery();
        query.eq(ColumnArticleDO::getColumnId, columnId).select(ColumnArticleDO::getArticleId)
                .orderByAsc(ColumnArticleDO::getOrder);
        List<ColumnArticleDO> list = columnArticleMapper.selectList(query);
        return list.stream().map(ColumnArticleDO::getArticleId).collect(Collectors.toList());
    }

    public ColumnArticleDO getColumnArticleId(long columnId, long articleId) {
        LambdaQueryWrapper<ColumnArticleDO> query = Wrappers.lambdaQuery();
        query.eq(ColumnArticleDO::getColumnId, columnId)
                .eq(ColumnArticleDO::getArticleId, articleId);
        return columnArticleMapper.selectOne(query);
    }
}
