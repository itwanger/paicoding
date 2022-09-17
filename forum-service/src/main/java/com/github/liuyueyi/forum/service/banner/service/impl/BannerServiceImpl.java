package com.github.liuyueyi.forum.service.banner.service.impl;

import com.github.liueyueyi.forum.api.model.enums.BannerTypeEnum;
import com.github.liueyueyi.forum.api.model.vo.banner.dto.BannerDTO;
import com.github.liuyueyi.forum.service.banner.converter.BannerConverter;
import com.github.liuyueyi.forum.service.banner.repository.dao.BannerDao;
import com.github.liuyueyi.forum.service.banner.repository.entity.BannerDO;
import com.github.liuyueyi.forum.service.banner.service.BannerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Banner前台接口
 *
 * @author louzai
 * @date 2022-07-24
 */
@Service
public class BannerServiceImpl implements BannerService {

    @Autowired
    private BannerDao bannerDao;

    @Override
    public List<BannerDTO> getBannerList(BannerTypeEnum bannerTypeEnum) {
        return bannerDao.listBannerByBannerType(bannerTypeEnum.getCode());
    }
}
