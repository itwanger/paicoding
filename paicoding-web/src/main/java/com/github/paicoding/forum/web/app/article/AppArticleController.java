package com.github.paicoding.forum.web.app.article;

import com.github.paicoding.forum.api.model.enums.article.ArticleRankTypeEnum;
import com.github.paicoding.forum.api.model.vo.PageListVo;
import com.github.paicoding.forum.api.model.vo.PageParam;
import com.github.paicoding.forum.api.model.vo.ResVo;
import com.github.paicoding.forum.api.model.vo.article.dto.ArticleDTO;
import com.github.paicoding.forum.api.model.vo.comment.dto.CurrentCommentDTO;
import com.github.paicoding.forum.service.article.service.ArticleReadService;
import com.github.paicoding.forum.service.comment.service.AppCommentReadService;
import com.github.paicoding.forum.web.app.article.extend.AppArticleServiceExtend;
import com.github.paicoding.forum.web.front.article.vo.ArticleDetailVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author YiHui
 * @date 2024/3/14
 */
@Api(tags = {"APP文章"}, value = "app文章详情相关接口")
@RestController
@RequestMapping(path = "app/article")
public class AppArticleController {
    @Resource
    private ArticleReadService articleReadService;

    @Resource
    private AppArticleServiceExtend appArticleServiceExtend;

    @Resource
    private AppCommentReadService appCommentReadService;

    /**
     * 排行棒列表
     *
     * @param sort 排行类型
     * @param page 当前页
     * @param size 每页的数量
     * @return
     */
    @RequestMapping(path = "rank")
    @ApiOperation(tags = "APP文章", value = "文章排行列表", notes = "文章排行列表")
    public ResVo<PageListVo<ArticleDTO>> rankList(@RequestParam(name = "sort", required = false) String sort,
                                                  @RequestParam(name = "page", required = false) Long page,
                                                  @RequestParam(name = "size", required = false) Long size) {
        ArticleRankTypeEnum type = ArticleRankTypeEnum.typeOf(sort);
        if (type == null) type = ArticleRankTypeEnum.READ_COUNT;
        PageParam pageParam = PageParam.buildPageParam(page, size);
        return ResVo.ok(articleReadService.queryRankList(type, pageParam));
    }

    @RequestMapping(path = "detail/{articleId}")
    @ApiOperation(tags = "APP文章", value = "文章详情", notes = "文章详情")
    public ResVo<ArticleDetailVo> detail(@PathVariable("articleId") Long articleId) {
        return ResVo.ok(appArticleServiceExtend.queryDetail(articleId));
    }

    @RequestMapping(path = "comment/{articleId}")
    @ApiOperation(tags = "APP文章", value = "文章评论", notes = "文章评论")
    public ResVo<PageListVo<CurrentCommentDTO>> comments(@PathVariable("articleId") Long articleId,
                                                         @RequestParam(name = "page", required = false) Long page,
                                                         @RequestParam(name = "size", required = false) Long size) {
        PageParam pageParam = PageParam.buildPageParam(page, size);
        List<CurrentCommentDTO> list = appCommentReadService.queryLatestComments(articleId, pageParam);
        return ResVo.ok(PageListVo.newVo(list, pageParam.getPageSize()));
    }
}
