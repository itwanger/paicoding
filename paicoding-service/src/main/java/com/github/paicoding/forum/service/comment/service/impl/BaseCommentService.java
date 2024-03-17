package com.github.paicoding.forum.service.comment.service.impl;

import com.github.paicoding.forum.api.model.context.ReqInfoContext;
import com.github.paicoding.forum.api.model.enums.DocumentTypeEnum;
import com.github.paicoding.forum.api.model.enums.PraiseStatEnum;
import com.github.paicoding.forum.api.model.vo.comment.dto.BaseCommentDTO;
import com.github.paicoding.forum.api.model.vo.comment.dto.TopCommentDTO;
import com.github.paicoding.forum.api.model.vo.user.dto.BaseUserInfoDTO;
import com.github.paicoding.forum.service.statistics.service.CountService;
import com.github.paicoding.forum.service.user.repository.entity.UserFootDO;
import com.github.paicoding.forum.service.user.service.UserFootService;
import com.github.paicoding.forum.service.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Objects;

/**
 * @author YiHui
 * @date 2024/3/16
 */
public class BaseCommentService {

    @Autowired
    protected UserService userService;

    @Autowired
    protected CountService countService;

    @Autowired
    protected UserFootService userFootService;

    /**
     * 填充评论对应的信息，如用户信息，点赞数等
     *
     * @param comment
     */
    protected void fillCommentInfo(BaseCommentDTO comment) {
        BaseUserInfoDTO userInfoDO = userService.queryBasicUserInfo(comment.getUserId());
        if (userInfoDO == null) {
            // 如果用户注销，给一个默认的用户
            comment.setUserName("用户已注销");
            comment.setUserPhoto("");
            if (comment instanceof TopCommentDTO) {
                ((TopCommentDTO) comment).setCommentCount(0);
            }
        } else {
            comment.setUserName(userInfoDO.getUserName());
            comment.setUserPhoto(userInfoDO.getPhoto());
            if (comment instanceof TopCommentDTO) {
                ((TopCommentDTO) comment).setCommentCount(((TopCommentDTO) comment).getChildComments().size());
            }
        }

        // 查询点赞数
        Long praiseCount = countService.queryCommentPraiseCount(comment.getCommentId());
        comment.setPraiseCount(praiseCount.intValue());

        // 查询当前登录用于是否点赞过
        Long loginUserId = ReqInfoContext.getReqInfo().getUserId();
        if (loginUserId != null) {
            // 判断当前用户是否点过赞
            UserFootDO foot = userFootService.queryUserFoot(comment.getCommentId(), DocumentTypeEnum.COMMENT.getCode(), loginUserId);
            comment.setPraised(foot != null && Objects.equals(foot.getPraiseStat(), PraiseStatEnum.PRAISE.getCode()));
        } else {
            comment.setPraised(false);
        }
    }
}
