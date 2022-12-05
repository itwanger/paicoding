package com.github.liuyueyi.forum.web.admin.view;

import com.github.liueyueyi.forum.api.model.vo.PageParam;
import com.github.liueyueyi.forum.api.model.vo.PageVo;
import com.github.liueyueyi.forum.api.model.vo.ResVo;
import com.github.liueyueyi.forum.api.model.vo.article.dto.ColumnDTO;
import com.github.liueyueyi.forum.api.model.vo.article.dto.SimpleArticleDTO;
import com.github.liuyueyi.forum.core.permission.Permission;
import com.github.liuyueyi.forum.core.permission.UserRole;
import com.github.liuyueyi.forum.core.util.NumUtil;
import com.github.liuyueyi.forum.service.article.service.ColumnSettingService;
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
@Permission(role = UserRole.ADMIN)
@RequestMapping(path = "admin/column/")
public class ColumnSettingViewController {

    @Autowired
    private ColumnSettingService columnSettingService;

    @ResponseBody
    @GetMapping(path = "listColumn")
    public ResVo<PageVo<ColumnDTO>> listColumn(@RequestParam(name = "pageNumber", required = false) Integer pageNumber,
                                         @RequestParam(name = "pageSize", required = false) Integer pageSize) {
        pageNumber = NumUtil.nullOrZero(pageNumber) ? 1 : pageNumber;
        pageSize = NumUtil.nullOrZero(pageSize) ? 10 : pageSize;
        PageVo<ColumnDTO> columnDTOPageVo = columnSettingService.listColumn(PageParam.newPageInstance(pageNumber, pageSize));
        return ResVo.ok(columnDTOPageVo);
    }

    @ResponseBody
    @GetMapping(path = "listColumnArticle")
    public ResVo<List<SimpleArticleDTO>> listColumnArticle(@RequestParam(name = "columnId") Integer columnId) {
        List<SimpleArticleDTO> simpleArticleDTOS = columnSettingService.queryColumnArticles(columnId);
        return ResVo.ok(simpleArticleDTOS);
    }
}
