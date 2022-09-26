package com.github.liuyueyi.forum.web.front.article.vo;

import com.github.liueyueyi.forum.api.model.vo.article.dto.ArticleDTO;
import com.github.liueyueyi.forum.api.model.vo.comment.dto.TopCommentDTO;
import com.github.liueyueyi.forum.api.model.vo.recommend.SideBarDTO;
import com.github.liueyueyi.forum.api.model.vo.user.dto.UserStatisticInfoDTO;
import lombok.Data;

import java.util.List;

/**
 * @author YiHui
 * @date 2022/9/2
 */
@Data
public class ArticleDetailVo {
    /**
     * 文章信息
     */
    private ArticleDTO article;

    /**
     * 评论信息
     */
    private List<TopCommentDTO> comments;

    /**
     * 作者相关信息
     */
    private UserStatisticInfoDTO author;

    /**
     * 侧边栏信息
     */
    private List<SideBarDTO> sideBarItems;

}
