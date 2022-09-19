package com.github.liuyueyi.forum.web.front.backstage.rest;

import com.github.liueyueyi.forum.api.model.enums.PushStatusEnum;
import com.github.liueyueyi.forum.api.model.vo.ResVo;
import com.github.liueyueyi.forum.api.model.vo.article.CategoryReq;
import com.github.liueyueyi.forum.api.model.vo.article.TagReq;
import com.github.liueyueyi.forum.api.model.vo.constants.StatusEnum;
import com.github.liuyueyi.forum.service.article.service.CategorySettingService;
import com.github.liuyueyi.forum.service.article.service.TagSettingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author LouZai
 * @date 2022/9/19
 */
@RestController
@RequestMapping(path = "backstage/tag/")
public class TagRestController {

    @Autowired
    private TagSettingService tagSettingService;

    @ResponseBody
    @PostMapping(path = "save")
    public ResVo<String> save(@RequestBody TagReq req) {
        tagSettingService.saveTag(req);
        return ResVo.ok("ok");
    }

    @ResponseBody
    @GetMapping(path = "delete")
    public ResVo<String> delete(@RequestParam(name = "tagId") Integer tagId) {
        tagSettingService.deleteTag(tagId);
        return ResVo.ok("ok");
    }

    @ResponseBody
    @GetMapping(path = "operate")
    public ResVo<String> operate(@RequestParam(name = "tagId") Integer tagId,
                                 @RequestParam(name = "operateType") Integer operateType) {
        if (operateType != PushStatusEnum.OFFLINE.getCode() || operateType!= PushStatusEnum.ONLINE.getCode()) {
            return ResVo.fail(StatusEnum.ILLEGAL_ARGUMENTS);
        }
        tagSettingService.operateTag(tagId, operateType);
        return ResVo.ok("ok");
    }
}
