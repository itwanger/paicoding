package com.github.paicoding.forum.web.app.article.extend;

import com.github.paicoding.forum.api.model.context.ReqInfoContext;
import com.github.paicoding.forum.api.model.vo.article.dto.ArticleDTO;
import com.github.paicoding.forum.api.model.vo.user.dto.UserStatisticInfoDTO;
import com.github.paicoding.forum.service.article.service.ArticleReadService;
import com.github.paicoding.forum.service.user.service.UserService;
import com.github.paicoding.forum.web.front.article.vo.ArticleDetailVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author YiHui
 * @date 2024/3/14
 */
@Service
public class AppArticleServiceExtend {

    @Autowired
    private ArticleReadService articleService;

    @Autowired
    private UserService userService;

    /**
     * 文章详情
     *
     * @param articleId
     * @return
     */
    public ArticleDetailVo queryDetail(Long articleId) {
        ArticleDetailVo vo = new ArticleDetailVo();
        // 文章相关信息
        ArticleDTO articleDTO = articleService.queryFullArticleInfo(articleId, ReqInfoContext.getReqInfo().getUserId());
        articleDTO.setContent((articleDTO.getContent()));
        vo.setArticle(articleDTO);

        // 作者信息
        UserStatisticInfoDTO user = userService.queryUserInfoWithStatistic(articleDTO.getAuthor());
        articleDTO.setAuthorName(user.getUserName());
        articleDTO.setAuthorAvatar(user.getPhoto());
        vo.setAuthor(user);
        return vo;
    }

}
