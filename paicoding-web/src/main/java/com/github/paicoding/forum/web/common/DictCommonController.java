package com.github.paicoding.forum.web.common;

import com.github.paicoding.forum.api.model.vo.ResVo;
import com.github.paicoding.forum.core.permission.Permission;
import com.github.paicoding.forum.core.permission.UserRole;
import com.github.paicoding.forum.service.config.service.DictCommonService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 通用
 *
 * @author XuYifei
 * @date 2024-07-12
 */
@RestController
@Slf4j
@Permission(role = UserRole.LOGIN)
@Tag(name = "通用接口管理控制器", description = "全局设置")
@RequestMapping(path = {"common/","api/admin/common/", "admin/common/"})
public class DictCommonController {

    @Autowired
    private DictCommonService dictCommonService;

    @ResponseBody
    @GetMapping(path = "/dict")
    public ResVo<Map<String, Object>> list() {
        log.debug("获取字典");
        Map<String, Object> bannerDTOPageVo = dictCommonService.getDict();
        return ResVo.ok(bannerDTOPageVo);
    }
}
