package com.github.liuyueyi.forum.service.banner.converter;

import com.github.liueyueyi.forum.api.model.enums.PushStatusEnum;
import com.github.liueyueyi.forum.api.model.vo.banner.BannerReq;
import com.github.liueyueyi.forum.api.model.vo.banner.dto.BannerDTO;
import com.github.liuyueyi.forum.service.banner.repository.entity.BannerDO;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Banner转换
 *
 * @author louzai
 * @date 2022-09-20
 */
public class BannerConverter {

    public static List<BannerDTO> ToDTOS(List<BannerDO> bannerDOS) {
        if (CollectionUtils.isEmpty(bannerDOS)){
            return Collections.emptyList();
        }
        List<BannerDTO> bannerDTOS = new ArrayList<>(bannerDOS.size());
        bannerDOS.forEach( v -> bannerDTOS.add(ToDTO(v)));
        return bannerDTOS;
    }

    public static BannerDTO ToDTO(BannerDO bannerDO) {
        if (bannerDO == null) {
            return null;
        }
        BannerDTO bannerDTO = new BannerDTO();
        bannerDTO.setBannerName(bannerDO.getBannerName());
        bannerDTO.setBannerUrl(bannerDO.getBannerUrl());
        bannerDTO.setBannerType(bannerDO.getBannerType());
        bannerDTO.setRank(bannerDO.getRank());
        bannerDTO.setStatus(bannerDO.getStatus());
        bannerDTO.setId(bannerDO.getId());
        bannerDTO.setCreateTime(bannerDO.getCreateTime());
        bannerDTO.setUpdateTime(bannerDO.getUpdateTime());
        return bannerDTO;
    }

    public static BannerDO ToDO(BannerReq bannerReq) {
        if (bannerReq == null) {
            return null;
        }
        BannerDO bannerDO = new BannerDO();
        bannerDO.setBannerName(bannerReq.getBannerName());
        bannerDO.setBannerUrl(bannerReq.getBannerUrl());
        bannerDO.setBannerType(bannerReq.getBannerType());
        bannerDO.setRank(bannerReq.getRank());
        bannerDO.setStatus(PushStatusEnum.OFFLINE.getCode());
        return bannerDO;
    }
}
