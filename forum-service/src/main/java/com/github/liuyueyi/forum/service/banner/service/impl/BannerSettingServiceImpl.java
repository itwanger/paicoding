package com.github.liuyueyi.forum.service.banner.service.impl;

import com.github.liueyueyi.forum.api.model.enums.YesOrNoEnum;
import com.github.liueyueyi.forum.api.model.vo.PageParam;
import com.github.liueyueyi.forum.api.model.vo.PageVo;
import com.github.liueyueyi.forum.api.model.vo.banner.BannerReq;
import com.github.liueyueyi.forum.api.model.vo.banner.dto.BannerDTO;
import com.github.liuyueyi.forum.core.util.NumUtil;
import com.github.liuyueyi.forum.service.banner.converter.BannerConverter;
import com.github.liuyueyi.forum.service.banner.repository.dao.BannerDao;
import com.github.liuyueyi.forum.service.banner.repository.entity.BannerDO;
import com.github.liuyueyi.forum.service.banner.service.BannerSettingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Banner后台接口
 *
 * @author louzai
 * @date 2022-07-24
 */
@Service
public class BannerSettingServiceImpl implements BannerSettingService {

    @Autowired
    private BannerDao bannerDao;

    @Override
    public void saveBanner(BannerReq bannerReq) {
        BannerDO bannerDO = BannerConverter.ToDO(bannerReq);
        if (NumUtil.nullOrZero(bannerReq.getBannerId())) {
            bannerDao.save(bannerDO);
        } else {
            bannerDO.setId(bannerReq.getBannerId());
            bannerDao.updateById(bannerDO);
        }
    }

    @Override
    public void deleteBanner(Integer bannerId) {
        BannerDO bannerDO = bannerDao.getById(bannerId);
        if (bannerDO != null){
            bannerDO.setDeleted(YesOrNoEnum.YES.getCode());
            bannerDao.updateById(bannerDO);
        }
    }

    @Override
    public void operateBanner(Integer bannerId, Integer operateType) {
        BannerDO bannerDO = bannerDao.getById(bannerId);
        if (bannerDO != null){
            bannerDO.setStatus(operateType);
            bannerDao.updateById(bannerDO);
        }
    }

    @Override
    public PageVo<BannerDTO> getBannerList(PageParam pageParam) {
        List<BannerDTO> bannerDTOS = bannerDao.listBanner(pageParam);
        Integer totalCount = bannerDao.countBanner();
        return PageVo.build(bannerDTOS, pageParam.getPageSize(), pageParam.getPageNum(), totalCount);
    }
}
