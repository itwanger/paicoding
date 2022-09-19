package com.github.liuyueyi.forum.web.front.backstage.rest;

import com.github.liueyueyi.forum.api.model.enums.PushStatusEnum;
import com.github.liueyueyi.forum.api.model.vo.ResVo;
import com.github.liueyueyi.forum.api.model.vo.banner.BannerReq;
import com.github.liueyueyi.forum.api.model.vo.constants.StatusEnum;
import com.github.liuyueyi.forum.service.banner.service.impl.BannerSettingServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author LouZai
 * @date 2022/9/19
 */
@RestController
@RequestMapping(path = "backstage/banner/")
public class BannerRestController {

    @Autowired
    private BannerSettingServiceImpl bannerSettingService;

    @ResponseBody
    @PostMapping(path = "save")
    public ResVo<String> save(@RequestBody BannerReq bannerReq) {
        bannerSettingService.saveBanner(bannerReq);
        return ResVo.ok("ok");
    }

    @ResponseBody
    @GetMapping(path = "delete")
    public ResVo<String> delete(@RequestParam(name = "bannerId") Integer bannerId) {
        bannerSettingService.deleteBanner(bannerId);
        return ResVo.ok("ok");
    }

    @ResponseBody
    @GetMapping(path = "operate")
    public ResVo<String> operate(@RequestParam(name = "bannerId") Integer bannerId,
                                 @RequestParam(name = "operateType") Integer operateType) {
        if (operateType != PushStatusEnum.OFFLINE.getCode() && operateType!= PushStatusEnum.ONLINE.getCode()) {
            return ResVo.fail(StatusEnum.ILLEGAL_ARGUMENTS);
        }
        bannerSettingService.operateBanner(bannerId, operateType);
        return ResVo.ok("ok");
    }
}
