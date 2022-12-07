package com.github.liuyueyi.forum.service.article.service.impl;

import com.github.liueyueyi.forum.api.model.enums.PushStatusEnum;
import com.github.liueyueyi.forum.api.model.vo.PageParam;
import com.github.liueyueyi.forum.api.model.vo.PageVo;
import com.github.liueyueyi.forum.api.model.vo.article.ColumnArticleReq;
import com.github.liueyueyi.forum.api.model.vo.article.ColumnReq;
import com.github.liueyueyi.forum.api.model.vo.article.dto.ArticleDTO;
import com.github.liueyueyi.forum.api.model.vo.article.dto.ColumnDTO;
import com.github.liueyueyi.forum.api.model.vo.article.dto.SimpleArticleDTO;
import com.github.liueyueyi.forum.api.model.vo.user.dto.BaseUserInfoDTO;
import com.github.liuyueyi.forum.core.util.NumUtil;
import com.github.liuyueyi.forum.service.article.conveter.ArticleConverter;
import com.github.liuyueyi.forum.service.article.conveter.ColumnConvert;
import com.github.liuyueyi.forum.service.article.repository.dao.ArticleDao;
import com.github.liuyueyi.forum.service.article.repository.dao.ColumnDao;
import com.github.liuyueyi.forum.service.article.repository.entity.ArticleDO;
import com.github.liuyueyi.forum.service.article.repository.entity.ColumnArticleDO;
import com.github.liuyueyi.forum.service.article.repository.entity.ColumnInfoDO;
import com.github.liuyueyi.forum.service.article.repository.mapper.ColumnArticleMapper;
import com.github.liuyueyi.forum.service.article.service.ColumnSettingService;
import com.github.liuyueyi.forum.service.user.service.UserService;
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
            columnArticleDO.setSection(columnArticleReq.getSection());
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
    public PageVo<SimpleArticleDTO> queryColumnArticles(long columnId, PageParam pageParam) {
        List<SimpleArticleDTO> simpleArticleDTOS = new ArrayList<>();
        List<ColumnArticleDO> columnArticleDOS = columnDao.listColumnArticlesDetail(columnId, pageParam);
        for (ColumnArticleDO columnArticleDO : columnArticleDOS) {
            ArticleDO articleDO = articleDao.getById(columnArticleDO.getArticleId());
            if (articleDO == null) {
                continue;
            }
            ColumnInfoDO columnInfoDO = columnDao.getById(columnArticleDO.getColumnId());
            if (columnInfoDO == null) {
                continue;
            }
            SimpleArticleDTO simpleArticleDTO = new SimpleArticleDTO();
            simpleArticleDTO.setId(articleDO.getId());
            simpleArticleDTO.setTitle(articleDO.getTitle());
            simpleArticleDTO.setSort(columnArticleDO.getSection());
            simpleArticleDTO.setColumnId(columnArticleDO.getColumnId());
            simpleArticleDTO.setColumn(columnInfoDO.getColumnName());
            simpleArticleDTOS.add(simpleArticleDTO);
        }
        Integer totalCount = columnDao.countColumnArticles(columnId);
        return PageVo.build(simpleArticleDTOS, pageParam.getPageSize(), pageParam.getPageNum(), totalCount);
    }
}
