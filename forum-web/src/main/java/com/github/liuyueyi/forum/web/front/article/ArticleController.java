package com.github.liuyueyi.forum.web.front.article;

import com.github.liueyueyi.forum.api.model.vo.ResVo;
import com.github.liueyueyi.forum.api.model.vo.article.ArticlePostReq;
import com.github.liueyueyi.forum.api.model.vo.constants.StatusEnum;
import com.github.liuyueyi.forum.service.article.ArticleService;
import com.github.liuyueyi.forum.service.article.CategoryService;
import com.github.liuyueyi.forum.service.article.TagService;
import com.github.liuyueyi.forum.service.article.dto.ArticleDTO;
import com.github.liuyueyi.forum.service.article.dto.CategoryDTO;
import com.github.liuyueyi.forum.service.article.dto.TagDTO;
import com.github.liuyueyi.forum.service.user.UserService;
import com.github.liuyueyi.forum.service.user.dto.UserHomeDTO;
import com.github.liuyueyi.forum.service.user.repository.entity.UserInfoDO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

/**
 * 文章
 */
@Controller
@RequestMapping(path = "article")
public class ArticleController {
    @Autowired
    private ArticleService articleService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private TagService tagService;

    @Autowired
    private UserService userService;

    /**
     * 文章编辑页
     *
     * @param articleId
     * @return
     */
    @GetMapping(path = "edit")
    public String edit(@RequestParam(required = false) Long articleId, Model model) {
        if (articleId != null) {
            ArticleDTO article = articleService.queryArticleDetail(articleId);
            model.addAttribute("article", article);

            List<CategoryDTO> categoryList = categoryService.loadAllCategories(false);
            categoryList.forEach(s -> {
                s.setSelected(s.getCategoryId().equals(article.getArticleId()));
            });
            model.addAttribute("categories", categoryList);
            model.addAttribute("tags", article.getTags());
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
     * - 博文：* [5.请求重定向 | 一灰灰Learning](https://hhui.top/spring-web/02.response/05.190929-springboot%E7%B3%BB%E5%88%97%E6%95%99%E7%A8%8Bweb%E7%AF%87%E4%B9%8B%E9%87%8D%E5%AE%9A%E5%90%91/)
     *
     * @return
     */
    @PostMapping(path = "post")
    public String post(@RequestBody ArticlePostReq req) {
        Long id = articleService.saveArticle(req);
        return "redirect:/article/detail/" + id;
    }

    /**
     * 文章详情页
     * - 参数解析知识点
     * - * [1.Get请求参数解析姿势汇总 | 一灰灰Learning](https://hhui.top/spring-web/01.request/01.190824-springboot%E7%B3%BB%E5%88%97%E6%95%99%E7%A8%8Bweb%E7%AF%87%E4%B9%8Bget%E8%AF%B7%E6%B1%82%E5%8F%82%E6%95%B0%E8%A7%A3%E6%9E%90%E5%A7%BF%E5%8A%BF%E6%B1%87%E6%80%BB/)
     *
     * @param articleId
     * @return
     */
    @GetMapping("detail/{articleId}")
    public String detail(@PathVariable(name = "articleId") Long articleId, Model model) {
        ArticleDTO articleDTO = articleService.queryArticleDetail(articleId);
        model.addAttribute("article", articleDTO);

        // 作者信息
        UserHomeDTO user = userService.getUserHomeDTO(articleDTO.getAuthor());
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

}
