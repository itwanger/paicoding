package com.github.liuyueyi.forum.service.article.repository.dao;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.liueyueyi.forum.api.model.enums.ColumnStatusEnum;
import com.github.liueyueyi.forum.api.model.enums.YesOrNoEnum;
import com.github.liueyueyi.forum.api.model.vo.PageParam;
import com.github.liueyueyi.forum.api.model.vo.article.dto.ColumnArticlesDTO;
import com.github.liuyueyi.forum.service.article.repository.entity.CategoryDO;
import com.github.liuyueyi.forum.service.article.repository.entity.ColumnArticleDO;
import com.github.liuyueyi.forum.service.article.repository.entity.ColumnInfoDO;
import com.github.liuyueyi.forum.service.article.repository.mapper.ColumnArticleMapper;
import com.github.liuyueyi.forum.service.article.repository.mapper.ColumnInfoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
                .orderByAsc(ColumnInfoDO::getSection);
        return baseMapper.selectList(query);
    }

    /**
     * 统计专栏的文章数
     *
     * @return
     */
    public int countColumnArticles(Long columnId) {
        LambdaQueryWrapper<ColumnArticleDO> query = Wrappers.lambdaQuery();
        if (columnId != null && columnId > 0) {
            query.eq(ColumnArticleDO::getColumnId, columnId);
        }
        return columnArticleMapper.selectCount(query).intValue();
    }

    /**
     * 根据专栏ID查询文章信息列表
     *
     * @param columnId
     * @return
     */
    public List<ColumnArticleDO> listColumnArticlesDetail(Long columnId, PageParam pageParam) {
        LambdaQueryWrapper<ColumnArticleDO> query = Wrappers.lambdaQuery();
        if (columnId != null && columnId > 0) {
            query.eq(ColumnArticleDO::getColumnId, columnId);
        }
        query.orderByAsc(ColumnArticleDO::getColumnId, ColumnArticleDO::getSection);
        query.last(PageParam.getLimitSql(pageParam));
        return columnArticleMapper.selectList(query);
    }

    /**
     * 获取文章列表
     *
     * @param columnId
     * @return
     */
    public List<Long> listColumnArticles(Long columnId) {
        return columnArticleMapper.listColumnArticles(columnId);
    }

    public Long getColumnArticleId(long columnId, Integer section) {
        return columnArticleMapper.getColumnArticle(columnId, section);
    }

    /**
     * 分页查询专辑列表（后台）
     *
     * @param pageParam
     * @return
     */
    public List<ColumnInfoDO> listColumns(PageParam pageParam) {
        LambdaQueryWrapper<ColumnInfoDO> query = Wrappers.lambdaQuery();
        query.last(PageParam.getLimitSql(pageParam))
                .orderByAsc(ColumnInfoDO::getSection);
        return baseMapper.selectList(query);
    }

    /**
     * 查询专辑列表总数（后台）
     *
     * @return
     */
    public Integer countColumns() {
        return lambdaQuery().count().intValue();
    }

    /**
     * 删除专栏
     *
     * @param columnId
     */
    public void deleteColumn(Integer columnId) {
        ColumnInfoDO columnInfoDO = baseMapper.selectById(columnId);
        if (columnInfoDO != null) {
            LambdaQueryWrapper<ColumnArticleDO> query = Wrappers.lambdaQuery();
            query.eq(ColumnArticleDO::getColumnId, columnId);
            columnArticleMapper.delete(query);
            baseMapper.deleteById(columnId);
        }
    }
}
