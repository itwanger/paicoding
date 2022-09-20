package com.github.liuyueyi.forum.service.banner.repository.dao;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.liueyueyi.forum.api.model.enums.PushStatusEnum;
import com.github.liueyueyi.forum.api.model.enums.YesOrNoEnum;
import com.github.liueyueyi.forum.api.model.vo.PageParam;
import com.github.liueyueyi.forum.api.model.vo.banner.dto.BannerDTO;
import com.github.liuyueyi.forum.service.banner.converter.BannerConverter;
import com.github.liuyueyi.forum.service.banner.repository.entity.BannerDO;
import com.github.liuyueyi.forum.service.banner.repository.mapper.BannerMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author YiHui
 * @date 2022/9/2
 */
@Repository
public class BannerDao extends ServiceImpl<BannerMapper, BannerDO> {

    /**
     * 根据类型获取 Banner 列表（无需分页）
     *
     * @param bannerType
     * @return
     */
    public List<BannerDTO> listBannerByBannerType(Integer bannerType) {
        List<BannerDO> bannerDOS = lambdaQuery()
                .eq(BannerDO::getBannerType, bannerType)
                .eq(BannerDO::getStatus, PushStatusEnum.ONLINE.getCode())
                .eq(BannerDO::getDeleted, YesOrNoEnum.NO.getCode())
                .orderByAsc(BannerDO::getRank)
                .list();
        return BannerConverter.ToDTOS(bannerDOS);
    }

    /**
     * 获取所有 Banner 列表（分页）
     *
     * @return
     */
    public List<BannerDTO> listBanner(PageParam pageParam) {
        List<BannerDO> bannerDOS = lambdaQuery()
                .eq(BannerDO::getDeleted, YesOrNoEnum.NO.getCode())
                .orderByDesc(BannerDO::getCreateTime)
                .last(PageParam.getLimitSql(pageParam))
                .list();
        return BannerConverter.ToDTOS(bannerDOS);
    }

    /**
     * 获取所有 Banner 总数（分页）
     *
     * @return
     */
    public Integer countBanner() {
        return lambdaQuery()
                .eq(BannerDO::getDeleted, YesOrNoEnum.NO.getCode())
                .count()
                .intValue();
    }
}
