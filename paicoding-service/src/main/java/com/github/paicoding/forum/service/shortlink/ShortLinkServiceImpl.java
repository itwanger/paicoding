package com.github.paicoding.forum.service.shortlink;

import com.github.paicoding.forum.api.model.context.ReqInfoContext;
import com.github.paicoding.forum.api.model.vo.shortlink.*;
import com.github.paicoding.forum.core.cache.RedisClient;
import com.github.paicoding.forum.service.shortlink.repository.mapper.ShortLinkMapper;
import com.github.paicoding.forum.service.shortlink.repository.mapper.ShortLinkRecordMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

@Slf4j
@Service
public class ShortLinkServiceImpl  implements ShortLinkService{


    // Redis中短链接的前缀
    private static final String REDIS_SHORT_LINK_PREFIX = "short_link:";

    @Resource
    private ShortLinkMapper shortLinkMapper;

    @Resource
    private ShortLinkRecordMapper shortLinkRecordMapper;

    public ShortLinkServiceImpl(ShortLinkMapper shortLinkMapper, ShortLinkRecordMapper shortLinkRecordMapper) {
        this.shortLinkMapper = shortLinkMapper;
        this.shortLinkRecordMapper = shortLinkRecordMapper;
    }


    /**
     * 创建短链接
     *
     * @param shortLinkDTO 包含原始URL和用户信息的数据传输对象
     * @return 包含短链接和原始URL的ShortLinkVO对象
     * @throws NoSuchAlgorithmException 如果生成短码时发生错误
     */
    @Override
    public ShortLinkVO createShortLink(ShortLinkDTO shortLinkDTO) throws NoSuchAlgorithmException {
        log.debug("Creating short link for URL: {}", shortLinkDTO.getOriginalUrl());

        String path = shortLinkDTO.getOriginalUrl().replaceAll("^(https?://|http://[^/]+)(/.*)?$", "$2");
        String shortCode = generateUniqueShortCode(path);

        ShortLinkDO shortLinkDO = createShortLinkDO(shortLinkDTO, shortCode);

        // 保存原始链接--短链接映射到DB与Cache
        int  shortLinkId = shortLinkMapper.GetIdAfterInsert(shortLinkDO);
        log.debug("Short link created with ID: {}", shortLinkId);
        RedisClient.hSet(REDIS_SHORT_LINK_PREFIX + shortCode, shortLinkDO.getOriginalUrl(),  String.class);

        // 保存记录到DB
        ShortLinkRecordDO shortLinkRecordDO = createShortLinkRecordDO(shortLinkDO.getShortCode(), shortLinkDTO);
        shortLinkRecordMapper.insert(shortLinkRecordDO);

        log.debug("Short link record saved for short code: {}", shortCode);
        return createShortLinkVO(shortLinkDO);
    }


    /**
     * 获取原始URL
     *
     * @param shortCode 短码
     * @return 包含原始URL的ShortLinkVO对象
     */
    @Override
    public ShortLinkVO getOriginalLink(String shortCode) {
        log.debug("Fetching original link for short code: {}", shortCode);

        String originalUrl = getOriginalUrlFromCacheOrDb(shortCode);

        if (!StringUtils.hasText(originalUrl)) {
            log.error("Short link not found for short code: {}", shortCode);
            throw new RuntimeException("Short link not found");
        }
        String paramUserId = ((null == ReqInfoContext.getReqInfo().getUserId()) ? "0" : ReqInfoContext.getReqInfo().getUserId().toString());
        ShortLinkRecordDO shortLinkRecordDO = createShortLinkRecordDO( shortCode,new ShortLinkDTO(originalUrl, paramUserId,shortCode));
        shortLinkRecordMapper.insert(shortLinkRecordDO);

        log.debug("Original link found for short code: {}", shortCode);
        return new ShortLinkVO(originalUrl, originalUrl);
    }

    /**
     * 将ShortLinkDO对象转换为ShortLinkVO对象
     *
     * @param shortLinkDO ShortLinkDO对象
     * @return ShortLinkVO对象
     */
    private ShortLinkVO createShortLinkVO(ShortLinkDO shortLinkDO) {
        ShortLinkVO shortLinkVO = new ShortLinkVO();
        String host = shortLinkDO.getOriginalUrl().replaceAll("^(https?://|http://)([^/]+).*$", "$1$2");
        shortLinkVO.setShortUrl(host + "/sol/" + shortLinkDO.getShortCode());
        shortLinkVO.setOriginalUrl(shortLinkDO.getOriginalUrl());
        return shortLinkVO;
    }


    /**
     * 生成唯一的短码
     *
     * @param path URL路径
     * @return 短码
     * @throws NoSuchAlgorithmException 如果生成短码时发生错误
     */
    private String generateUniqueShortCode(String path) throws NoSuchAlgorithmException {
        // 用时间戳保证唯一性
        String shortCode = ShortCodeGenerator.generateShortCode(path + System.currentTimeMillis());
        long generateTime = 0;
        while (null != shortLinkMapper.getByShortCode(shortCode) && generateTime < 3) {
            shortCode = ShortCodeGenerator.generateShortCode(path + System.currentTimeMillis());
            generateTime ++;
        }
        return shortCode;
    }


    /**
     * 创建ShortLinkDO对象
     *
     * @param shortLinkDTO 短链接数据
     * @param shortCode 生成的短码
     * @return 创建的ShortLinkDO对象
     */
    private ShortLinkDO createShortLinkDO(ShortLinkDTO shortLinkDTO, String shortCode) {
        long currentTimeMillis = System.currentTimeMillis();
        Date currentDate = new Date(currentTimeMillis);
        ShortLinkDO shortLinkDO = new ShortLinkDO();
        shortLinkDO.setOriginalUrl(shortLinkDTO.getOriginalUrl());
        shortLinkDO.setShortCode(shortCode);
        shortLinkDO.setCreateTime(currentDate);
        shortLinkDO.setUpdateTime(currentDate);
        shortLinkDO.setDeleted(0);
        return shortLinkDO;
    }


    /**
     * 创建ShortLinkRecordDO对象
     *
     * @param shortcode 短链接代码
     * @param shortLinkDTO 短链接数据
     * @return 创建的ShortLinkRecordDO对象
     */
    private ShortLinkRecordDO createShortLinkRecordDO(String shortcode, ShortLinkDTO shortLinkDTO) {
        ShortLinkRecordDO shortLinkRecordDO = new ShortLinkRecordDO();
        shortLinkRecordDO.setShortCode(shortcode);
        shortLinkRecordDO.setUserId(shortLinkDTO.getUserId());
        shortLinkRecordDO.setAccessTime(System.currentTimeMillis());
        // fixme: 目前没有很好的办法获得用户的登陆方式 因为用户都不一定登录了
        shortLinkRecordDO.setLoginMethod("Unknown");
        shortLinkRecordDO.setIpAddress(ReqInfoContext.getReqInfo().getClientIp());
        shortLinkRecordDO.setAccessSource(SourceDetector.detectSource());
        return shortLinkRecordDO;
    }








    /**
     * 从Redis缓存或数据库中获取原始URL
     *
     * @param shortCode 短码
     * @return 原始URL
     */
    private String getOriginalUrlFromCacheOrDb(String shortCode) {
        String originalUrl = RedisClient.hGet(REDIS_SHORT_LINK_PREFIX + shortCode, "originalUrl", String.class);
        if (!StringUtils.hasText(originalUrl)) {
            ShortLinkDO shortLinkDO = shortLinkMapper.getByShortCode(shortCode);
            if (shortLinkDO != null) {
                originalUrl = shortLinkDO.getOriginalUrl();
            }
        }
        return originalUrl;
    }
}