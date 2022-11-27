package com.github.liuyueyi.forum.web.admin.view;

import com.github.liueyueyi.forum.api.model.vo.PageParam;
import com.github.liueyueyi.forum.api.model.vo.PageVo;
import com.github.liueyueyi.forum.api.model.vo.ResVo;
import com.github.liueyueyi.forum.api.model.vo.banner.dto.ConfigDTO;
import com.github.liuyueyi.forum.core.util.NumUtil;
import com.github.liuyueyi.forum.service.config.service.impl.ConfigSettingServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * Banner后台
 *
 * @author LouZai
 * @date 2022/9/19
 */
@RestController
@RequestMapping(path = "admin/config/")
public class ConfigSettingViewController {

    @Autowired
    private ConfigSettingServiceImpl configSettingService;

    @ResponseBody
    @GetMapping(path = "list")
    public ResVo<PageVo<ConfigDTO>> list(@RequestParam(name = "pageNumber", required = false) Integer pageNumber,
                                         @RequestParam(name = "pageSize", required = false) Integer pageSize) {
        pageNumber = NumUtil.nullOrZero(pageNumber) ? 1 : pageNumber;
        pageSize = NumUtil.nullOrZero(pageSize) ? 10 : pageSize;
        PageVo<ConfigDTO> bannerDTOPageVo = configSettingService.getConfigList(PageParam.newPageInstance(pageNumber, pageSize));
        return ResVo.ok(bannerDTOPageVo);
    }
}
