package com.github.liuyueyi.forum.web.front.backstage.view;

import com.github.liueyueyi.forum.api.model.vo.PageParam;
import com.github.liueyueyi.forum.api.model.vo.PageVo;
import com.github.liueyueyi.forum.api.model.vo.ResVo;
import com.github.liueyueyi.forum.api.model.vo.banner.dto.BannerDTO;
import com.github.liuyueyi.forum.core.util.NumUtil;
import com.github.liuyueyi.forum.service.banner.service.impl.BannerSettingServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author LouZai
 * @date 2022/9/19
 */
@RestController
@RequestMapping(path = "backstage/banner/")
public class BannerViewController {

    @Autowired
    private BannerSettingServiceImpl bannerSettingService;

    @ResponseBody
    @GetMapping(path = "list")
    public ResVo<PageVo<BannerDTO>> list(@RequestParam(name = "pageNumber", required = false) Integer pageNumber,
                                         @RequestParam(name = "pageSize", required = false) Integer pageSize) {
        pageNumber = NumUtil.nullOrZero(pageNumber) ? 1 : pageNumber;
        pageSize = NumUtil.nullOrZero(pageSize) ? 10 : pageSize;
        PageVo<BannerDTO> bannerDTOPageVo = bannerSettingService.getBannerList(PageParam.newPageInstance(pageNumber, pageSize));
        return ResVo.ok(bannerDTOPageVo);
    }
}
