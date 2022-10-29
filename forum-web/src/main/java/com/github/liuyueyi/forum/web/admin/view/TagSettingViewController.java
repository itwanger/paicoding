package com.github.liuyueyi.forum.web.admin.view;

import com.github.liueyueyi.forum.api.model.vo.PageParam;
import com.github.liueyueyi.forum.api.model.vo.PageVo;
import com.github.liueyueyi.forum.api.model.vo.ResVo;
import com.github.liueyueyi.forum.api.model.vo.article.dto.TagDTO;
import com.github.liuyueyi.forum.core.util.NumUtil;
import com.github.liuyueyi.forum.service.article.service.TagSettingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 标签后台
 *
 * @author LouZai
 * @date 2022/9/19
 */
@RestController
@RequestMapping(path = "admin/tag/")
public class TagSettingViewController {

    @Autowired
    private TagSettingService tagSettingService;

    @ResponseBody
    @GetMapping(path = "list")
    public ResVo<PageVo<TagDTO>> list(@RequestParam(name = "pageNumber", required = false) Integer pageNumber,
                                           @RequestParam(name = "pageSize", required = false) Integer pageSize) {
        pageNumber = NumUtil.nullOrZero(pageNumber) ? 1 : pageNumber;
        pageSize = NumUtil.nullOrZero(pageSize) ? 10 : pageSize;
        PageVo<TagDTO> tagDTOPageVo = tagSettingService.getTagList(PageParam.newPageInstance(pageNumber, pageSize));
        return ResVo.ok(tagDTOPageVo);
    }
}
