package com.github.paicoding.forum.service.article.repository.dao;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.paicoding.forum.api.model.enums.column.ColumnStatusEnum;
import com.github.paicoding.forum.api.model.exception.ExceptionUtil;
import com.github.paicoding.forum.api.model.vo.PageParam;
import com.github.paicoding.forum.api.model.vo.article.dto.ColumnArticleDTO;
import com.github.paicoding.forum.api.model.vo.article.dto.SimpleArticleDTO;
import com.github.paicoding.forum.api.model.vo.constants.StatusEnum;
import com.github.paicoding.forum.service.article.repository.entity.ColumnArticleDO;
import com.github.paicoding.forum.service.article.repository.entity.ColumnInfoDO;
import com.github.paicoding.forum.service.article.repository.mapper.ColumnArticleMapper;
import com.github.paicoding.forum.service.article.repository.mapper.ColumnInfoMapper;
import com.github.paicoding.forum.service.article.repository.params.SearchColumnArticleParams;
import com.github.paicoding.forum.service.article.repository.params.SearchColumnParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author XuYifei
 * @date 2024-07-12
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
     * 使用mybatis-plus的分页插件
     * 分页查询专栏列表
     *
     * @param currentPage
     * @param pagesize
     * @return
     */
    public Page<ColumnInfoDO> listOnlineColumnsByPage(Long currentPage, Long pagesize) {
        LambdaQueryWrapper<ColumnInfoDO> query = Wrappers.lambdaQuery();
        query.gt(ColumnInfoDO::getState, ColumnStatusEnum.OFFLINE.getCode())
//                .last(PageParam.getLimitSql(pageParam))
                .orderByAsc(ColumnInfoDO::getSection);
        Page<ColumnInfoDO> page = new Page<>(currentPage, pagesize);
        return baseMapper.selectPage(page, query);
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

    public Long countColumnArticles() {
        return columnArticleMapper.selectCount(Wrappers.emptyWrapper());
    }

    /**
     * 统计专栏的阅读人数
     * @return
     */
    public int countColumnReadPeoples(Long columnId) {
        return columnArticleMapper.countColumnReadUserNums(columnId).intValue();
    }

    /**
     * 根据教程ID查询文章信息列表
     * @return
     */
    public List<ColumnArticleDTO> listColumnArticlesDetail(SearchColumnArticleParams params,
                                                           PageParam pageParam) {
        return columnArticleMapper.listColumnArticlesByColumnIdArticleName(params.getColumnId(),
                params.getArticleTitle(),
                pageParam);
    }

    public Integer countColumnArticles(SearchColumnArticleParams params) {
        return columnArticleMapper.countColumnArticlesByColumnIdArticleName(params.getColumnId(),
                params.getArticleTitle()).intValue();
    }

    /**
     * 根据教程ID查询文章ID列表
     *
     * @param columnId
     * @return
     */
    public List<SimpleArticleDTO> listColumnArticles(Long columnId) {
        return columnArticleMapper.listColumnArticles(columnId);
    }

    public ColumnArticleDO getColumnArticleId(long columnId, Integer section) {
        return columnArticleMapper.getColumnArticle(columnId, section);
    }

    /**
     * 删除专栏
     *
     * fixme 改为逻辑删除
     *
     * @param columnId
     */
    public void deleteColumn(Long columnId) {
        ColumnInfoDO columnInfoDO = baseMapper.selectById(columnId);
        if (columnInfoDO != null) {
            // 如果专栏对应的文章不为空，则不允许删除
            // 统计专栏的文章数
            int count = countColumnArticles(columnId);
            if (count > 0) {
                throw ExceptionUtil.of(StatusEnum.COLUMN_ARTICLE_EXISTS,"请先删除教程");
            }

            // 删除专栏
            baseMapper.deleteById(columnId);
        }
    }

    /**
     * 查询教程
     */
    public List<ColumnInfoDO> listColumnsByParams(SearchColumnParams params, PageParam pageParam) {
        LambdaQueryWrapper<ColumnInfoDO> query = Wrappers.lambdaQuery();
        // 加上判空条件
        query.like(StringUtils.isNotBlank(params.getColumn()), ColumnInfoDO::getColumnName, params.getColumn());
        query.last(PageParam.getLimitSql(pageParam))
                .orderByAsc(ColumnInfoDO::getSection)
                .orderByDesc(ColumnInfoDO::getUpdateTime);
        return baseMapper.selectList(query);

    }

    /**
     * 查询教程总数
     */
    public Integer countColumnsByParams(SearchColumnParams params) {
        LambdaQueryWrapper<ColumnInfoDO> query = Wrappers.lambdaQuery();
        lambdaQuery().like(StringUtils.isNotBlank(params.getColumn()), ColumnInfoDO::getColumnName, params.getColumn());
        return baseMapper.selectCount(query).intValue();
    }
}
