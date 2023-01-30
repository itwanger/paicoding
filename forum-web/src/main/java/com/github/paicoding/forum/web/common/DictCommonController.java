package com.github.paicoding.forum.web.common;

import com.github.paicoding.forum.api.model.vo.ResVo;
import com.github.paicoding.forum.service.config.service.DictCommonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 通用
 *
 * @author LouZai
 * @date 2022/9/19
 */
@RestController
@RequestMapping(path = "common/")
public class DictCommonController {

    @Autowired
    private DictCommonService dictCommonService;

    @ResponseBody
    @GetMapping(path = "/dict")
    public ResVo<Map<String, Object>> list() {
        Map<String, Object> bannerDTOPageVo = dictCommonService.getDict();
        return ResVo.ok(bannerDTOPageVo);
    }
}
