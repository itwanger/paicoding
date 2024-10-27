package com.github.paicoding.forum.web.controller.article.rest;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.paicoding.forum.api.model.context.ReqInfoContext;
import com.github.paicoding.forum.api.model.enums.column.ColumnArticleReadEnum;
import com.github.paicoding.forum.api.model.enums.column.ColumnTypeEnum;
import com.github.paicoding.forum.api.model.enums.user.UserAIStatEnum;
import com.github.paicoding.forum.api.model.vo.PageParam;
import com.github.paicoding.forum.api.model.vo.article.dto.*;
import com.github.paicoding.forum.api.model.vo.comment.dto.TopCommentDTO;
import com.github.paicoding.forum.api.model.vo.recommend.SideBarDTO;
import com.github.paicoding.forum.core.util.SpringUtil;
import com.github.paicoding.forum.service.article.repository.entity.ColumnArticleDO;
import com.github.paicoding.forum.service.article.service.ArticleReadService;
import com.github.paicoding.forum.service.article.service.ColumnService;
import com.github.paicoding.forum.service.comment.service.CommentReadService;
import com.github.paicoding.forum.service.sidebar.service.SidebarService;
import com.github.paicoding.forum.web.config.GlobalViewConfig;
import com.github.paicoding.forum.web.controller.article.vo.ColumnVo;
import com.github.paicoding.forum.web.global.SeoInjectService;
import com.github.paicoding.forum.web.global.vo.ResultVo;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author XuYifei
 * @date 2024-07-12
 */
@RestController
@RequestMapping(path = "column/api")
public class ColumnRestController {
    @Autowired
    private ColumnService columnService;

    @Autowired
    private ArticleReadService articleReadService;

    @Autowired
    private CommentReadService commentReadService;

    @Autowired
    private SidebarService sidebarService;

    @Resource
    private GlobalViewConfig globalViewConfig;

    /**
     * 专栏主页，展示专栏列表
     *
     * @param page
     * @param size
     * @return
     */
    @GetMapping(path = {"/", "", "home"})
    public ResultVo<ColumnVo> listByPage(@RequestParam(value = "page", required = false, defaultValue = "1") Long page,
                                   @RequestParam(name = "size", required = false, defaultValue = "10") Long size) {
        IPage<ColumnDTO> columns = columnService.listColumnByPage(page, size);
        List<SideBarDTO> sidebars = sidebarService.queryColumnSidebarList();
        ColumnVo vo = new ColumnVo();
        vo.setColumnPage(columns);
        vo.setSideBarItems(sidebars);
        return ResultVo.ok(vo);
    }

    /**
     * 文章与专栏的对应关系
     * @param articleId
     * @return
     */
    @GetMapping("article/{articleId}")
    public ResultVo<ColumnArticleDO> article(@PathVariable("articleId") Long articleId) {
        // 针对专栏文章，给出一个映射关系
        ColumnArticleDO columnArticle = columnService.getColumnArticleRelation(articleId);
        return ResultVo.ok(columnArticle);
    }

    /**
     * 专栏的文章阅读界面
     *
     * @param columnId 专栏id
     * @param section  节数，从1开始
     * @param model
     * @return
     */
    @GetMapping(path = "{columnId}/{section}")
    public ResultVo<ColumnArticlesDTO> articles(@PathVariable("columnId") Long columnId, @PathVariable("section") Integer section, Model model) {
        if (section <= 0) section = 1;
        // 查询专栏
        ColumnDTO column = columnService.queryBasicColumnInfo(columnId);

        ColumnArticleDO columnArticle = columnService.queryColumnArticle(columnId, section);
        Long articleId = columnArticle.getArticleId();
        // 文章信息
        ArticleDTO articleDTO = articleReadService.queryFullArticleInfo(articleId, ReqInfoContext.getReqInfo().getUserId());
        // 返回html格式的文档内容
        articleDTO.setContent(articleDTO.getContent());
        // 评论信息
        List<TopCommentDTO> comments = commentReadService.getArticleComments(articleId, PageParam.newPageInstance());

        // 热门评论
        TopCommentDTO hotComment = commentReadService.queryHotComment(articleId);

        // 文章列表
        List<SimpleArticleDTO> articles = columnService.queryColumnArticles(columnId);

        ColumnArticlesDTO vo = new ColumnArticlesDTO();
        vo.setArticle(articleDTO);
        vo.setComments(comments);
        vo.setHotComment(hotComment);
        vo.setColumn(columnId);
        vo.setSection(section);
        vo.setArticleList(articles);


        ArticleOtherDTO other = new ArticleOtherDTO();

        // 教程类型
        updateReadType(other, column, articleDTO, ColumnArticleReadEnum.valueOf(columnArticle.getReadType()));


        // 把是文章翻页的参数封装到这里
        // prev 的 href 和 是否显示的 flag
        ColumnArticleFlipDTO flip = new ColumnArticleFlipDTO();
        flip.setPrevHref("/column/" + columnId + "/" + (section - 1));
        flip.setPrevShow(section > 1);
        // next 的 href 和 是否显示的 flag
        flip.setNextHref("/column/" + columnId + "/" + (section + 1));
        flip.setNextShow(section < articles.size());
        other.setFlip(flip);

        // 放入 model 中
        vo.setOther(other);
        model.addAttribute("vo", vo);

        // 更新seo信息
        SpringUtil.getBean(SeoInjectService.class).initColumnSeo(vo, column);
        return ResultVo.ok(vo);
    }

    /**
     * 对于要求登录阅读的文章进行进行处理
     *
     * @param vo
     * @param column
     * @param articleDTO
     */
    private void updateReadType(ArticleOtherDTO vo, ColumnDTO column, ArticleDTO articleDTO, ColumnArticleReadEnum articleReadEnum) {
        Long loginUser = ReqInfoContext.getReqInfo().getUserId();
        if (loginUser != null && loginUser.equals(articleDTO.getAuthor())) {
            vo.setReadType(ColumnTypeEnum.FREE.getType());
            return;
        }

        if (articleReadEnum == ColumnArticleReadEnum.COLUMN_TYPE) {
            // 专栏中的文章，没有特殊指定时，直接沿用专栏的规则
            if (column.getType() == ColumnTypeEnum.TIME_FREE.getType()) {
                long now = System.currentTimeMillis();
                if (now > column.getFreeEndTime() || now < column.getFreeStartTime()) {
                    vo.setReadType(ColumnTypeEnum.LOGIN.getType());
                } else {
                    vo.setReadType(ColumnTypeEnum.FREE.getType());
                }
            } else {
                vo.setReadType(column.getType());
            }
        } else {
            // 直接使用文章特殊设置的规则
            vo.setReadType(articleReadEnum.getRead());
        }
        // 如果是星球 or 登录阅读时，不返回全量的文章内容
        articleDTO.setContent(trimContent(vo.getReadType(), articleDTO.getContent()));
        // fix 关于 cover 封面，文章详情的前端已经不显示了，这里直接删除
    }

    /**
     * 文章内容隐藏
     *
     * @param readType
     * @param content
     * @return
     */
    private String trimContent(int readType, String content) {
        if (readType == ColumnTypeEnum.STAR_READ.getType()) {
            // 判断登录用户是否绑定了星球，如果是，则直接阅读完整的专栏内容
            if (ReqInfoContext.getReqInfo().getUser() != null && ReqInfoContext.getReqInfo().getUser().getStarStatus() == UserAIStatEnum.FORMAL) {
                return content;
            }

            // 如果没有绑定星球，则返回 10% 的内容
            // 10% 从全局的配置参数中获取
            int count = Integer.parseInt(globalViewConfig.getZsxqArticleReadCount());
            return content.substring(0, content.length() * count / 100);
        }

        if ((readType == ColumnTypeEnum.LOGIN.getType() && ReqInfoContext.getReqInfo().getUserId() == null)) {
            // 如果是登录阅读，但是用户没有登录，则返回 20% 的内容
            int count = Integer.parseInt(globalViewConfig.getNeedLoginArticleReadCount());
            return content.substring(0, content.length() * count / 100);
        }

        return content;
    }
}
