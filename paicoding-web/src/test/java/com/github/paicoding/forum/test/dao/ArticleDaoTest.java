package com.github.paicoding.forum.test.dao;

import com.github.paicoding.forum.api.model.vo.PageListVo;
import com.github.paicoding.forum.api.model.vo.PageParam;
import com.github.paicoding.forum.api.model.vo.article.dto.ArticleDTO;
import com.github.paicoding.forum.api.model.vo.article.dto.TagDTO;
import com.github.paicoding.forum.service.article.service.TagSettingService;
import com.github.paicoding.forum.test.BasicTest;
import com.github.paicoding.forum.service.article.repository.dao.CategoryDao;
import com.github.paicoding.forum.service.article.repository.dao.TagDao;
import com.github.paicoding.forum.service.article.repository.entity.CategoryDO;
import com.github.paicoding.forum.service.article.repository.entity.TagDO;
import com.github.paicoding.forum.service.article.service.ArticleReadService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author XuYifei
 * @date 2024-07-12
 */
@Slf4j
public class ArticleDaoTest extends BasicTest {

    @Autowired
    private TagDao tagDao;

    @Autowired
    private CategoryDao categoryDao;

    @Autowired
    private ArticleReadService articleService;

    @Autowired
    private TagSettingService tagSettingService;

    @Test
    public void testMysqlRedisSyc() {
        TagDTO tagDTO = tagSettingService.getTagById(1L);
        log.info("getTagById:{}", tagDTO);
    }

    @Test
    public void testCategory() {
        CategoryDO category = new CategoryDO();
        category.setCategoryName("后端");
        category.setStatus(1);
        categoryDao.save(category);
        log.info("save category:{} -> id:{}", category, category.getId());
    }

    @Test
    public void testTag() {
        TagDO tag = new TagDO();
        tag.setTagName("Java");
        tag.setTagType(1);
//        tag.setCategoryId(1L);
        tagDao.save(tag);
        log.info("tagId: {}", tag.getId());
    }

    @Test
    public void testArticle() {
        PageListVo<ArticleDTO> articleListDTO = articleService.queryArticlesByCategory(1L, PageParam.newPageInstance(1L, 10L));
        log.info("articleListDTO: {}", articleListDTO);
    }

}
