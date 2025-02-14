package com.github.paicoding.forum.service.shortlink.repository;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.paicoding.forum.api.model.vo.shortlink.ShortLinkDO;
import com.github.paicoding.forum.service.user.repository.entity.UserInfoDO;
import com.github.paicoding.forum.service.user.repository.mapper.UserInfoMapper;

import javax.annotation.Resource;
import java.util.List;

public class ShortLinkDao extends ServiceImpl<UserInfoMapper, UserInfoDO> {
    @Resource
    private ShortLinkMapper shortLinkMapper;

    /**
     * 新增短链接信息
     *
     * @param shortLinkDO
     * @return
     */
    public int insert(ShortLinkDO shortLinkDO) {
        return shortLinkMapper.insert(shortLinkDO);
    }

    /**
     * 根据短链接查询
     *
     * @param shortUrl
     * @return
     */
    public ShortLinkDO getByShortUrl(String shortUrl) {
        return shortLinkMapper.getByShortUrl(shortUrl);
    }

    /**
     * 根据原始链接查询
     *
     * @param originalUrl
     * @return
     */
    public ShortLinkDO getByOriginalUrl(String originalUrl) {
        return shortLinkMapper.getByOriginalUrl(originalUrl);
    }




}
