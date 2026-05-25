package com.github.paicoding.forum.service.article.service.impl;

import com.github.paicoding.forum.api.model.exception.ExceptionUtil;
import com.github.paicoding.forum.api.model.vo.PageListVo;
import com.github.paicoding.forum.api.model.vo.PageParam;
import com.github.paicoding.forum.api.model.vo.article.dto.ColumnDTO;
import com.github.paicoding.forum.api.model.vo.article.dto.SimpleArticleDTO;
import com.github.paicoding.forum.api.model.vo.constants.StatusEnum;
import com.github.paicoding.forum.api.model.vo.user.dto.BaseUserInfoDTO;
import com.github.paicoding.forum.api.model.vo.user.dto.ColumnFootCountDTO;
import com.github.paicoding.forum.core.senstive.SensitiveService;
import com.github.paicoding.forum.core.util.UrlSlugUtil;
import com.github.paicoding.forum.service.article.conveter.ColumnConvert;
import com.github.paicoding.forum.service.article.repository.dao.ArticleDao;
import com.github.paicoding.forum.service.article.repository.dao.ColumnArticleDao;
import com.github.paicoding.forum.service.article.repository.dao.ColumnDao;
import com.github.paicoding.forum.service.article.repository.entity.ArticleDO;
import com.github.paicoding.forum.service.article.repository.entity.ColumnArticleDO;
import com.github.paicoding.forum.service.article.repository.entity.ColumnInfoDO;
import com.github.paicoding.forum.service.article.service.ColumnService;
import com.github.paicoding.forum.service.article.service.SlugGeneratorService;
import com.github.paicoding.forum.service.sensitive.service.SensitiveBypassService;
import com.github.paicoding.forum.service.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author YiHui
 * @date 2022/9/14
 */
@Service
@Slf4j
public class ColumnServiceImpl implements ColumnService {
    @Autowired
    private ColumnDao columnDao;
    @Autowired
    private ArticleDao articleDao;

    @Autowired
    private ColumnArticleDao columnArticleDao;

    @Autowired
    private UserService userService;

    @Autowired
    private SensitiveService sensitiveService;

    @Autowired
    private SensitiveBypassService sensitiveBypassService;

    @Autowired
    private SlugGeneratorService slugGeneratorService;

    @Override
    public ColumnArticleDO getColumnArticleRelation(Long articleId) {
        return columnArticleDao.selectColumnArticleByArticleId(articleId);
    }

    /**
     * 专栏列表
     *
     * @return
     */
    @Override
    public PageListVo<ColumnDTO> listColumn(PageParam pageParam) {
        List<ColumnInfoDO> columnList = columnDao.listOnlineColumns(pageParam);
        List<ColumnDTO> result = columnList.stream().map(column -> buildColumnInfo(ensureColumnUrlSlug(column))).collect(Collectors.toList());
        return PageListVo.newVo(result, pageParam.getPageSize());
    }

    @Override
    public ColumnDTO queryBasicColumnInfo(Long columnId) {
        // 查找专栏信息
        ColumnInfoDO column = columnDao.getById(columnId);
        if (column == null) {
            throw ExceptionUtil.of(StatusEnum.COLUMN_NOT_EXISTS, columnId);
        }
        return ColumnConvert.toDto(ensureColumnUrlSlug(column));
    }

    @Override
    public ColumnDTO queryBasicColumnInfo(String columnKey) {
        if (StringUtils.isBlank(columnKey)) {
            throw ExceptionUtil.of(StatusEnum.COLUMN_NOT_EXISTS, columnKey);
        }
        if (StringUtils.isNumeric(columnKey)) {
            return queryBasicColumnInfo(Long.valueOf(columnKey));
        }
        ColumnInfoDO column = columnDao.getByUrlSlug(columnKey);
        if (column == null) {
            throw ExceptionUtil.of(StatusEnum.COLUMN_NOT_EXISTS, columnKey);
        }
        return ColumnConvert.toDto(ensureColumnUrlSlug(column));
    }

    @Override
    public ColumnDTO queryColumnInfo(Long columnId) {
        return buildColumnInfo(queryBasicColumnInfo(columnId));
    }

    @Override
    public ColumnDTO queryColumnInfo(String columnKey) {
        return buildColumnInfo(queryBasicColumnInfo(columnKey));
    }

    private ColumnDTO buildColumnInfo(ColumnInfoDO info) {
        return buildColumnInfo(ColumnConvert.toDto(info));
    }

    /**
     * 构建专栏详情信息
     *
     * @param dto
     * @return
     */
    private ColumnDTO buildColumnInfo(ColumnDTO dto) {
        // 补齐专栏对应的用户信息
        BaseUserInfoDTO user = userService.queryBasicUserInfo(dto.getAuthor());
        dto.setAuthorName(user.getUserName());
        dto.setAuthorAvatar(user.getPhoto());
        dto.setAuthorProfile(user.getProfile());

        // 统计计数
        ColumnFootCountDTO countDTO = new ColumnFootCountDTO();
        // 更新文章数
        countDTO.setArticleCount(columnDao.countColumnArticles(dto.getColumnId()));
        // 专栏阅读人数
        countDTO.setReadCount(columnDao.countColumnReadPeoples(dto.getColumnId()));
        // 总的章节数
        countDTO.setTotalNums(dto.getNums());
        dto.setCount(countDTO);
        return dto;
    }


    @Override
    public ColumnArticleDO queryColumnArticle(long columnId, Integer section) {
        ColumnArticleDO article = columnDao.getColumnArticleId(columnId, section);
        if (article == null) {
            throw ExceptionUtil.of(StatusEnum.ARTICLE_NOT_EXISTS, section);
        }
        return article;
    }

    @Override
    public ColumnArticleDO queryColumnArticle(long columnId, Long articleId) {
        ColumnArticleDO article = columnArticleDao.selectColumnArticle(columnId, articleId);
        if (article == null) {
            throw ExceptionUtil.of(StatusEnum.ARTICLE_NOT_EXISTS, articleId);
        }
        return article;
    }

    @Override
    public ColumnArticleDO queryColumnArticle(long columnId, String articleSlug) {
        if (StringUtils.isBlank(articleSlug)) {
            throw ExceptionUtil.of(StatusEnum.ARTICLE_NOT_EXISTS, articleSlug);
        }
        ColumnArticleDO article = columnArticleDao.selectColumnArticleByArticleSlug(columnId, articleSlug);
        if (article == null) {
            for (SimpleArticleDTO item : queryColumnArticles(columnId)) {
                if (StringUtils.equals(item.getUrlSlug(), articleSlug)) {
                    return queryColumnArticle(columnId, item.getId());
                }
            }
            throw ExceptionUtil.of(StatusEnum.ARTICLE_NOT_EXISTS, articleSlug);
        }
        return article;
    }

    @Override
    public List<SimpleArticleDTO> queryColumnArticles(long columnId) {
        List<SimpleArticleDTO> list = columnDao.listColumnArticles(columnId);
        long preGroup = -1;
        for (SimpleArticleDTO article : list) {
            if (!sensitiveBypassService.shouldBypassByUserId(article.getAuthorId())) {
                article.setTitle(sanitizeText(article.getTitle()));
                article.setColumn(sanitizeText(article.getColumn()));
                article.setGroupName(sanitizeText(article.getGroupName()));
            }
            article.setUrlSlug(ensureColumnArticleUrlSlug(columnId, article.getId(), article.getTitle(), article.getUrlSlug()));
            if (preGroup != article.getGroupLevel()) {
                preGroup = article.getGroupLevel();
                article.setGroupLevel(groupSectionToLevel(article.getGroupLevel()));
            } else {
                // 和前面一个是同一层级，则不需要显示分组，直接沿用之前的即可
                article.setGroupLevel(groupSectionToLevel(article.getGroupLevel()));
            }
        }
        return list;
    }

    private int groupSectionToLevel(long section) {
        // 0 - 1000 是一层, 1000 1000_000 是二层
        if (section < 1000) {
            return 1;
        } else if (section < 1000_000) {
            return 2;
        } else if (section < 1000_000_000L) {
            return 3;
        } else if (section < 1000_000_000_000L) {
            return 4;
        } else if (section < 1000_000_000_000_000L) {
            return 5;
        } else {
            return 6;
        }
    }

    @Override
    public Long getTutorialCount() {
        return this.columnDao.countColumnArticles();
    }

    private String sanitizeText(String text) {
        return text == null ? null : sensitiveService.replace(text);
    }

    @Override
    public String ensureColumnArticleUrlSlug(long columnId, Long articleId, String title, String currentSlug) {
        if (isOwnReadmeArticle(columnId, articleId) && StringUtils.equals(currentSlug, "readme")) {
            return currentSlug;
        }
        if (isValidArticleSlug(currentSlug)
                && !articleDao.existsUrlSlug(currentSlug, articleId)
                && !columnDao.existsUrlSlug(currentSlug, null)) {
            return currentSlug;
        }
        String baseSlug = generateArticleSlug(title);
        if (StringUtils.isBlank(baseSlug)) {
            baseSlug = "article";
        } else if (StringUtils.isNumeric(baseSlug)) {
            baseSlug = "article-" + baseSlug;
        }
        String slug = baseSlug;
        int suffix = 2;
        while (articleDao.existsUrlSlug(slug, articleId) || columnDao.existsUrlSlug(slug, null)) {
            slug = baseSlug + "-" + suffix++;
        }
        ArticleDO update = new ArticleDO();
        update.setId(articleId);
        update.setUrlSlug(slug);
        articleDao.updateById(update);
        return slug;
    }

    private String generateArticleSlug(String title) {
        try {
            return slugGeneratorService.generateSlugWithAI(title);
        } catch (Exception e) {
            log.warn("AI生成教程文章slug失败，使用本地规则兜底: title={}", title, e);
            return UrlSlugUtil.generateSlug(title);
        }
    }

    private ColumnInfoDO ensureColumnUrlSlug(ColumnInfoDO column) {
        if (column == null || isValidColumnSlug(column.getUrlSlug())) {
            return column;
        }
        String urlSlug = generateUniqueColumnSlug(column.getColumnName(), column.getId());
        ColumnInfoDO update = new ColumnInfoDO();
        update.setId(column.getId());
        update.setUrlSlug(urlSlug);
        columnDao.updateById(update);
        column.setUrlSlug(urlSlug);
        return column;
    }

    private String generateUniqueColumnSlug(String columnName, Long columnId) {
        String baseSlug = UrlSlugUtil.generateSlug(columnName);
        if (StringUtils.isBlank(baseSlug)) {
            baseSlug = "column";
        } else if (StringUtils.isNumeric(baseSlug)) {
            baseSlug = "column-" + baseSlug;
        }
        String slug = baseSlug;
        int suffix = 2;
        while (columnDao.existsUrlSlug(slug, columnId) || articleDao.existsUrlSlug(slug, null)) {
            slug = baseSlug + "-" + suffix++;
        }
        return slug;
    }

    private boolean isValidColumnSlug(String urlSlug) {
        return UrlSlugUtil.isValidSlug(urlSlug) && !StringUtils.isNumeric(urlSlug);
    }

    private boolean isValidArticleSlug(String urlSlug) {
        return UrlSlugUtil.isValidSlug(urlSlug) && !StringUtils.isNumeric(urlSlug);
    }

    private boolean isOwnReadmeArticle(long columnId, Long articleId) {
        ColumnInfoDO column = columnDao.getById(columnId);
        return column != null
                && Objects.equals(column.getReadmeArticleId(), articleId);
    }

}
