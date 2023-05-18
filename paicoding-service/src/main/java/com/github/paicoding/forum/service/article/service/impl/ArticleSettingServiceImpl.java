package com.github.paicoding.forum.service.article.service.impl;

import com.github.paicoding.forum.api.model.enums.ArticleEventEnum;
import com.github.paicoding.forum.api.model.enums.OperateArticleEnum;
import com.github.paicoding.forum.api.model.enums.PushStatusEnum;
import com.github.paicoding.forum.api.model.enums.YesOrNoEnum;
import com.github.paicoding.forum.api.model.exception.ExceptionUtil;
import com.github.paicoding.forum.api.model.vo.PageParam;
import com.github.paicoding.forum.api.model.vo.PageVo;
import com.github.paicoding.forum.api.model.vo.article.ArticleMsgEvent;
import com.github.paicoding.forum.api.model.vo.article.ArticlePostReq;
import com.github.paicoding.forum.api.model.vo.article.SearchArticleReq;
import com.github.paicoding.forum.api.model.vo.article.dto.ArticleDTO;
import com.github.paicoding.forum.api.model.vo.constants.StatusEnum;
import com.github.paicoding.forum.api.model.vo.user.dto.BaseUserInfoDTO;
import com.github.paicoding.forum.core.util.SpringUtil;
import com.github.paicoding.forum.service.article.conveter.ArticleConverter;
import com.github.paicoding.forum.service.article.conveter.SearchArticleMapper;
import com.github.paicoding.forum.service.article.repository.dao.ArticleDao;
import com.github.paicoding.forum.service.article.repository.entity.ArticleDO;
import com.github.paicoding.forum.service.article.repository.params.SearchArticleParams;
import com.github.paicoding.forum.service.article.service.ArticleSettingService;
import com.github.paicoding.forum.service.user.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

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

    @Autowired
    private UserService userService;

    @Override
    @CacheEvict(key = "'sideBar_' + #req.articleId", cacheManager = "caffeineCacheManager", cacheNames = "article")
    public void updateArticle(ArticlePostReq req) {
        ArticleDO article = articleDao.getById(req.getArticleId());
        if (article == null) {
            return;
        }

        if (StringUtils.isNotBlank(req.getTitle())) {
            article.setTitle(req.getTitle());
        }
        article.setShortTitle(req.getShortTitle());

        ArticleEventEnum operateEvent = null;
        if (req.getStatus() != null) {
            article.setStatus(req.getStatus());
            if (req.getStatus() == PushStatusEnum.OFFLINE.getCode()) {
                operateEvent = ArticleEventEnum.OFFLINE;
            } else if (req.getStatus() == PushStatusEnum.REVIEW.getCode()) {
                operateEvent = ArticleEventEnum.REVIEW;
            } else if (req.getStatus() == PushStatusEnum.ONLINE.getCode()) {
                operateEvent = ArticleEventEnum.ONLINE;
            }
        }
        articleDao.updateById(article);

        if (operateEvent != null) {
            // 发布文章待审核、上线、下线事件
            SpringUtil.publishEvent(new ArticleMsgEvent<>(this, operateEvent, article.getId()));
        }
    }

    @Override
    public Integer getArticleCount() {
        return articleDao.countArticle();
    }

    @Override
    public PageVo<ArticleDTO> getArticleList(SearchArticleReq req) {
        // 转换参数，从前端获取的参数转换为数据库查询参数
        SearchArticleParams searchArticleParams = SearchArticleMapper.INSTANCE.toSearchParams(req);

        // 查询文章列表，分页
        List<ArticleDO> articleDOS = articleDao.listArticlesByParams(
                searchArticleParams, PageParam.newPageInstance(req.getPageNumber(), req.getPageSize()));

        // 转换文章列表，从数据库查询结果转换为前端展示结果
        List<ArticleDTO> articleDTOS = ArticleConverter.toArticleDtoList(articleDOS);

        // 查询文章作者信息
        // fixme: 这里最好直接从数据库使用多表联合查询，而不是遍历后再查询
        articleDTOS.forEach(articleDTO -> {
            BaseUserInfoDTO user = userService.queryBasicUserInfo(articleDTO.getAuthor());
            articleDTO.setAuthorName(user.getUserName());
        });

        // 查询文章总数
        Integer totalCount = articleDao.countArticleByParams(searchArticleParams);
        return PageVo.build(articleDTOS, req.getPageSize(), req.getPageNumber(), totalCount);
    }

    @Override
    public void deleteArticle(Long articleId) {
        ArticleDO dto = articleDao.getById(articleId);
        if (dto != null && dto.getDeleted() != YesOrNoEnum.YES.getCode()) {
            dto.setDeleted(YesOrNoEnum.YES.getCode());
            articleDao.updateById(dto);

            // 发布文章删除事件
            SpringUtil.publishEvent(new ArticleMsgEvent<>(this, ArticleEventEnum.DELETE, articleId));
        }
    }

    @Override
    public void operateArticle(Long articleId, OperateArticleEnum operate) {
        ArticleDO articleDO = articleDao.getById(articleId);
        if (articleDO == null) {
            throw ExceptionUtil.of(StatusEnum.ARTICLE_NOT_EXISTS, articleId);
        }
        setArticleStat(articleDO, operate);
        articleDao.updateById(articleDO);
    }

    private boolean setArticleStat(ArticleDO articleDO, OperateArticleEnum operate) {
        switch (operate) {
            case OFFICAL:
            case CANCEL_OFFICAL:
                return compareAndUpdate(articleDO::getOfficalStat, articleDO::setOfficalStat, operate.getDbStatCode());
            case TOPPING:
            case CANCEL_TOPPING:
                return compareAndUpdate(articleDO::getToppingStat, articleDO::setToppingStat, operate.getDbStatCode());
            case CREAM:
            case CANCEL_CREAM:
                return compareAndUpdate(articleDO::getCreamStat, articleDO::setCreamStat, operate.getDbStatCode());
            default:
                return false;
        }
    }

    /**
     * 相同则直接返回false不用更新；不同则更新,返回true
     *
     * @param supplier
     * @param consumer
     * @param input
     * @param <T>
     * @return
     */
    private <T> boolean compareAndUpdate(Supplier<T> supplier, Consumer<T> consumer, T input) {
        if (Objects.equals(supplier.get(), input)) {
            return false;
        }
        consumer.accept(input);
        return true;
    }
}
