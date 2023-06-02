package com.github.paicoding.forum.service.article.repository.dao;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.paicoding.forum.service.article.repository.entity.ColumnArticleDO;
import com.github.paicoding.forum.service.article.repository.mapper.ColumnArticleMapper;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

/**
 * 微信搜索「沉默王二」，回复 Java
 *
 * @author 沉默王二
 * @date 5/30/23
 */
@Repository
public class ColumnArticleDao extends ServiceImpl<ColumnArticleMapper, ColumnArticleDO> {
    @Resource
    private ColumnArticleMapper columnArticleMapper;

    public Integer selectMaxSection(Long columnId) {
        return columnArticleMapper.selectMaxSection(columnId);
    }

}
