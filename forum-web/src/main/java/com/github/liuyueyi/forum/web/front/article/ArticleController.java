package com.github.liuyueyi.forum.web.front.article;

import com.github.liueyueyi.forum.api.model.context.ReqInfoContext;
import com.github.liueyueyi.forum.api.model.enums.DocumentTypeEnum;
import com.github.liueyueyi.forum.api.model.enums.OperateTypeEnum;
import com.github.liueyueyi.forum.api.model.vo.PageParam;
import com.github.liueyueyi.forum.api.model.vo.ResVo;
import com.github.liueyueyi.forum.api.model.vo.article.ArticlePostReq;
import com.github.liueyueyi.forum.api.model.vo.article.dto.ArticleDTO;
import com.github.liueyueyi.forum.api.model.vo.article.dto.CategoryDTO;
import com.github.liueyueyi.forum.api.model.vo.article.dto.TagDTO;
import com.github.liueyueyi.forum.api.model.vo.comment.dto.TopCommentDTO;
import com.github.liueyueyi.forum.api.model.vo.constants.StatusEnum;
import com.github.liueyueyi.forum.api.model.vo.user.dto.UserHomeDTO;
import com.github.liuyueyi.forum.core.permission.Permission;
import com.github.liuyueyi.forum.core.permission.UserRole;
import com.github.liuyueyi.forum.service.article.ArticleService;
import com.github.liuyueyi.forum.service.article.CategoryService;
import com.github.liuyueyi.forum.service.article.TagService;
import com.github.liuyueyi.forum.service.article.repository.entity.ArticleDO;
import com.github.liuyueyi.forum.service.comment.CommentService;
import com.github.liuyueyi.forum.service.user.UserFootService;
import com.github.liuyueyi.forum.service.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 文章
 * todo: 所有的入口都放在一个Controller，会导致功能划分非常混乱
 * ： 文章列表
 * ： 文章编辑
 * ： 文章详情
 * ---
 *  - 返回视图 view
 *  - 返回json数据
 */
@Controller
@RequestMapping(path = "article")
public class ArticleController {
    @Autowired
    private ArticleService articleService;

    @Autowired
    private UserFootService userFootService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private TagService tagService;

    @Autowired
    private UserService userService;

    @Autowired
    private CommentService commentService;

    /**
     * 文章编辑页
     *
     * @param articleId
     * @return
     */
    @Permission(role = UserRole.LOGIN)
    @GetMapping(path = "edit")
    public String edit(@RequestParam(required = false) Long articleId, Model model) {
        if (articleId != null) {
            ArticleDTO article = articleService.queryArticleDetail(articleId);
            if (!Objects.equals(article.getAuthor(), ReqInfoContext.getReqInfo().getUserId())) {
                // 没有权限
                model.addAttribute("toast", "内容不存在");
                return "redirect:403";
            }
            model.addAttribute("article", article);

            List<CategoryDTO> categoryList = categoryService.loadAllCategories(false);
            categoryList.forEach(s -> {
                s.setSelected(s.getCategoryId().equals(article.getCategory().getCategoryId()));
            });
            model.addAttribute("categories", categoryList);
            model.addAttribute("tagChooses", article.getTags());
            model.addAttribute("tags", tagService.getTagListByCategoryId(article.getCategory().getCategoryId()));
        } else {
            List<CategoryDTO> categoryList = categoryService.loadAllCategories(false);
            model.addAttribute("categories", categoryList);
            model.addAttribute("tags", Collections.emptyList());
        }

        return "biz/article/edit";
    }

    /**
     * 发布文章，完成后跳转到详情页
     * - 这里有一个重定向的知识点
     * - fixme 博文：* [5.请求重定向 | 一灰灰Learning](https://hhui.top/spring-web/02.response/05.190929-springboot%E7%B3%BB%E5%88%97%E6%95%99%E7%A8%8Bweb%E7%AF%87%E4%B9%8B%E9%87%8D%E5%AE%9A%E5%90%91/)
     *
     * @return
     */
    @Permission(role = UserRole.LOGIN)
    @PostMapping(path = "post")
    @ResponseBody
    @Transactional(rollbackFor = Exception.class)
    public ResVo<Long> post(@RequestBody ArticlePostReq req, HttpServletResponse response) throws IOException {
        Long id = articleService.saveArticle(req);
//        return "redirect:/article/detail/" + id;
//        response.sendRedirect("/article/detail/" + id);
        // 这里采用前端重定向策略
        return ResVo.ok(id);
    }

    /**
     * 文章详情页
     * - 参数解析知识点
     * - fixme * [1.Get请求参数解析姿势汇总 | 一灰灰Learning](https://hhui.top/spring-web/01.request/01.190824-springboot%E7%B3%BB%E5%88%97%E6%95%99%E7%A8%8Bweb%E7%AF%87%E4%B9%8Bget%E8%AF%B7%E6%B1%82%E5%8F%82%E6%95%B0%E8%A7%A3%E6%9E%90%E5%A7%BF%E5%8A%BF%E6%B1%87%E6%80%BB/)
     *
     * @param articleId
     * @return
     */
    @GetMapping("detail/{articleId}")
    public String detail(@PathVariable(name = "articleId") Long articleId, Model model) {
        ArticleDTO articleDTO = articleService.queryTotalArticleDetail(articleId, ReqInfoContext.getReqInfo().getUserId());
        model.addAttribute("article", articleDTO);

        // 评论信息
        List<TopCommentDTO> comments = commentService.getArticleComments(articleId, PageParam.newPageInstance(1L, 10L));
        model.addAttribute("comments", comments);

        // 作者信息
        UserHomeDTO user = userService.getUserHomeDTO(articleDTO.getAuthor());
        articleDTO.setAuthorName(user.getUserName());
        model.addAttribute("author", user);
        return "biz/article/detail";
    }

    /**
     * 查询所有的标签
     *
     * @return
     */
    @ResponseBody
    @GetMapping(path = "tag/list")
    public ResVo<List<TagDTO>> queryTags(Long categoryId) {
        if (categoryId == null || categoryId <= 0L) {
            return ResVo.fail(StatusEnum.ILLEGAL_ARGUMENTS, categoryId);
        }

        List<TagDTO> list = tagService.getTagListByCategoryId(categoryId);
        return ResVo.ok(list);
    }

    /**
     * 获取所有的分类
     *
     * @return
     */
    @ResponseBody
    @GetMapping(path = "category/list")
    public ResVo<List<CategoryDTO>> getCategoryList(@RequestParam(name = "categoryId", required = false) Long categoryId) {
        List<CategoryDTO> list = categoryService.loadAllCategories(false);
        if (categoryId != null) {
            list.forEach(c -> c.setSelected(c.getCategoryId().equals(categoryId)));
        }
        return ResVo.ok(list);
    }


    /**
     * 收藏、点赞等相关操作
     *
     * @param articleId
     * @param type      取值来自于 OperateTypeEnum#code
     * @return
     */
    @ResponseBody
    @Permission(role = UserRole.LOGIN)
    @GetMapping(path = "favor")
    public ResVo<Boolean> favor(@RequestParam(name = "articleId") Long articleId,
                                @RequestParam(name = "type") Integer type) {
        OperateTypeEnum operate = OperateTypeEnum.fromCode(type);
        if (operate == OperateTypeEnum.EMPTY) {
            return ResVo.fail(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, type + "非法");
        }

        // 要求文章必须存在
        ArticleDO article = articleService.queryBasicArticle(articleId);
        if (article == null) {
            return ResVo.fail(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "文章不存在!");
        }

        userFootService.saveOrUpdateUserFoot(DocumentTypeEnum.ARTICLE, articleId, article.getUserId(),
                ReqInfoContext.getReqInfo().getUserId(),
                operate);
        return ResVo.ok(true);
    }
}
