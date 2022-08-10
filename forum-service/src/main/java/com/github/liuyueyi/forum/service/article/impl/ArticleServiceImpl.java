package com.github.liuyueyi.forum.service.article.impl;

import com.github.liueyueyi.forum.api.model.context.ReqInfoContext;
import com.github.liueyueyi.forum.api.model.enums.ArticleTypeEnum;
import com.github.liueyueyi.forum.api.model.enums.OperateTypeEnum;
import com.github.liueyueyi.forum.api.model.enums.PushStatusEnum;
import com.github.liueyueyi.forum.api.model.enums.YesOrNoEnum;
import com.github.liueyueyi.forum.api.model.vo.PageParam;
import com.github.liueyueyi.forum.api.model.vo.article.ArticlePostReq;
import com.github.liuyueyi.forum.service.article.ArticleService;
import com.github.liuyueyi.forum.service.article.CategoryService;
import com.github.liuyueyi.forum.service.article.TagService;
import com.github.liuyueyi.forum.service.article.conveter.ArticleConverter;
import com.github.liuyueyi.forum.service.article.dto.ArticleDTO;
import com.github.liuyueyi.forum.service.article.dto.ArticleListDTO;
import com.github.liuyueyi.forum.service.article.dto.CategoryDTO;
import com.github.liuyueyi.forum.service.article.dto.TagDTO;
import com.github.liuyueyi.forum.service.article.repository.ArticleRepository;
import com.github.liuyueyi.forum.service.article.repository.entity.ArticleDO;
import com.github.liuyueyi.forum.service.article.repository.mapper.ArticleMapper;
import com.github.liuyueyi.forum.service.user.UserFootService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 文章Service
 *
 * @author louzai
 * @date 2022-07-20
 */
@Service
public class ArticleServiceImpl implements ArticleService {

    @Resource
    private ArticleRepository articleRepository;

    @Resource
    private ArticleMapper articleMapper;

    @Resource
    private CategoryService categoryService;

    @Resource
    private TagService tagService;

    @Resource
    private ArticleConverter articleConverter;

    /**
     * 在一个项目中，UserFootService 就是内部服务调用
     * 拆微服务时，这个会作为远程服务访问
     */
    @Autowired
    private UserFootService userFootService;

    /**
     * 获取文章详情
     *
     * @param articleId
     * @return
     */
    @Override
    public ArticleDTO queryArticleDetail(Long articleId) {
        ArticleDTO article = articleRepository.queryArticleDetail(articleId);
        if (article == null) {
            throw new IllegalArgumentException("文章不存在");
        }

        // 更新分类
        CategoryDTO category = article.getCategory();
        category.setCategory(categoryService.getCategoryName(category.getCategoryId()));

        // 更新tagIds
        Set<Long> tagIds = article.getTags().stream().map(TagDTO::getTagId).collect(Collectors.toSet());
        article.setTags(tagService.getTags(tagIds));

        // 更新统计计数
        article.setCount(userFootService.saveArticleCount(articleId, article.getAuthor(), OperateTypeEnum.READ));
        return article;
    }

    /**
     * 保存文章，当articleId存在时，表示更新记录； 不存在时，表示插入
     *
     * @param req
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Long saveArticle(ArticlePostReq req) {
        ArticleDO article = new ArticleDO();
        // 设置作者ID
        article.setUserId(ReqInfoContext.getReqInfo().getUserId());
        article.setId(req.getArticleId());
        article.setTitle(req.getTitle());
        article.setShortTitle(req.getSubTitle());
        article.setArticleType(ArticleTypeEnum.valueOf(req.getArticleType().toUpperCase()).getCode());
        article.setPicture(req.getCover() == null ? "" : req.getCover());
        article.setCategoryId(req.getCategoryId());
        article.setSource(req.getSource());
        article.setSourceUrl(req.getSourceUrl());
        article.setSummary(req.getSummary());
        article.setStatus(req.pushStatus().getCode());
        article.setDeleted(req.deleted() ? 1 : 0);

        return articleRepository.saveArticle(article, req.getContent(), req.getTagIds());
    }


    @Override
    public ArticleListDTO queryArticlesByCategory(Long categoryId, PageParam page) {
        if (categoryId != null && categoryId <= 0) {
            categoryId = null;
        }
        List<ArticleDO> records = articleRepository.getArticleListByCategoryId(categoryId, page);
        List<ArticleDTO> result = new ArrayList<>();
        records.forEach(record -> {
            ArticleDTO dto = articleConverter.toDTO(record);
            dto.setCount(userFootService.queryArticleCountByArticleId(record.getId()));
            result.add(dto);
        });

        ArticleListDTO dto = new ArticleListDTO();
        dto.setArticleList(result);
        dto.setIsMore(result.size() == page.getPageSize());
        return dto;
    }

    @Override
    public void deleteArticle(Long articleId) {
        ArticleDO articleDTO = articleMapper.selectById(articleId);
        if (articleDTO != null) {
            articleDTO.setDeleted(YesOrNoEnum.YES.getCode());
            articleMapper.updateById(articleDTO);
        }
    }

    @Override
    public void operateArticle(Long articleId, PushStatusEnum pushStatusEnum) {
        ArticleDO articleDTO = articleMapper.selectById(articleId);
        if (articleDTO != null) {
            articleDTO.setStatus(pushStatusEnum.getCode());
            articleMapper.updateById(articleDTO);
        }
    }

    @Override
    public ArticleListDTO getArticleListByUserId(Long userId, PageParam pageParam) {

        ArticleListDTO articleListDTO = new ArticleListDTO();
        List<ArticleDO> articleDTOS = articleRepository.getArticleListByUserId(userId, pageParam);
        if (articleDTOS.isEmpty()) {
            articleListDTO.setIsMore(Boolean.FALSE);
            return articleListDTO;
        }

        List<ArticleDTO> articleList = new ArrayList<>();
        for (ArticleDO articleDTO : articleDTOS) {
            ArticleDTO dto = articleConverter.toDTO(articleDTO);
            // TODO: 筛其它数据
            articleList.add(dto);
        }

        Boolean isMore = (articleList.size() == pageParam.getPageSize()) ? Boolean.TRUE : Boolean.FALSE;

        articleListDTO.setArticleList(articleList);
        articleListDTO.setIsMore(isMore);
        return articleListDTO;
    }

    @Override
    public ArticleListDTO getCollectionArticleListByUserId(Long userId, PageParam pageParam) {
        ArticleListDTO articleListDTO = new ArticleListDTO();

        List<ArticleDO> articleDTOS = userFootService.queryCollectionArticleList(userId, pageParam);
        if (articleDTOS.isEmpty()) {
            articleListDTO.setIsMore(Boolean.FALSE);
            return articleListDTO;
        }

        List<ArticleDTO> articleList = new ArrayList<>();
        for (ArticleDO articleDTO : articleDTOS) {
            ArticleDTO dto = articleConverter.toDTO(articleDTO);
            // TODO: 筛其它数据
            articleList.add(dto);
        }

        Boolean isMore = (articleList.size() == pageParam.getPageSize()) ? Boolean.TRUE : Boolean.FALSE;

        articleListDTO.setArticleList(articleList);
        articleListDTO.setIsMore(isMore);
        return articleListDTO;
    }

    @Override
    public ArticleListDTO getReadArticleListByUserId(Long userId, PageParam pageParam) {
        ArticleListDTO articleListDTO = new ArticleListDTO();

        List<ArticleDO> articleDTOS = userFootService.queryReadArticleList(userId, pageParam);
        if (articleDTOS.isEmpty()) {
            articleListDTO.setIsMore(Boolean.FALSE);
            return articleListDTO;
        }

        List<ArticleDTO> articleList = new ArrayList<>();
        for (ArticleDO articleDTO : articleDTOS) {
            ArticleDTO dto = articleConverter.toDTO(articleDTO);
            // TODO: 筛其它数据
            articleList.add(dto);
        }

        Boolean isMore = (articleList.size() == pageParam.getPageSize()) ? Boolean.TRUE : Boolean.FALSE;

        articleListDTO.setArticleList(articleList);
        articleListDTO.setIsMore(isMore);
        return articleListDTO;
    }
}
