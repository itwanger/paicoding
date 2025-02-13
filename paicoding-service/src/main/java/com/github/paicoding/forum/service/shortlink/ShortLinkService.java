package com.github.paicoding.forum.service.shortlink;

import com.github.paicoding.forum.api.model.context.ReqInfoContext;
import com.github.paicoding.forum.api.model.vo.shortlink.*;
import com.github.paicoding.forum.service.shortlink.repository.mapper.ShortLinkMapper;
import com.github.paicoding.forum.service.shortlink.repository.mapper.ShortLinkRecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.NoSuchAlgorithmException;
import java.security.Timestamp;


@Service
public class ShortLinkService {

    @Autowired
    private ShortLinkMapper shortLinkMapper;

    @Autowired
    private ShortLinkRecordMapper shortLinkRecordMapper;

    public ShortLinkVO createShortLink(ShortLinkDTO shortLinkDTO) throws NoSuchAlgorithmException {

        String path = shortLinkDTO.getOriginalUrl().replaceAll("^(https?://|http://[^/]+)(/.*)?$", "$2");
        // 用时间戳保证唯一性
        String shortCode = ShortCodeGenerator.generateShortCode(path+ System.currentTimeMillis());
        if( null != shortLinkMapper.getByShortCode(shortCode)) {
            shortCode = ShortCodeGenerator.generateShortCode(path+ System.currentTimeMillis());
        }


        ShortLinkDO shortLinkDO = new ShortLinkDO();
        shortLinkDO.setOriginalUrl(shortLinkDTO.getOriginalUrl());
        shortLinkDO.setShortCode(shortCode);
        shortLinkDO.setCreateTime(System.currentTimeMillis());
        shortLinkDO.setUpdateTime(System.currentTimeMillis());
        shortLinkDO.setDeleted(0);






        // 保存到MYSQL
        int  shortLinkId = shortLinkMapper.GetIdAfterInsert(shortLinkDO);

        ShortLinkRecordDO shortLinkRecordDO = new ShortLinkRecordDO();
        shortLinkRecordDO.setShortLinkId((long) shortLinkId);
        shortLinkRecordDO.setUserId(shortLinkDTO.getUserId());
        shortLinkRecordDO.setAccessTime(System.currentTimeMillis());
        shortLinkRecordDO.setIpAddress(ReqInfoContext.getReqInfo().getClientIp());
        shortLinkRecordDO.setAccessSource("目前不知道怎么办");


        // 保存到MYSQL
        shortLinkRecordMapper.insert(shortLinkRecordDO);


        return createShortLinkVOFromshortLinkDO(shortLinkDO);
    }

    public ShortLinkVO getOriginalLink(String shortCode) {
        // TODO: 后续改成先从Redis访问，如果没有再从MySQL访问
        ShortLinkDO shortLinkDO = shortLinkMapper.getByShortCode(shortCode);
        if (shortLinkDO == null) {
            throw new RuntimeException("Short link not found");
        }
        return createShortLinkVOFromshortLinkDO(shortLinkDO);
    }

    private ShortLinkVO createShortLinkVOFromshortLinkDO(ShortLinkDO shortLinkDO) {
        // DO转VO
        ShortLinkVO shortLinkVO = new ShortLinkVO();
        String host = shortLinkDO.getOriginalUrl().replaceAll("^(https?://|http://)([^/]+).*$", "$1$2");
        shortLinkVO.setShortUrl(host + "/sol/" + shortLinkDO.getShortCode());
        shortLinkVO.setOriginalUrl(shortLinkDO.getOriginalUrl());
        return shortLinkVO;
    }
}