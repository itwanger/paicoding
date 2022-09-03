package com.github.liuyueyi.forum.service.article.service.impl;

import com.github.liueyueyi.forum.api.model.enums.*;
import com.github.liueyueyi.forum.api.model.exception.ExceptionUtil;
import com.github.liueyueyi.forum.api.model.vo.PageParam;
import com.github.liueyueyi.forum.api.model.vo.article.dto.ArticleDTO;
import com.github.liueyueyi.forum.api.model.vo.article.dto.ArticleListDTO;
import com.github.liueyueyi.forum.api.model.vo.article.dto.CategoryDTO;
import com.github.liueyueyi.forum.api.model.vo.constants.StatusEnum;
import com.github.liuyueyi.forum.service.article.conveter.ArticleConverter;
import com.github.liuyueyi.forum.service.article.repository.dao.ArticleDao;
import com.github.liuyueyi.forum.service.article.repository.dao.ArticleTagDao;
import com.github.liuyueyi.forum.service.article.repository.entity.ArticleDO;
import com.github.liuyueyi.forum.service.article.service.ArticleReadService;
import com.github.liuyueyi.forum.service.article.service.CategoryService;
import com.github.liuyueyi.forum.service.user.repository.entity.UserFootDO;
import com.github.liuyueyi.forum.service.user.service.CountService;
import com.github.liuyueyi.forum.service.user.service.UserFootService;
import com.github.liuyueyi.forum.service.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 文章查询相关服务类
 *
 * @author louzai
 * @date 2022-07-20
 */
@Service
public class ArticleReadServiceImpl implements ArticleReadService {

    @Autowired
    private ArticleDao articleDao;

    @Autowired
    private ArticleTagDao articleTagDao;

    @Autowired
    private CategoryService categoryService;

    /**
     * 在一个项目中，UserFootService 就是内部服务调用
     * 拆微服务时，这个会作为远程服务访问
     */
    @Autowired
    private UserFootService userFootService;

    @Autowired
    private CountService countService;

    @Autowired
    private UserService userService;

    @Override
    public ArticleDO queryBasicArticle(Long articleId) {
        return articleDao.getById(articleId);
    }

    @Override
    public ArticleDTO queryDetailArticleInfo(Long articleId) {
        ArticleDTO article = articleDao.queryArticleDetail(articleId);
        if (article == null) {
            throw ExceptionUtil.of(StatusEnum.RECORDS_NOT_EXISTS, "文章不存在");
        }
        // 更新分类相关信息
        CategoryDTO category = article.getCategory();
        category.setCategory(categoryService.queryCategoryName(category.getCategoryId()));

        // 更新标签信息
        article.setTags(articleTagDao.queryArticleTagDetails(articleId));
        return article;
    }

    /**
     * 查询文章所有的关联信息，正文，分类，标签，阅读计数，当前登录用户是否点赞、评论过
     *
     * @param articleId
     * @param readUser
     * @return
     */
    @Override
    public ArticleDTO queryTotalArticleInfo(Long articleId, Long readUser) {
        ArticleDTO article = queryDetailArticleInfo(articleId);

        // 文章阅读计数+1
        articleDao.incrReadCount(articleId);

        // 文章的操作标记
        if (readUser != null) {
            // 更新用于足迹，并判断是否点赞、评论、收藏
            UserFootDO foot = userFootService.saveOrUpdateUserFoot(DocumentTypeEnum.ARTICLE, articleId, article.getAuthor(), readUser, OperateTypeEnum.READ);
            article.setPraised(Objects.equals(foot.getPraiseStat(), PraiseStatEnum.PRAISE.getCode()));
            article.setCommented(Objects.equals(foot.getCommentStat(), CommentStatEnum.COMMENT.getCode()));
            article.setCollected(Objects.equals(foot.getCommentStat(), CollectionStatEnum.COLLECTION.getCode()));
        } else {
            // 未登录，全部设置为未处理
            article.setPraised(false);
            article.setCommented(false);
            article.setCollected(false);
        }

        // 更新文章统计计数
        article.setCount(countService.queryArticleCountInfoByArticleId(articleId));
        return article;
    }


    @Override
    public ArticleListDTO queryArticlesByCategory(Long categoryId, PageParam page) {
        List<ArticleDO> records = articleDao.listArticlesByCategoryId(categoryId, page);
        return buildArticleListVo(records, page.getPageSize());
    }

    @Override
    public ArticleListDTO queryArticlesBySearchKey(String key, PageParam page) {
        List<ArticleDO> records = articleDao.listArticlesByBySearchKey(key, page);
        return buildArticleListVo(records, page.getPageSize());
    }

    public ArticleListDTO queryArticlesByUserAndType(Long userId, PageParam pageParam, HomeSelectEnum select) {
        List<ArticleDO> records = null;
        if (select == HomeSelectEnum.ARTICLE) {
            // 用户的文章列表
            records = articleDao.listArticlesByUserId(userId, pageParam);
        } else if (select == HomeSelectEnum.READ) {
            // 用户的阅读记录
            List<Long> articleIds = userFootService.queryUserReadArticleList(userId, pageParam);
            records = CollectionUtils.isEmpty(articleIds) ? Collections.emptyList() : articleDao.listByIds(articleIds);
        } else if (select == HomeSelectEnum.COLLECTION) {
            // 用户的收藏列表
            List<Long> articleIds = userFootService.queryUserCollectionArticleList(userId, pageParam);
            records = CollectionUtils.isEmpty(articleIds) ? Collections.emptyList() : articleDao.listByIds(articleIds);
        }

        if (CollectionUtils.isEmpty(records)) {
            return new ArticleListDTO();
        }
        return buildArticleListVo(records, pageParam.getPageSize());
    }

    private ArticleListDTO buildArticleListVo(List<ArticleDO> records, long pageSize) {
        List<ArticleDTO> result = records.stream().map(this::fillArticleRelatedInfo).collect(Collectors.toList());
        ArticleListDTO dto = new ArticleListDTO();
        dto.setArticleList(result);
        dto.setIsMore(result.size() == pageSize);
        return dto;
    }

    /**
     * 补全文章的阅读计数、作者、分类、标签等信息
     *
     * @param record
     * @return
     */
    private ArticleDTO fillArticleRelatedInfo(ArticleDO record) {
        ArticleDTO dto = ArticleConverter.toDto(record);
        // 分类信息
        dto.getCategory().setCategory(categoryService.queryCategoryName(record.getCategoryId()));
        // 标签列表
        dto.setTags(articleTagDao.queryArticleTagDetails(record.getId()));
        // 阅读计数统计
        dto.setCount(countService.queryArticleCountInfoByArticleId(record.getId()));
        // 作者信息
        dto.setAuthorName(userService.queryBasicUserInfo(dto.getAuthor()).getUserName());
        return dto;
    }

    @Override
    public int queryArticleCount(long authorId) {
        return articleDao.countArticleByUser(authorId);
    }
}
