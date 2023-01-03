package com.github.liuyueyi.forum.web.admin.rest;

import com.github.liueyueyi.forum.api.model.enums.PushStatusEnum;
import com.github.liueyueyi.forum.api.model.vo.PageParam;
import com.github.liueyueyi.forum.api.model.vo.PageVo;
import com.github.liueyueyi.forum.api.model.vo.ResVo;
import com.github.liueyueyi.forum.api.model.vo.banner.ConfigReq;
import com.github.liueyueyi.forum.api.model.vo.banner.dto.ConfigDTO;
import com.github.liueyueyi.forum.api.model.vo.constants.StatusEnum;
import com.github.liuyueyi.forum.core.permission.Permission;
import com.github.liuyueyi.forum.core.permission.UserRole;
import com.github.liuyueyi.forum.core.util.NumUtil;
import com.github.liuyueyi.forum.service.config.service.DictCommonService;
import com.github.liuyueyi.forum.service.config.service.impl.ConfigSettingServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Banner后台
 *
 * @author LouZai
 * @date 2022/9/19
 */
@RestController
@Permission(role = UserRole.ADMIN)
@RequestMapping(path = "admin/common/")
public class CommonSettingrRestController {

    @Autowired
    private DictCommonService dictCommonService;

    @ResponseBody
    @GetMapping(path = "/dict")
    public ResVo<Map<String, Object>> list() {
        Map<String, Object> bannerDTOPageVo = dictCommonService.getDict();
        return ResVo.ok(bannerDTOPageVo);
    }
}
