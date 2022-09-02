package com.github.liueyueyi.forum.test.dao;

import com.github.liueyueyi.forum.api.model.vo.PageParam;
import com.github.liueyueyi.forum.test.BasicTest;
import com.github.liueyueyi.forum.api.model.vo.article.dto.ArticleListDTO;
import com.github.liuyueyi.forum.service.article.service.impl.ArticleReadServiceImpl;
import com.github.liuyueyi.forum.service.article.service.impl.CategoryServiceImpl;
import com.github.liuyueyi.forum.service.article.service.impl.TagServiceImpl;
import com.github.liueyueyi.forum.api.model.vo.article.dto.TagDTO;
import com.github.liuyueyi.forum.service.article.repository.entity.CategoryDO;
import com.github.liuyueyi.forum.service.article.repository.entity.TagDO;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * @author YiHui
 * @date 2022/7/20
 */
@Slf4j
public class ArticleDaoTest extends BasicTest {

    @Autowired
    private TagServiceImpl tagService;

    @Autowired
    private CategoryServiceImpl categoryService;

    @Autowired
    private ArticleReadServiceImpl articleService;

    @Test
    public void testCategory() {
        CategoryDO category = new CategoryDO();
        category.setCategoryName("后端");
        category.setStatus(1);
        categoryService.save(category);
        log.info("save category:{} -> id:{}", category, category.getId());
    }

    @Test
    public void testTag() {
        TagDO tag = new TagDO();
        tag.setTagName("Java");
        tag.setTagType(1);
        tag.setCategoryId(1L);
        tagService.save(tag);
        log.info("tagId: {}", tag.getId());

        List<TagDTO> list = tagService.queryTagsByCategoryId(1L);
        log.info("tagList: {}", list);
    }

    @Test
    public void testArticle() {
        ArticleListDTO articleListDTO = articleService.getCollectionArticleListByUserId(1L, PageParam.newPageInstance(1L, 10L));
        log.info("articleListDTO: {}", articleListDTO);
    }

}
