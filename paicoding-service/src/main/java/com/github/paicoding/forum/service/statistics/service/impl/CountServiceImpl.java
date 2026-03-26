package com.github.paicoding.forum.service.statistics.service.impl;

import com.github.paicoding.forum.api.model.enums.DocumentTypeEnum;
import com.github.paicoding.forum.api.model.vo.user.dto.ArticleFootCountDTO;
import com.github.paicoding.forum.api.model.vo.user.dto.UserStatisticInfoDTO;
import com.github.paicoding.forum.core.cache.RedisClient;
import com.github.paicoding.forum.core.util.MapUtils;
import com.github.paicoding.forum.service.article.repository.dao.ArticleDao;
import com.github.paicoding.forum.service.article.repository.mapper.ReadCountMapper;
import com.github.paicoding.forum.service.comment.service.CommentReadService;
import com.github.paicoding.forum.service.statistics.constants.CountConstants;
import com.github.paicoding.forum.service.statistics.service.CountService;
import com.github.paicoding.forum.service.user.repository.dao.UserDao;
import com.github.paicoding.forum.service.user.repository.dao.UserFootDao;
import com.github.paicoding.forum.service.user.repository.dao.UserRelationDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 计数服务，后续计数相关的可以考虑基于redis来做
 *
 * @author YiHui
 * @date 2022/9/2
 */
@Slf4j
@Service
public class CountServiceImpl implements CountService {
    private final UserFootDao userFootDao;
    public CountServiceImpl(UserFootDao userFootDao) {
        this.userFootDao = userFootDao;
    }

    @Resource
    private UserRelationDao userRelationDao;

    @Resource
    private ArticleDao articleDao;

    @Resource
    private CommentReadService commentReadService;

    @Resource
    private UserDao userDao;

    @Resource
    private ReadCountMapper readCountMapper;

    @Resource(name = "stringRedisTemplate")
    private RedisTemplate<String, String> stringRedisTemplate;

    @Override
    public ArticleFootCountDTO queryArticleCountInfoByArticleId(Long articleId) {
        ArticleFootCountDTO res = userFootDao.countArticleByArticleId(articleId);
        if (res == null) {
            res = new ArticleFootCountDTO();
        } else {
            res.setCommentCount(commentReadService.queryCommentCount(articleId));
        }
        return res;
    }


    @Override
    public ArticleFootCountDTO queryArticleCountInfoByUserId(Long userId) {
        return userFootDao.countArticleByUserId(userId);
    }

    /**
     * 查询评论的点赞数
     *
     * @param commentId
     * @return
     */
    @Override
    public Long queryCommentPraiseCount(Long commentId) {
        return userFootDao.countCommentPraise(commentId);
    }

    @Override
    public UserStatisticInfoDTO queryUserStatisticInfo(Long userId) {
        Map<String, Integer> ans = RedisClient.hGetAll(CountConstants.USER_STATISTIC_INFO + userId, Integer.class);
        UserStatisticInfoDTO info = new UserStatisticInfoDTO();
        info.setFollowCount(ans.getOrDefault(CountConstants.FOLLOW_COUNT, 0));
        info.setArticleCount(ans.getOrDefault(CountConstants.ARTICLE_COUNT, 0));
        info.setPraiseCount(ans.getOrDefault(CountConstants.PRAISE_COUNT, 0));
        info.setCollectionCount(ans.getOrDefault(CountConstants.COLLECTION_COUNT, 0));
        info.setReadCount(ans.getOrDefault(CountConstants.READ_COUNT, 0));
        info.setFansCount(ans.getOrDefault(CountConstants.FANS_COUNT, 0));
        return info;
    }

    @Override
    public ArticleFootCountDTO queryArticleStatisticInfo(Long articleId) {
        Map<String, Integer> ans = RedisClient.hGetAll(CountConstants.ARTICLE_STATISTIC_INFO + articleId, Integer.class);
        ArticleFootCountDTO info = new ArticleFootCountDTO();
        info.setPraiseCount(ans.getOrDefault(CountConstants.PRAISE_COUNT, 0));
        info.setCollectionCount(ans.getOrDefault(CountConstants.COLLECTION_COUNT, 0));
        info.setCommentCount(ans.getOrDefault(CountConstants.COMMENT_COUNT, 0));
        info.setReadCount(ans.getOrDefault(CountConstants.READ_COUNT, 0));
        return info;
    }

    @Override
    public void incrArticleReadCount(Long authorUserId, Long articleId) {
        // 优化：只更新 Redis 计数器，不再直接写数据库
        // 定时任务 syncArticleReadCountToDb 会将 Redis 计数同步到数据库
        RedisClient.pipelineAction()
                .add(CountConstants.ARTICLE_STATISTIC_INFO + articleId, CountConstants.READ_COUNT,
                        (connection, key, value) -> connection.hIncrBy(key, value, 1))
                .add(CountConstants.USER_STATISTIC_INFO + authorUserId, CountConstants.READ_COUNT,
                        (connection, key, value) -> connection.hIncrBy(key, value, 1))
                .execute();
    }

    /**
     * 每天4:15分执行定时任务，全量刷新用户的统计信息
     */
    @Scheduled(cron = "0 15 4 * * ?")
    public void autoRefreshAllUserStatisticInfo() {
        Long now = System.currentTimeMillis();
        log.info("开始自动刷新用户统计信息");
        Long userId = 0L;
        int batchSize = 20;
        while (true) {
            List<Long> userIds = userDao.scanUserId(userId, batchSize);
            userIds.forEach(this::refreshUserStatisticInfo);
            if (userIds.size() < batchSize) {
                userId = userIds.get(userIds.size() - 1);
                break;
            } else {
                userId = userIds.get(batchSize - 1);
            }
        }
        log.info("结束自动刷新用户统计信息，共耗时: {}ms, maxUserId: {}", System.currentTimeMillis() - now, userId);
    }

    /**
     * 每5分钟执行一次，将 Redis 中的文章阅读计数同步到数据库
     */
    @Scheduled(cron = "0 */5 * * * ?")
    public void syncArticleReadCountToDb() {
        Long start = System.currentTimeMillis();
        log.info("开始同步文章阅读计数到数据库");

        try {
            // 扫描所有文章统计 key
            Set<String> keys = scanKeys(CountConstants.ARTICLE_STATISTIC_INFO + "*");

            int synced = 0;
            for (String key : keys) {
                try {
                    // 提取 articleId
                    Long articleId = Long.parseLong(key.replace(CountConstants.ARTICLE_STATISTIC_INFO, ""));

                    // 获取 Redis 中的阅读计数
                    Integer readCount = getArticleReadCount(key);
                    if (readCount != null && readCount > 0) {
                        // 更新数据库
                        saveArticleReadCount(articleId, readCount);
                        synced++;
                    }
                } catch (Exception e) {
                    log.error("同步阅读计数失败, key: {}", key, e);
                }
            }

            log.info("同步文章阅读计数完成，共同步 {} 篇文章，耗时: {}ms", synced, System.currentTimeMillis() - start);
        } catch (Exception e) {
            log.error("同步文章阅读计数异常", e);
        }
    }

    /**
     * 扫描匹配的 Redis key
     */
    @SuppressWarnings("unchecked")
    Set<String> scanKeys(String pattern) {
        Set<String> keys = new HashSet<>();
        ScanOptions options = ScanOptions.scanOptions().match("pai_" + pattern).count(100).build();

        try (Cursor<String> cursor = stringRedisTemplate.scan(options)) {
            while (cursor.hasNext()) {
                String key = cursor.next();
                // 移除前缀 "pai_"
                keys.add(key.substring(4));
            }
        }
        return keys;
    }

    /**
     * 获取 Redis 中文章当前的阅读总数
     */
    Integer getArticleReadCount(String key) {
        return RedisClient.hGet(key, CountConstants.READ_COUNT, Integer.class);
    }

    /**
     * 将 Redis 中的阅读总数覆盖写入数据库，保证定时同步幂等
     */
    void saveArticleReadCount(Long articleId, Integer readCount) {
        readCountMapper.insertOrUpdate(articleId, DocumentTypeEnum.ARTICLE.getCode(), readCount);
    }


    /**
     * 更新用户的统计信息
     *
     * @param userId
     */
    @Override
    public void refreshUserStatisticInfo(Long userId) {
        // 用户的文章点赞数，收藏数，阅读计数
        ArticleFootCountDTO count = userFootDao.countArticleByUserId(userId);
        if (count == null) {
            count = new ArticleFootCountDTO();
        }

        // 获取关注数
        Long followCount = userRelationDao.queryUserFollowCount(userId);
        // 粉丝数
        Long fansCount = userRelationDao.queryUserFansCount(userId);

        // 查询用户发布的文章数
        Integer articleNum = articleDao.countArticleByUser(userId);

        String key = CountConstants.USER_STATISTIC_INFO + userId;
        RedisClient.hMSet(key, MapUtils.create(CountConstants.PRAISE_COUNT, count.getPraiseCount(),
                CountConstants.COLLECTION_COUNT, count.getCollectionCount(),
                CountConstants.READ_COUNT, count.getReadCount(),
                CountConstants.FANS_COUNT, fansCount,
                CountConstants.FOLLOW_COUNT, followCount,
                CountConstants.ARTICLE_COUNT, articleNum));

    }


    public void refreshArticleStatisticInfo(Long articleId) {
        ArticleFootCountDTO res = userFootDao.countArticleByArticleId(articleId);
        if (res == null) {
            res = new ArticleFootCountDTO();
        } else {
            res.setCommentCount(commentReadService.queryCommentCount(articleId));
        }

        RedisClient.hMSet(CountConstants.ARTICLE_STATISTIC_INFO + articleId,
                MapUtils.create(CountConstants.COLLECTION_COUNT, res.getCollectionCount(),
                        CountConstants.PRAISE_COUNT, res.getPraiseCount(),
                        CountConstants.READ_COUNT, res.getReadCount(),
                        CountConstants.COMMENT_COUNT, res.getCommentCount()
                )
        );
    }
}
