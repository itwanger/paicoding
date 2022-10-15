package com.github.liuyueyi.forum.service.banner.service;

import com.github.liueyueyi.forum.api.model.enums.BannerTypeEnum;
import com.github.liueyueyi.forum.api.model.vo.PageParam;
import com.github.liueyueyi.forum.api.model.vo.PageVo;
import com.github.liueyueyi.forum.api.model.vo.banner.BannerReq;
import com.github.liueyueyi.forum.api.model.vo.banner.dto.BannerDTO;

import java.util.List;

/**
 * Banner前台接口
 *
 * @author louzai
 * @date 2022-07-24
 */
public interface BannerService {

    /**
     * 获取 Banner 列表
     *
     * @param bannerTypeEnum
     * @return
     */
    List<BannerDTO> getBannerList(BannerTypeEnum bannerTypeEnum);
}
