package com.github.liuyueyi.forum.service.article.impl;

import com.github.liuyueyi.forum.service.article.ArticleTagService;
import com.github.liuyueyi.forum.service.article.repository.mapper.ArticleTagMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 文章标签Service
 *
 * @author louzai
 * @date 2022-07-20
 */
@Service
public class ArticleTagServiceImpl implements ArticleTagService {

    @Resource
    private ArticleTagMapper articleTagMapper;
}
