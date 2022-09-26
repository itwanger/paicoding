package com.github.liuyueyi.forum.web.front.backstage.rest;

import com.github.liueyueyi.forum.api.model.vo.ResVo;
import com.github.liueyueyi.forum.api.model.vo.article.ColumnArticleReq;
import com.github.liueyueyi.forum.api.model.vo.article.ColumnReq;
import com.github.liueyueyi.forum.api.model.vo.banner.BannerReq;
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
@RequestMapping(path = "backstage/column/")
public class ColumnSettingRestController {

    @Autowired
    private ColumnSettingService columnSettingService;

    @ResponseBody
    @PostMapping(path = "saveColumn")
    public ResVo<String> saveColumn(@RequestBody ColumnReq req) {
        columnSettingService.saveColumn(req);
        return ResVo.ok("ok");
    }

    @ResponseBody
    @PostMapping(path = "saveColumnArticle")
    public ResVo<String> saveColumnArticle(@RequestBody ColumnArticleReq req) {
        columnSettingService.saveColumnArticle(req);
        return ResVo.ok("ok");
    }

    @ResponseBody
    @PostMapping(path = "sortColumnArticle")
    public ResVo<String> sortColumnArticle(@RequestBody List<ColumnArticleReq> columnArticleReqs) {
        columnSettingService.sortColumnArticle(columnArticleReqs);
        return ResVo.ok("ok");
    }

    @ResponseBody
    @GetMapping(path = "deleteColumn")
    public ResVo<String> deleteColumn(@RequestParam(name = "columnId") Integer columnId) {
        columnSettingService.deleteColumn(columnId);
        return ResVo.ok("ok");
    }

    @ResponseBody
    @GetMapping(path = "deleteColumnArticle")
    public ResVo<String> deleteColumnArticle(@RequestParam(name = "id") Integer id) {
        columnSettingService.deleteColumnArticle(id);
        return ResVo.ok("ok");
    }
}
