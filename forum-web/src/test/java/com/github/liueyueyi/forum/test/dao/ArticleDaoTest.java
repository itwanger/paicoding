package com.github.liueyueyi.forum.test.dao;

import com.github.liueyueyi.forum.api.model.vo.PageParam;
import com.github.liueyueyi.forum.api.model.vo.article.dto.ArticleListDTO;
import com.github.liueyueyi.forum.test.BasicTest;
import com.github.liuyueyi.forum.service.article.repository.dao.CategoryDao;
import com.github.liuyueyi.forum.service.article.repository.dao.TagDao;
import com.github.liuyueyi.forum.service.article.repository.entity.CategoryDO;
import com.github.liuyueyi.forum.service.article.repository.entity.TagDO;
import com.github.liuyueyi.forum.service.article.service.ArticleReadService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author YiHui
 * @date 2022/7/20
 */
@Slf4j
public class ArticleDaoTest extends BasicTest {

    @Autowired
    private TagDao tagDao;

    @Autowired
    private CategoryDao categoryDao;

    @Autowired
    private ArticleReadService articleService;

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
        tag.setCategoryId(1L);
        tagDao.save(tag);
        log.info("tagId: {}", tag.getId());
    }

    @Test
    public void testArticle() {
        ArticleListDTO articleListDTO = articleService.queryArticlesByCategory(1L, PageParam.newPageInstance(1L, 10L));
        log.info("articleListDTO: {}", articleListDTO);
    }

}
