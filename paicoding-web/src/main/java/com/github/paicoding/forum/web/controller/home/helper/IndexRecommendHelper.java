package com.github.paicoding.forum.web.controller.home.helper;

import com.github.paicoding.forum.api.model.vo.PageParam;
import com.github.paicoding.forum.api.model.vo.article.dto.ArticleDTO;
import com.github.paicoding.forum.api.model.vo.article.dto.CategoryDTO;
import com.github.paicoding.forum.core.common.CommonConstants;
import com.github.paicoding.forum.service.article.service.ArticleReadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 首页推荐相关
 *
 * @author XuYifei
 * @date 2024-07-12
 */
@Component
public class IndexRecommendHelper {

    @Autowired
    private ArticleReadService articleService;

    /**
     * 置顶top 文章列表
     */
    public List<ArticleDTO> topArticleList(CategoryDTO category) {
        List<ArticleDTO> topArticles = articleService.queryTopArticlesByCategory(category.getCategoryId() == 0 ? null : category.getCategoryId());
        if (topArticles.size() < PageParam.TOP_PAGE_SIZE) {
            // 当分类下文章数小于置顶数时，为了避免显示问题，直接不展示
            topArticles.clear();
            return topArticles;
        }

        // 查询分类对应的头图列表
        List<String> topPicList = CommonConstants.HOMEPAGE_TOP_PIC_MAP.getOrDefault(category.getCategory(),
                CommonConstants.HOMEPAGE_TOP_PIC_MAP.get(CommonConstants.CATEGORY_ALL));

        // 替换头图，下面做了一个数组越界的保护，避免当topPageSize数量变大，但是默认的cover图没有相应增大导致数组越界异常
        AtomicInteger index = new AtomicInteger(0);
        topArticles.forEach(s -> s.setCover(topPicList.get(index.getAndIncrement() % topPicList.size())));
        return topArticles;
    }


}
