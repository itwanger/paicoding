package com.github.liuyueyi.forum.service.banner.service;

import com.github.liueyueyi.forum.api.model.vo.PageParam;
import com.github.liueyueyi.forum.api.model.vo.PageVo;
import com.github.liueyueyi.forum.api.model.vo.banner.BannerReq;
import com.github.liueyueyi.forum.api.model.vo.banner.dto.BannerDTO;

/**
 * Banner后台接口
 *
 * @author louzai
 * @date 2022-07-24
 */
public interface BannerSettingService {

    /**
     * 保存
     *
     * @param bannerReq
     */
    void saveBanner(BannerReq bannerReq);

    /**
     * 删除
     *
     * @param bannerId
     */
    void deleteBanner(Integer bannerId);

    /**
     * 操作（上线/下线）
     *
     * @param bannerId
     */
    void operateBanner(Integer bannerId, Integer operateType);

    /**
     * 获取 Banner 列表
     *
     * @param pageParam
     * @return
     */
    PageVo<BannerDTO> getBannerList(PageParam pageParam);
}
