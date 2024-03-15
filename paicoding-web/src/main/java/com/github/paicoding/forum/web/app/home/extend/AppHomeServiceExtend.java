package com.github.paicoding.forum.web.app.home.extend;

import com.github.paicoding.forum.api.model.vo.PageListVo;
import com.github.paicoding.forum.api.model.vo.PageParam;
import com.github.paicoding.forum.api.model.vo.article.dto.ArticleDTO;
import com.github.paicoding.forum.api.model.vo.article.dto.CategoryDTO;
import com.github.paicoding.forum.core.common.CommonConstants;
import com.github.paicoding.forum.service.article.service.ArticleReadService;
import com.github.paicoding.forum.service.article.service.CategoryService;
import com.github.paicoding.forum.web.app.home.vo.RecommendTopicVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 扩展服务
 *
 * @author YiHui
 * @date 2024/3/14
 */
@Service
public class AppHomeServiceExtend {
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private ArticleReadService articleService;

    /**
     * 获取分类信息
     *
     * @param loadAll
     * @param size
     * @return
     */
    public List<CategoryDTO> listCategory(boolean loadAll, int size) {
        List<CategoryDTO> list = categoryService.loadAllCategories();
        if (!loadAll) {
            // 查询所有分类的对应的文章数
            Map<Long, Long> articleCnt = articleService.queryArticleCountsByCategory();
            // 过滤掉文章数为0的分类
            list.removeIf(c -> articleCnt.getOrDefault(c.getCategoryId(), 0L) <= 0L);
        }
        if (!loadAll && list.size() > size) {
            list = list.subList(0, size);
        }
        return list;
    }


    /**
     * 获取推荐专栏
     * - 默认选择四个分类作为首页的推荐的专栏
     *
     * @return
     */
    public List<RecommendTopicVo> queryRecommendTopic(Integer size) {
        boolean queryAll = size == null;
        List<CategoryDTO> categoryList = listCategory(queryAll, queryAll ? Integer.MAX_VALUE: size);
        return categoryList.stream().map(category -> {
            RecommendTopicVo vo = new RecommendTopicVo();
            vo.setId(category.getCategoryId());
            vo.setTopic(category.getCategory());
            vo.setDesc("");
            vo.setCover(getCategoryCover(category.getCategory()));
            vo.setTopicType("category");
            return vo;
        }).collect(Collectors.toList());
    }

    private String getCategoryCover(String category) {
        List<String> list = CommonConstants.HOMEPAGE_TOP_PIC_MAP.getOrDefault(category,
                CommonConstants.HOMEPAGE_TOP_PIC_MAP.get(CommonConstants.CATEGORY_ALL));
        return list.get(0);
    }


    /**
     * 置顶top 文章列表
     */
    public List<ArticleDTO> queryTopArticleList(Long categoryId) {
        List<ArticleDTO> topArticles = articleService.queryTopArticlesByCategory(categoryId);
        return topArticles;
    }

    /**
     * 文章列表
     *
     * @param categoryId
     * @param page
     * @return
     */
    public PageListVo<ArticleDTO> queryArticleList(Long categoryId, PageParam page) {
        PageListVo<ArticleDTO> list = articleService.queryArticlesByCategory(categoryId, page);
        return list;
    }
}
