package com.github.liuyueyi.forum.web.front.article.rest;

import com.github.liueyueyi.forum.api.model.context.ReqInfoContext;
import com.github.liueyueyi.forum.api.model.enums.DocumentTypeEnum;
import com.github.liueyueyi.forum.api.model.enums.OperateTypeEnum;
import com.github.liueyueyi.forum.api.model.vo.ResVo;
import com.github.liueyueyi.forum.api.model.vo.article.dto.CategoryDTO;
import com.github.liueyueyi.forum.api.model.vo.article.dto.TagDTO;
import com.github.liueyueyi.forum.api.model.vo.constants.StatusEnum;
import com.github.liuyueyi.forum.core.permission.Permission;
import com.github.liuyueyi.forum.core.permission.UserRole;
import com.github.liuyueyi.forum.service.article.repository.entity.ArticleDO;
import com.github.liuyueyi.forum.service.article.service.ArticleReadService;
import com.github.liuyueyi.forum.service.article.service.CategoryService;
import com.github.liuyueyi.forum.service.article.service.TagService;
import com.github.liuyueyi.forum.service.user.service.UserFootService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 返回json格式数据
 *
 * @author YiHui
 * @date 2022/9/2
 */
@RequestMapping(path = "article/api")
@RestController
public class ArticleRestController {
    @Autowired
    private ArticleReadService articleService;
    @Autowired
    private UserFootService userFootService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private TagService tagService;


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

        List<TagDTO> list = tagService.queryTagsByCategoryId(categoryId);
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
        List<CategoryDTO> list = categoryService.loadAllCategories();
        list.forEach(c -> c.setSelected(c.getCategoryId().equals(categoryId)));
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
