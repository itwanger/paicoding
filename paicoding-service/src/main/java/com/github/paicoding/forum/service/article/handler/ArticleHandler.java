package com.github.paicoding.forum.service.article.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.github.paicoding.forum.service.article.repository.entity.ArticleDO;
import com.github.paicoding.forum.service.constant.RedisConstant;
import com.github.paicoding.forum.service.utils.RedisUtil;

import lombok.extern.slf4j.Slf4j;
import top.javatool.canal.client.annotation.CanalTable;
import top.javatool.canal.client.handler.EntryHandler;

/**
 * 文章详情Handler：mysql——>redis
 *
 * @ClassName: ArticleHandler
 * @Author: ygl
 * @Date: 2023/6/10 19:11
 * @Version: 1.0
 */
@Slf4j
@Component
@CanalTable("article")
public class ArticleHandler implements EntryHandler<ArticleDO> {

    @Autowired
    private RedisUtil redisUtil;

    @Override
    public void insert(ArticleDO articleDO) {

        log.info("Article表增加数据");
    }

    @Override
    public void update(ArticleDO before, ArticleDO after) {

        Long articleId = after.getId();
        this.delRedisKey(articleId);
        log.info("Article表更新数据");
    }

    @Override
    public void delete(ArticleDO articleDO) {

        Long articleId = articleDO.getId();
        this.delRedisKey(articleId);

        log.info("Article表删除数据");
    }

    private void delRedisKey(Long articleId) {

        String redisCacheKey =
                RedisConstant.REDIS_PAI_DEFAULT
                        + RedisConstant.REDIS_PRE_ARTICLE
                        + RedisConstant.REDIS_CACHE
                        + articleId;
        redisUtil.del(redisCacheKey);
        log.info("删除Redis的key值：" + redisCacheKey);

    }
}
