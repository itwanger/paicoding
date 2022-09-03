package com.github.liuyueyi.forum.service.article.service;

import com.github.liueyueyi.forum.api.model.enums.PushStatusEnum;
import com.github.liueyueyi.forum.api.model.vo.article.ArticlePostReq;

public interface ArticleWriteService {

    /**
     * 保存or更新文章
     *
     * @param req
     * @param author 作者
     * @return
     */
    Long saveArticle(ArticlePostReq req, Long author);

    /**
     * 删除文章
     *
     * @param articleId
     */
    void deleteArticle(Long articleId);

    /**
     * 上线/下线文章
     *
     * @param articleId
     * @param pushStatusEnum
     */
    void operateArticle(Long articleId, PushStatusEnum pushStatusEnum);
}
