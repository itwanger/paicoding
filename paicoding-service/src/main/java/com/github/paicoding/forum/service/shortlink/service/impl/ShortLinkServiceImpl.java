package com.github.paicoding.forum.service.shortlink.service.impl;

import com.github.paicoding.forum.api.model.context.ReqInfoContext;
import com.github.paicoding.forum.service.shortlink.repository.entity.ShortLinkDO;
import com.github.paicoding.forum.api.model.vo.shortlink.dto.ShortLinkDTO;
import com.github.paicoding.forum.service.shortlink.repository.entity.ShortLinkRecordDO;
import com.github.paicoding.forum.api.model.vo.shortlink.ShortLinkVO;
import com.github.paicoding.forum.core.cache.RedisClient;
import com.github.paicoding.forum.service.shortlink.help.ShortCodeGenerator;
import com.github.paicoding.forum.service.shortlink.help.SourceDetector;
import com.github.paicoding.forum.service.shortlink.repository.mapper.ShortLinkMapper;
import com.github.paicoding.forum.service.shortlink.repository.mapper.ShortLinkRecordMapper;
import com.github.paicoding.forum.service.shortlink.service.ShortLinkService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.List;

@Slf4j
@Service
public class ShortLinkServiceImpl implements ShortLinkService {


    // Redis中短链接的前缀
    private static final String REDIS_SHORT_LINK_PREFIX = "short_link:";

    @Resource
    private ShortLinkMapper shortLinkMapper;

    @Resource
    private ShortLinkRecordMapper shortLinkRecordMapper;

    @Value("${view.site.host:https://paicoding.com}")
    private String host;

    public ShortLinkServiceImpl(ShortLinkMapper shortLinkMapper, ShortLinkRecordMapper shortLinkRecordMapper) {
        this.shortLinkMapper = shortLinkMapper;
        this.shortLinkRecordMapper = shortLinkRecordMapper;
    }


    // 域名白名单
    @Value("#{'${short-link.whitelist:}'.split(',')}")
    private List<String> domainWhitelist;


    /**
     * 创建短链接
     *
     * @param shortLinkDTO 包含原始URL和用户信息的数据传输对象
     * @return 包含短链接和原始URL的ShortLinkVO对象
     * @throws NoSuchAlgorithmException 如果生成短码时发生错误
     */
    @Override
    public ShortLinkVO createShortLink(ShortLinkDTO shortLinkDTO) throws NoSuchAlgorithmException {
        if (log.isDebugEnabled()) {
            log.debug("Creating short link for URL: {}", shortLinkDTO.getOriginalUrl());
        }

        // 验证域名是否在白名单中
        if (!isUrlInWhitelist(shortLinkDTO.getOriginalUrl())) {
            log.warn("域名不在白名单中: {}", shortLinkDTO.getOriginalUrl());
            throw new RuntimeException("不允许为该域名创建短链接");
        }

        // 从原始URL中提取路径部分
        // ^(https?://|http://[^/]+) - 匹配URL开头的协议和域名部分
        String path = shortLinkDTO.getOriginalUrl().replaceAll("^(https?://|http://[^/]+)(/.*)?$", "$2");

        String shortCode = generateUniqueShortCode(path);

        ShortLinkDO shortLinkDO = createShortLinkDO(shortLinkDTO, shortCode);

        // 保存原始链接--短链接映射到DB与Cache
        int shortLinkId = shortLinkMapper.getIdAfterInsert(shortLinkDO);
        if (log.isDebugEnabled()) {
            log.debug("Short link created with ID: {}", shortLinkId);
        }
        RedisClient.hSet(REDIS_SHORT_LINK_PREFIX + shortCode, shortLinkDO.getOriginalUrl(), String.class);

        // 保存记录到DB
        ShortLinkRecordDO shortLinkRecordDO = createShortLinkRecordDO(shortLinkDO.getShortCode(), shortLinkDTO);
        shortLinkRecordMapper.insert(shortLinkRecordDO);

        if (log.isDebugEnabled()) {
            log.debug("Short link record saved for short code: {}", shortCode);
        }
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
        if (log.isDebugEnabled()) {
            log.debug("Fetching original link for short code: {}", shortCode);
        }

        String originalUrl = getOriginalUrlFromCacheOrDb(shortCode);

        if (!StringUtils.hasText(originalUrl)) {
            log.error("Short link not found for short code: {}", shortCode);
            throw new RuntimeException("Short link not found");
        }
        String paramUserId = ((null == ReqInfoContext.getReqInfo().getUserId()) ? "0" : ReqInfoContext.getReqInfo().getUserId().toString());
        log.info("Short link retrieved - shortCode: {}, originalUrl: {}, userId: {}", shortCode, originalUrl, paramUserId);
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
        long generateTime = 0;
        String shortCode;
        do {
            shortCode = ShortCodeGenerator.generateShortCode(path);
            generateTime++;
        } while (null != shortLinkMapper.getByShortCode(shortCode) && generateTime < 3);
        return shortCode;
    }


    /**
     * 创建ShortLinkDO对象
     *
     * @param shortLinkDTO 短链接数据
     * @param shortCode    生成的短码
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
     * @param shortcode    短链接代码
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

    /**
     * 检查URL是否在白名单中
     *
     * @param url 待检查的URL
     * @return 是否在白名单中
     */
    private boolean isUrlInWhitelist(String url) {
        if (domainWhitelist == null || domainWhitelist.isEmpty()) {
            return true; // 如果白名单为空，则允许所有域名
        }

        try {
            URI uri = new URI(url);
            String hostRaw = uri.getHost();
            if (hostRaw == null) {
                log.error("无效的URL格式，缺少host: {}", url);
                return false;
            }

            // 去掉URL中的协议部分
            String host = hostRaw.replaceAll("^[a-zA-Z]+://", "");
            String hostWithPort = host + (uri.getPort() != -1 ? ":" + uri.getPort() : "");

            return domainWhitelist.stream()
                    // 白名单中无协议名，只有域名
                    .map(String::trim)
                    // 检查域名是否在白名单中
                    // 精确匹配域名，避免子域名攻击
                    .anyMatch(domain ->
                            hostWithPort.equals(domain) || // 带端口
                                    host.equals(domain)    // 不带端口
                    );
        } catch (URISyntaxException e) {
            log.error("无效的URL格式: {}", url, e);
            return false;
        }
    }
}
