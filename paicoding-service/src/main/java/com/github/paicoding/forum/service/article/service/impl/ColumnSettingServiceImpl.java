package com.github.paicoding.forum.service.article.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.paicoding.forum.api.model.vo.PageParam;
import com.github.paicoding.forum.api.model.vo.PageVo;
import com.github.paicoding.forum.api.model.vo.article.ColumnArticleReq;
import com.github.paicoding.forum.api.model.vo.article.ColumnReq;
import com.github.paicoding.forum.api.model.vo.article.dto.ColumnArticleDTO;
import com.github.paicoding.forum.api.model.vo.article.dto.ColumnDTO;
import com.github.paicoding.forum.api.model.vo.article.dto.SimpleColumnDTO;
import com.github.paicoding.forum.api.model.vo.user.dto.BaseUserInfoDTO;
import com.github.paicoding.forum.core.util.NumUtil;
import com.github.paicoding.forum.service.article.conveter.ColumnConvert;
import com.github.paicoding.forum.service.article.repository.dao.ArticleDao;
import com.github.paicoding.forum.service.article.repository.dao.ColumnDao;
import com.github.paicoding.forum.service.article.repository.entity.ArticleDO;
import com.github.paicoding.forum.service.article.repository.entity.ColumnArticleDO;
import com.github.paicoding.forum.service.article.repository.entity.ColumnInfoDO;
import com.github.paicoding.forum.service.article.repository.mapper.ColumnArticleMapper;
import com.github.paicoding.forum.service.article.service.ColumnSettingService;
import com.github.paicoding.forum.service.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 专栏后台接口
 *
 * @author louzai
 * @date 2022-09-19
 */
@Service
public class ColumnSettingServiceImpl implements ColumnSettingService {

    @Autowired
    private ColumnServiceImpl columnService;

    @Autowired
    private ColumnDao columnDao;

    @Autowired
    private ArticleDao articleDao;

    @Autowired
    private UserService userService;

    @Autowired
    private ColumnArticleMapper columnArticleMapper;

    @Override
    public void saveColumn(ColumnReq req) {
        ColumnInfoDO columnInfoDO = ColumnConvert.toDo(req);
        if (NumUtil.nullOrZero(req.getColumnId())) {
            columnDao.save(columnInfoDO);
        } else {
            columnInfoDO.setId(req.getColumnId());
            columnDao.updateById(columnInfoDO);
        }
    }

    @Override
    public void saveColumnArticle(ColumnArticleReq req) {
        ColumnArticleDO columnArticleDO = ColumnConvert.toDo(req);
        if (NumUtil.nullOrZero(req.getId())) {
            columnArticleMapper.insert(columnArticleDO);
        } else {
            columnArticleDO.setId(req.getId());
            columnArticleMapper.updateById(columnArticleDO);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sortColumnArticle(List<ColumnArticleReq> columnArticleReqs) {
        columnArticleReqs.forEach(columnArticleReq -> {
            ColumnArticleDO columnArticleDO = columnArticleMapper.selectById(columnArticleReq.getId());
            columnArticleDO.setSection(columnArticleReq.getSort());
            columnArticleMapper.updateById(columnArticleDO);
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteColumn(Integer columnId) {
        columnDao.deleteColumn(columnId);
    }

    @Override
    public void deleteColumnArticle(Integer id) {
        ColumnArticleDO columnArticleDO = columnArticleMapper.selectById(id);
        if (columnArticleDO != null) {
            columnArticleMapper.deleteById(id);
        }
    }

    @Override
    public PageVo<ColumnDTO> listColumn(PageParam pageParam) {
        List<ColumnInfoDO> columnList = columnDao.listColumns(pageParam);
        List<ColumnDTO> columnDTOS = ColumnConvert.toDtos(columnList);
        columnDTOS.forEach(columnDTO -> {
            BaseUserInfoDTO user = userService.queryBasicUserInfo(columnDTO.getAuthor());
            columnDTO.setAuthorName(user.getUserName());
            columnDTO.setAuthorAvatar(user.getPhoto());
            columnDTO.setAuthorProfile(user.getProfile());
        });
        Integer totalCount = columnDao.countColumns();
        return PageVo.build(columnDTOS, pageParam.getPageSize(), pageParam.getPageNum(), totalCount);
    }

    @Override
    public PageVo<ColumnArticleDTO> queryColumnArticles(long columnId, PageParam pageParam) throws Exception {
        List<ColumnArticleDTO> simpleArticleDTOS = new ArrayList<>();
        List<ColumnArticleDO> columnArticleDOS = columnDao.listColumnArticlesDetail(columnId, pageParam);
        for (ColumnArticleDO columnArticleDO : columnArticleDOS) {
            ArticleDO articleDO = articleDao.getById(columnArticleDO.getArticleId());
            if (articleDO == null) {
                throw new Exception("文章不存在");
            }
            ColumnInfoDO columnInfoDO = columnDao.getById(columnArticleDO.getColumnId());
            if (columnInfoDO == null) {
                throw new Exception("课程不存在");
            }
            ColumnArticleDTO columnArticleDTO = new ColumnArticleDTO();
            columnArticleDTO.setId(columnArticleDO.getId());
            columnArticleDTO.setArticleId(articleDO.getId());
            columnArticleDTO.setTitle(articleDO.getTitle());
            columnArticleDTO.setSort(columnArticleDO.getSection());
            columnArticleDTO.setColumnId(columnArticleDO.getColumnId());
            columnArticleDTO.setColumn(columnInfoDO.getColumnName());
            simpleArticleDTOS.add(columnArticleDTO);
        }
        Integer totalCount = columnDao.countColumnArticles(columnId);
        return PageVo.build(simpleArticleDTOS, pageParam.getPageSize(), pageParam.getPageNum(), totalCount);
    }

    @Override
    public List<SimpleColumnDTO> listSimpleColumnByBySearchKey(String key) {
        LambdaQueryWrapper<ColumnInfoDO> query = Wrappers.lambdaQuery();
        query.select(ColumnInfoDO::getId, ColumnInfoDO::getColumnName)
                .and(!StringUtils.isEmpty(key),
                    v -> v.like(ColumnInfoDO::getColumnName, key)
                )
                .orderByDesc(ColumnInfoDO::getId);
        return ColumnConvert.toSimpleColumnDTOs(columnDao.list(query));
    }
}
