package com.github.liuyueyi.forum.service.article.service.impl;

import com.github.liueyueyi.forum.api.model.enums.OperateArticleTypeEnum;
import com.github.liueyueyi.forum.api.model.vo.PageParam;
import com.github.liueyueyi.forum.api.model.vo.PageVo;
import com.github.liueyueyi.forum.api.model.vo.article.dto.ArticleDTO;
import com.github.liueyueyi.forum.api.model.vo.article.dto.FlagBitDTO;
import com.github.liuyueyi.forum.service.article.conveter.ArticleConverter;
import com.github.liuyueyi.forum.service.article.repository.dao.ArticleDao;
import com.github.liuyueyi.forum.service.article.repository.entity.ArticleDO;
import com.github.liuyueyi.forum.service.article.service.ArticleSettingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 文章后台
 *
 * @author louzai
 * @date 2022-09-19
 */
@Service
public class ArticleSettingServiceImpl implements ArticleSettingService {

    @Autowired
    private ArticleDao articleDao;

    @Override
    public Integer getArticleCount() {
        return articleDao.countArticle();
    }

    @Override
    public PageVo<ArticleDTO> getArticleList(PageParam pageParam) {
        List<ArticleDO> articleDOS = articleDao.listArticles(pageParam);
        Integer totalCount = articleDao.countArticle();
        return PageVo.build(ArticleConverter.toArticleDtoList(articleDOS),pageParam.getPageSize(), pageParam.getPageNum(),totalCount);
    }

    @Override
    public void operateArticle(Long articleId, Integer operateType) {
        ArticleDTO articleDTO = articleDao.queryArticleDetail(articleId);
        Integer flagBit = articleDTO.getFlagBit();

        FlagBitDTO flagBitDTO = OperateArticleTypeEnum.formCode(operateType).getFlagBit();
        if (flagBitDTO.getForward().equals(Boolean.TRUE)) {
            flagBit = flagBit | flagBitDTO.getFlagBit();
        } else {
            flagBit = flagBit & ~flagBitDTO.getFlagBit();
        }
        articleDao.updateArticleFlagBit(articleId, flagBit);
    }
}
