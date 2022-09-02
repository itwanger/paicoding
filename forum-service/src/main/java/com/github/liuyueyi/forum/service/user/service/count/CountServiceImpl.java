package com.github.liuyueyi.forum.service.user.service.count;

import com.github.liueyueyi.forum.api.model.vo.user.dto.ArticleFootCountDTO;
import com.github.liuyueyi.forum.service.comment.service.CommentReadService;
import com.github.liuyueyi.forum.service.user.repository.dao.UserFootDao;
import com.github.liuyueyi.forum.service.user.service.CountService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 计数服务，后续计数相关的可以考虑基于redis来做
 *
 * @author YiHui
 * @date 2022/9/2
 */
@Service
public class CountServiceImpl implements CountService {

    private final UserFootDao userFootDao;

    @Resource
    private CommentReadService commentReadService;

    public CountServiceImpl(UserFootDao userFootDao) {
        this.userFootDao = userFootDao;
    }

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
}
