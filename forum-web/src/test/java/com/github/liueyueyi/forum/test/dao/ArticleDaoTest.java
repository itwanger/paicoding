package com.github.liueyueyi.forum.test.dao;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.liueyueyi.forum.test.BasicTest;
import com.github.liuyueyi.forum.core.model.req.PageParam;
import com.github.liuyueyi.forum.service.article.impl.CategoryServiceImpl;
import com.github.liuyueyi.forum.service.article.impl.TagServiceImpl;
import com.github.liuyueyi.forum.service.article.dto.TagDTO;
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

    @Test
    public void testCategory() {
        CategoryDO category = new CategoryDO();
        category.setCategoryName("后端");
        category.setStatus(1);
        Long categoryId = categoryService.addCategory(category);
        log.info("save category:{} -> id:{}", category, categoryId);

        IPage<CategoryDO> list = categoryService.getCategoryByPage(PageParam.newPageInstance(0L, 10L));
        log.info("query list: {}", list.getRecords());

    }

    @Test
    public void testTag() {
        TagDO tag = new TagDO();
        tag.setTagName("Java");
        tag.setTagType(1);
        tag.setCategoryId(1L);
        Long tagId = tagService.addTag(tag);
        log.info("tagId: {}", tagId);

        List<TagDTO> list = tagService.getTagListByCategoryId(1L);
        log.info("tagList: {}", list);
    }

    @Test
    public void testArticle() {

    }

}
