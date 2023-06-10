package com.github.paicoding.forum.service.article.handler;

import org.springframework.stereotype.Component;

import com.github.paicoding.forum.service.article.repository.entity.ArticleDO;

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

    @Override
    public void insert(ArticleDO articleDO) {

        log.info("增加数据");
    }

    @Override
    public void update(ArticleDO before, ArticleDO after) {

        log.info("更新数据");
    }

    @Override
    public void delete(ArticleDO articleDO) {

        log.info("删除数据");
    }
}
