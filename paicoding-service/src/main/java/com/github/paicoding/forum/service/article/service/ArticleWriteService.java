package com.github.paicoding.forum.service.article.service;

import com.github.paicoding.forum.api.model.vo.article.ArticlePostReq;

public interface ArticleWriteService {

    /**
     * 保存or更新文章
     *
     * @param req    上传的文章体
     * @param author 作者
     * @return 返回文章主键
     */
    Long saveArticle(ArticlePostReq req, Long author);

    /**
     * 删除文章
     *
     * @param articleId   文章id
     * @param loginUserId 执行操作的用户
     */
    void deleteArticle(Long articleId, Long loginUserId);
}
