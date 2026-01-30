package com.github.paicoding.forum.service.sitemap.service.impl;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.github.paicoding.forum.api.model.enums.ArticleEventEnum;
import com.github.paicoding.forum.api.model.event.ArticleMsgEvent;
import com.github.paicoding.forum.api.model.vo.article.dto.SimpleArticleDTO;
import com.github.paicoding.forum.core.cache.RedisClient;
import com.github.paicoding.forum.core.util.DateUtil;
import com.github.paicoding.forum.service.article.repository.dao.ArticleDao;
import com.github.paicoding.forum.service.article.repository.dao.ColumnArticleDao;
import com.github.paicoding.forum.service.article.repository.entity.ArticleDO;
import com.github.paicoding.forum.service.article.repository.entity.ColumnArticleDO;
import com.github.paicoding.forum.service.sitemap.constants.SitemapConstants;
import com.github.paicoding.forum.service.sitemap.model.SiteCntVo;
import com.github.paicoding.forum.service.sitemap.model.SiteMapVo;
import com.github.paicoding.forum.service.sitemap.model.SiteUrlVo;
import com.github.paicoding.forum.service.sitemap.service.SitemapService;
import com.github.paicoding.forum.service.statistics.service.CountService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author YiHui
 * @date 2023/2/13
 */
@Slf4j
@Service
public class SitemapServiceImpl implements SitemapService {
    @Value("${view.site.host:https://paicoding.com}")
    private String host;
    private static final int SCAN_SIZE = 100;

    private static final String SITE_MAP_CACHE_KEY = "sitemap";

    @Resource
    private ArticleDao articleDao;
    @Resource
    private CountService countService;
    @Resource
    private ColumnArticleDao columnArticleDao;

    /**
     * 查询站点地图
     * @return 返回站点地图
     */
    public SiteMapVo getSiteMap() {
        // key = 文章id, value = 最后更新时间
        Map<String, Long> siteMap = RedisClient.hGetAll(SITE_MAP_CACHE_KEY, Long.class);
        if (CollectionUtils.isEmpty(siteMap)) {
            // 首次访问时，没有数据，全量初始化
            initSiteMap();
        }
        siteMap = RedisClient.hGetAll(SITE_MAP_CACHE_KEY, Long.class);
        SiteMapVo vo = initBasicSite();
        if (CollectionUtils.isEmpty(siteMap)) {
            return vo;
        }

        // 批量查询文章信息以获取slug
        List<Long> articleIds = siteMap.keySet().stream()
                .map(Long::valueOf)
                .collect(Collectors.toList());

        List<ArticleDO> articles = articleDao.listByIds(articleIds);
        Map<Long, ArticleDO> articleMap = articles.stream()
                .collect(Collectors.toMap(ArticleDO::getId, article -> article, (a, b) -> a));

        long now = System.currentTimeMillis();
        long thirtyDaysAgo = now - (30L * 24 * 60 * 60 * 1000);
        long ninetyDaysAgo = now - (90L * 24 * 60 * 60 * 1000);

        for (Map.Entry<String, Long> entry : siteMap.entrySet()) {
            Long articleId = Long.valueOf(entry.getKey());
            ArticleDO article = articleMap.get(articleId);
            if (article == null) {
                continue;
            }

            String url;
            if (StringUtils.isNotBlank(article.getShortTitle())) {
                ColumnArticleDO columnArticle = columnArticleDao.selectColumnArticleByArticleId(articleId);
                if (columnArticle != null) {
                    url = host + "/column/" + columnArticle.getColumnId() + "/" + columnArticle.getSection();
                } else {
                    url = buildArticleUrl(article, articleId);
                }
            } else {
                url = buildArticleUrl(article, articleId);
            }

            // 根据文章更新时间决定 changefreq 和 priority
            String changefreq;
            String priority;
            long updateTime = entry.getValue();
            
            if (updateTime > thirtyDaysAgo) {
                // 30天内更新的文章
                changefreq = "weekly";
                priority = "0.8";
            } else if (updateTime > ninetyDaysAgo) {
                // 30-90天内更新的文章
                changefreq = "monthly";
                priority = "0.6";
            } else {
                // 90天以上的旧文章
                changefreq = "yearly";
                priority = "0.5";
            }

            vo.addUrl(new SiteUrlVo(url, DateUtil.time2sitemapDate(updateTime), changefreq, priority));
        }
        return vo;
    }

    private String buildArticleUrl(ArticleDO article, Long articleId) {
        if (StringUtils.isNotBlank(article.getUrlSlug())) {
            return host + "/article/detail/" + articleId + "/" + article.getUrlSlug();
        } else {
            return host + "/article/detail/" + articleId;
        }
    }

    /**
     * fixme: 加锁初始化，更推荐的是采用分布式锁
     */
    private synchronized void initSiteMap() {
        long lastId = 0L;
        RedisClient.del(SITE_MAP_CACHE_KEY);
        while (true) {
            List<SimpleArticleDTO> list = articleDao.getBaseMapper().listArticlesOrderById(lastId, SCAN_SIZE);
            // 刷新文章的统计信息
            list.forEach(s -> countService.refreshArticleStatisticInfo(s.getId()));

            // 刷新站点地图信息
            Map<String, Long> map = list.stream().collect(Collectors.toMap(s -> String.valueOf(s.getId()), s -> s.getCreateTime().getTime(), (a, b) -> a));
            RedisClient.hMSet(SITE_MAP_CACHE_KEY, map);
            if (list.size() < SCAN_SIZE) {
                break;
            }
            lastId = list.get(list.size() - 1).getId();
        }
    }

    private SiteMapVo initBasicSite() {
        SiteMapVo vo = new SiteMapVo();
        String time = DateUtil.time2sitemapDate(System.currentTimeMillis());
        
        // 首页：最高优先级，每日更新
        vo.addUrl(new SiteUrlVo(host + "/", time, "daily", "1.0"));
        
        // 专栏列表：高优先级，每周更新
        vo.addUrl(new SiteUrlVo(host + "/column", time, "weekly", "0.8"));
        
        // 管理后台：低优先级，很少更新
        vo.addUrl(new SiteUrlVo(host + "/admin-view", time, "yearly", "0.3"));
        
        return vo;
    }

    /**
     * 重新刷新站点地图
     */
    @Override
    public void refreshSitemap() {
        initSiteMap();
    }

    /**
     * 生成 robots.txt 内容
     */
    @Override
    public String getRobotsTxt() {
        StringBuilder sb = new StringBuilder();
        sb.append("User-agent: *\n");
        sb.append("Allow: /\n");
        sb.append("\n");
        sb.append("# 禁止抓取管理后台\n");
        sb.append("Disallow: /admin/\n");
        sb.append("Disallow: /admin-view/\n");
        sb.append("\n");
        sb.append("# 禁止抓取API接口\n");
        sb.append("Disallow: /api/\n");
        sb.append("\n");
        sb.append("# 禁止抓取用户相关页面\n");
        sb.append("Disallow: /user/login\n");
        sb.append("Disallow: /user/register\n");
        sb.append("\n");
        sb.append("# Sitemap 位置\n");
        sb.append("Sitemap: ").append(host).append("/sitemap.xml\n");
        return sb.toString();
    }

    /**
     * 基于文章的上下线，自动更新站点地图
     *
     * @param event
     */
    @EventListener(ArticleMsgEvent.class)
    public void autoUpdateSiteMap(ArticleMsgEvent<ArticleDO> event) {
        ArticleEventEnum type = event.getType();
        if (type == ArticleEventEnum.ONLINE) {
            addArticle(event.getContent().getId());
        } else if (type == ArticleEventEnum.OFFLINE || type == ArticleEventEnum.DELETE) {
            rmArticle(event.getContent().getId());
        }
    }

    /**
     * 新增文章并上线
     *
     * @param articleId
     */
    private void addArticle(Long articleId) {
        RedisClient.hSet(SITE_MAP_CACHE_KEY, String.valueOf(articleId), System.currentTimeMillis());
    }

    /**
     * 删除文章、or文章下线
     *
     * @param articleId
     */
    private void rmArticle(Long articleId) {
        RedisClient.hDel(SITE_MAP_CACHE_KEY, String.valueOf(articleId));
    }


    /**
     * 采用定时器方案，每天5:15分刷新站点地图，确保数据的一致性
     */
    @Scheduled(cron = "0 15 5 * * ?")
    public void autoRefreshCache() {
        log.info("开始刷新sitemap.xml的url地址，避免出现数据不一致问题!");
        refreshSitemap();
        log.info("刷新完成！");
    }


    /**
     * 保存站点数据模型
     * <p>
     * 站点统计hash：
     * - visit_info:
     * ---- pv: 站点的总pv
     * ---- uv: 站点的总uv
     * ---- pv_path: 站点某个资源的总访问pv
     * ---- uv_path: 站点某个资源的总访问uv
     * - visit_info_ip:
     * ---- pv: 用户访问的站点总次数
     * ---- path_pv: 用户访问的路径总次数
     * - visit_info_20230822每日记录, 一天一条记录
     * ---- pv: 12  # field = 月日_pv, pv的计数
     * ---- uv: 5   # field = 月日_uv, uv的计数
     * ---- pv_path: 2 # 资源的当前访问计数
     * ---- uv_path: # 资源的当天访问uv
     * ---- pv_ip: # 用户当天的访问次数
     * ---- pv_path_ip: # 用户对资源的当天访问次数
     *
     * @param visitIp 访问者ip
     * @param path    访问的资源路径
     */
    @Override
    public void saveVisitInfo(String visitIp, String path) {
        String globalKey = SitemapConstants.SITE_VISIT_KEY;
        String day = SitemapConstants.day(LocalDate.now());

        String todayKey = globalKey + "_" + day;

        // 用户的全局访问计数+1
        Long globalUserVisitCnt = RedisClient.hIncr(globalKey + "_" + visitIp, "pv", 1);
        // 用户的当日访问计数+1
        Long todayUserVisitCnt = RedisClient.hIncr(todayKey, "pv_" + visitIp, 1);

        RedisClient.PipelineAction pipelineAction = RedisClient.pipelineAction();
        if (globalUserVisitCnt == 1) {
            // 站点新用户
            // 今日的uv + 1
            pipelineAction.add(todayKey, "uv"
                    , (connection, key, field) -> {
                        connection.hIncrBy(key, field, 1);
                    });
            pipelineAction.add(todayKey, "uv_" + path
                    , (connection, key, field) -> connection.hIncrBy(key, field, 1));

            // 全局站点的uv
            pipelineAction.add(globalKey, "uv", (connection, key, field) -> connection.hIncrBy(key, field, 1));
            pipelineAction.add(globalKey, "uv_" + path, (connection, key, field) -> connection.hIncrBy(key, field, 1));
        } else if (todayUserVisitCnt == 1) {
            // 判断是今天的首次访问，更新今天的uv+1
            pipelineAction.add(todayKey, "uv", (connection, key, field) -> connection.hIncrBy(key, field, 1));
            if (RedisClient.hIncr(todayKey, "pv_" + path + "_" + visitIp, 1) == 1) {
                // 判断是否为今天首次访问这个资源，若是，则uv+1
                pipelineAction.add(todayKey, "uv_" + path, (connection, key, field) -> connection.hIncrBy(key, field, 1));
            }

            // 判断是否是用户的首次访问这个path，若是，则全局的path uv计数需要+1
            if (RedisClient.hIncr(globalKey + "_" + visitIp, "pv_" + path, 1) == 1) {
                pipelineAction.add(globalKey, "uv_" + path, (connection, key, field) -> connection.hIncrBy(key, field, 1));
            }
        }


        // 更新pv 以及 用户的path访问信息
        // 今天的相关信息 pv
        pipelineAction.add(todayKey, "pv", (connection, key, field) -> connection.hIncrBy(key, field, 1));
        pipelineAction.add(todayKey, "pv_" + path, (connection, key, field) -> connection.hIncrBy(key, field, 1));
        if (todayUserVisitCnt > 1) {
            // 非当天首次访问，则pv+1; 因为首次访问时，在前面更新uv时，已经计数+1了
            pipelineAction.add(todayKey, "pv_" + path + "_" + visitIp, (connection, key, field) -> connection.hIncrBy(key, field, 1));
        }


        // 全局的 PV
        pipelineAction.add(globalKey, "pv", (connection, key, field) -> connection.hIncrBy(key, field, 1));
        pipelineAction.add(globalKey, "pv" + "_" + path, (connection, key, field) -> connection.hIncrBy(key, field, 1));

        // 保存访问信息
        pipelineAction.execute();
        if (log.isDebugEnabled()) {
            log.info("用户访问信息更新完成! 当前用户总访问: {}，今日访问: {}", globalUserVisitCnt, todayUserVisitCnt);
        }
    }

    /**
     * 查询站点某一天or总的访问信息
     *
     * @param date 日期，为空时，表示查询所有的站点信息
     * @param path 访问路径，为空时表示查站点信息
     * @return
     */
    @Override
    public SiteCntVo querySiteVisitInfo(LocalDate date, String path) {
        String globalKey = SitemapConstants.SITE_VISIT_KEY;
        String day = null, todayKey = globalKey;
        if (date != null) {
            day = SitemapConstants.day(date);
            todayKey = globalKey + "_" + day;
        }

        String pvField = "pv", uvField = "uv";
        if (path != null) {
            // 表示查询对应路径的访问信息
            pvField += "_" + path;
            uvField += "_" + path;
        }

        Map<String, Integer> map = RedisClient.hMGet(todayKey, Arrays.asList(pvField, uvField), Integer.class);
        SiteCntVo siteInfo = new SiteCntVo();
        siteInfo.setDay(day);
        siteInfo.setPv(map.getOrDefault(pvField, 0));
        siteInfo.setUv(map.getOrDefault(uvField, 0));
        return siteInfo;
    }
}
