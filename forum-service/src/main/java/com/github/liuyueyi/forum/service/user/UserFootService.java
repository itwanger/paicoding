package com.github.liuyueyi.forum.service.user;


import com.github.liuyueyi.forum.service.user.dto.ArticleFootCountDTO;

/**
 * 用户足迹Service接口
 *
 * @author louzai
 * @date 2022-07-20
 */
public interface UserFootService {

    /**
     * 查询文章计数
     * @param documentId
     * @return
     */
    ArticleFootCountDTO queryArticleCount(Long documentId);
}
