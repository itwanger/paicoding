package com.github.paicoding.forum.service.user.service.count;

import com.github.paicoding.forum.api.model.vo.user.dto.ArticleFootCountDTO;
import com.github.paicoding.forum.api.model.vo.user.dto.SimpleUserInfoDTO;
import com.github.paicoding.forum.service.comment.service.CommentReadService;
import com.github.paicoding.forum.service.user.repository.dao.UserDao;
import com.github.paicoding.forum.service.user.repository.dao.UserFootDao;
import com.github.paicoding.forum.service.user.service.CountService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

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

    @Resource
    private UserDao userDao;

    public CountServiceImpl(UserFootDao userFootDao) {
        this.userFootDao = userFootDao;
    }

    @Override
    public List<SimpleUserInfoDTO> queryPraiseUserInfosByArticleId(Long articleId) {
        return null;
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
