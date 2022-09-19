package com.github.liuyueyi.forum.web.front.backstage.view;

import com.github.liueyueyi.forum.api.model.vo.PageParam;
import com.github.liueyueyi.forum.api.model.vo.PageVo;
import com.github.liueyueyi.forum.api.model.vo.ResVo;
import com.github.liueyueyi.forum.api.model.vo.article.dto.CategoryDTO;
import com.github.liuyueyi.forum.core.util.NumUtil;
import com.github.liuyueyi.forum.service.article.service.CategorySettingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author LouZai
 * @date 2022/9/19
 */
@RestController
@RequestMapping(path = "backstage/category/")
public class CategoryViewController {

    @Autowired
    private CategorySettingService categorySettingService;

    @ResponseBody
    @GetMapping(path = "list")
    public ResVo<PageVo<CategoryDTO>> list(@RequestParam(name = "pageNumber", required = false) Integer pageNumber,
                                           @RequestParam(name = "pageSize", required = false) Integer pageSize) {
        pageNumber = NumUtil.nullOrZero(pageNumber) ? 1 : pageNumber;
        pageSize = NumUtil.nullOrZero(pageSize) ? 10 : pageSize;
        PageVo<CategoryDTO> categoryDTOPageVo = categorySettingService.getCategoryList(PageParam.newPageInstance(pageNumber, pageSize));
        return ResVo.ok(categoryDTOPageVo);
    }
}
