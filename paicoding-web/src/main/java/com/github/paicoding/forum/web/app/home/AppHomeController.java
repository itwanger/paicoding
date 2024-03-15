package com.github.paicoding.forum.web.app.home;

import com.github.paicoding.forum.api.model.vo.PageListVo;
import com.github.paicoding.forum.api.model.vo.PageParam;
import com.github.paicoding.forum.api.model.vo.ResVo;
import com.github.paicoding.forum.api.model.vo.article.dto.ArticleDTO;
import com.github.paicoding.forum.api.model.vo.article.dto.CategoryDTO;
import com.github.paicoding.forum.web.app.home.extend.AppHomeServiceExtend;
import com.github.paicoding.forum.web.app.home.vo.RecommendTopicVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * app 首页
 * 1. 类目列表
 * 2. 轮播图
 * 3. 热门文章
 * 4. 文章列表
 *
 * @author YiHui
 * @date 2024/3/14
 */
@Api(tags = {"APP首页"}, value = "app首页接口")
@RestController
@RequestMapping(path = "app/home")
public class AppHomeController {
    @Resource
    private AppHomeServiceExtend appHomeServiceExtend;

    /**
     * 返回类目列表
     *
     * @param all  true 标识查询所有的类目； false 表示查询有文章的类目
     * @param size 返回标签的数量，默认4个
     * @return
     */
    @ApiOperation(tags = "APP首页", value = "类目列表", notes = "根据查询条件，返回有文章/所有的类目信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "all", value = "true 标识查询所有的类目； false 表示查询有文章的类目", required = false, defaultValue = "true"),
            @ApiImplicitParam(name = "size", value = "返回标签的数量，默认4个", required = false, defaultValue = "4"),
    })
    @GetMapping(path = "category")
    public ResVo<List<CategoryDTO>> categoryList(@RequestParam(required = false, name = "all") Boolean all,
                                                 @RequestParam(required = false, name = "size", defaultValue = "4") Integer size) {
        if (all == null) {
            all = true;
        }
        if (!all && size == null) {
            size = 4;
        }
        return ResVo.ok(appHomeServiceExtend.listCategory(all, size));
    }

    @ApiOperation(tags = "APP首页", value = "热门专题", notes = "首页的专题列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "size", value = "null 表示查询所有"),
    })
    @GetMapping(path = "topic")
    public ResVo<List<RecommendTopicVo>> recommendTopic(@RequestParam(required = false, name = "size") Integer size) {
        return ResVo.ok(appHomeServiceExtend.queryRecommendTopic(size));
    }


    @ApiOperation(tags = "APP首页", value = "轮播", notes = "首页推荐的轮播文章")
    @GetMapping(path = "top")
    public ResVo<List<ArticleDTO>> topArticles(@RequestParam(name = "category", required = false) Long category) {
        List<ArticleDTO> list = appHomeServiceExtend.queryTopArticleList(category);
        return ResVo.ok(list);
    }

    @ApiOperation(tags = "APP首页", value = "文章列表", notes = "文章列表")
    @GetMapping(path = "articles")
    public ResVo<PageListVo<ArticleDTO>> categoryDataList(@RequestParam(name = "category", required = false) Long categoryId,
                                                          @RequestParam(name = "page", required = false) Long page,
                                                          @RequestParam(name = "size", required = false) Long size) {
        PageParam pageParam = PageParam.buildPageParam(page, size);
        PageListVo<ArticleDTO> list = appHomeServiceExtend.queryArticleList(categoryId, pageParam);
        return ResVo.ok(list);
    }

}
