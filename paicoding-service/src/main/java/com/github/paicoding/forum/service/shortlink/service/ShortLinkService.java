package com.github.paicoding.forum.service.shortlink.service;

import com.github.paicoding.forum.api.model.vo.shortlink.dto.ShortLinkDTO;
import com.github.paicoding.forum.api.model.vo.shortlink.ShortLinkVO;

import java.security.NoSuchAlgorithmException;

public interface ShortLinkService {


    /**
     * 创建短链接
     *
     * @param shortLinkDTO 包含原始URL和用户信息的数据传输对象
     * @return 包含短链接和原始URL的ShortLinkVO对象
     * @throws NoSuchAlgorithmException 如果生成短码时发生错误
     */
    ShortLinkVO createShortLink(ShortLinkDTO shortLinkDTO) throws NoSuchAlgorithmException;

    /**
     * 获取原始URL
     *
     * @param shortCode 短码
     * @return 包含原始URL的ShortLinkVO对象
     */
    ShortLinkVO getOriginalLink(String shortCode);
}