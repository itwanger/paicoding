package com.github.paicoding.forum.web.admin.rest;

import com.github.paicoding.forum.api.model.enums.PushStatusEnum;
import com.github.paicoding.forum.api.model.vo.PageParam;
import com.github.paicoding.forum.api.model.vo.PageVo;
import com.github.paicoding.forum.api.model.vo.ResVo;
import com.github.paicoding.forum.api.model.vo.article.ColumnArticleReq;
import com.github.paicoding.forum.api.model.vo.article.ColumnReq;
import com.github.paicoding.forum.api.model.vo.article.dto.ColumnArticleDTO;
import com.github.paicoding.forum.api.model.vo.article.dto.ColumnDTO;
import com.github.paicoding.forum.api.model.vo.article.dto.SimpleArticleDTO;
import com.github.paicoding.forum.api.model.vo.article.dto.SimpleColumnDTO;
import com.github.paicoding.forum.api.model.vo.constants.StatusEnum;
import com.github.paicoding.forum.core.permission.Permission;
import com.github.paicoding.forum.core.permission.UserRole;
import com.github.paicoding.forum.core.util.NumUtil;
import com.github.paicoding.forum.service.article.repository.entity.ArticleDO;
import com.github.paicoding.forum.service.article.service.ArticleReadService;
import com.github.paicoding.forum.service.article.service.ColumnSettingService;
import com.github.paicoding.forum.web.front.search.vo.SearchArticleVo;
import com.github.paicoding.forum.web.front.search.vo.SearchColumnVo;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 专栏后台
 *
 * @author LouZai
 * @date 2022/9/19
 */
@RestController
//@Permission(role = UserRole.LOGIN)
@Api(value = "专栏及专栏文章管理控制器", tags = "专栏管理")
@RequestMapping(path = {"api/admin/column/", "admin/column/"})
public class ColumnSettingRestController {

    @Autowired
    private ColumnSettingService columnSettingService;

    @Autowired
    private ArticleReadService articleReadService;

    @Permission(role = UserRole.ADMIN)
    @PostMapping(path = "saveColumn")
    public ResVo<String> saveColumn(@RequestBody ColumnReq req) {
        columnSettingService.saveColumn(req);
        return ResVo.ok("ok");
    }

    @Permission(role = UserRole.ADMIN)
    @PostMapping(path = "saveColumnArticle")
    public ResVo<String> saveColumnArticle(@RequestBody ColumnArticleReq req) {

        // 要求文章必须存在，且已经发布
        ArticleDO articleDO = articleReadService.queryBasicArticle(req.getArticleId());
        if (articleDO == null || articleDO.getStatus() == PushStatusEnum.OFFLINE.getCode()) {
            return ResVo.fail(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "文章不存在或未发布!");
        }

        columnSettingService.saveColumnArticle(req);
        return ResVo.ok("ok");
    }

    @Permission(role = UserRole.ADMIN)
    @PostMapping(path = "sortColumnArticle")
    public ResVo<String> sortColumnArticle(@RequestBody List<ColumnArticleReq> columnArticleReqs) {
        columnSettingService.sortColumnArticle(columnArticleReqs);
        return ResVo.ok("ok");
    }

    @Permission(role = UserRole.ADMIN)
    @GetMapping(path = "deleteColumn")
    public ResVo<String> deleteColumn(@RequestParam(name = "columnId") Integer columnId) {
        columnSettingService.deleteColumn(columnId);
        return ResVo.ok("ok");
    }

    @Permission(role = UserRole.ADMIN)
    @GetMapping(path = "deleteColumnArticle")
    public ResVo<String> deleteColumnArticle(@RequestParam(name = "id") Integer id) {
        columnSettingService.deleteColumnArticle(id);
        return ResVo.ok("ok");
    }


    @GetMapping(path = "listColumn")
    public ResVo<PageVo<ColumnDTO>> listColumn(@RequestParam(name = "pageNumber", required = false) Integer pageNumber,
                                               @RequestParam(name = "pageSize", required = false) Integer pageSize) {
        pageNumber = NumUtil.nullOrZero(pageNumber) ? 1 : pageNumber;
        pageSize = NumUtil.nullOrZero(pageSize) ? 10 : pageSize;
        PageVo<ColumnDTO> columnDTOPageVo = columnSettingService.listColumn(PageParam.newPageInstance(pageNumber, pageSize));
        return ResVo.ok(columnDTOPageVo);
    }

    @GetMapping(path = "listColumnArticle")
    public ResVo<PageVo<ColumnArticleDTO>> listColumnArticle(@RequestParam(name = "columnId") Integer columnId,
                                                             @RequestParam(name = "pageNumber", required = false) Integer pageNumber,
                                                             @RequestParam(name = "pageSize", required = false) Integer pageSize) throws Exception {
        pageNumber = NumUtil.nullOrZero(pageNumber) ? 1 : pageNumber;
        pageSize = NumUtil.nullOrZero(pageSize) ? 10 : pageSize;
        try {
            PageVo<ColumnArticleDTO> simpleArticleDTOS = columnSettingService.queryColumnArticles(
                    columnId, PageParam.newPageInstance(pageNumber, pageSize));
            return ResVo.ok(simpleArticleDTOS);
        } catch (Exception e) {
            return ResVo.fail(StatusEnum.COLUMN_QUERY_ERROR, e.getMessage());
        }
    }

    /**
     * 根据关键词给出搜索下拉框
     *
     * @param key
     */
    @GetMapping(path = "query")
    public ResVo<SearchColumnVo> recommend(@RequestParam(name = "key", required = false) String key) {
        List<SimpleColumnDTO> list = columnSettingService.listSimpleColumnByBySearchKey(key);
        SearchColumnVo vo = new SearchColumnVo();
        vo.setKey(key);
        vo.setItems(list);
        return ResVo.ok(vo);
    }
}
