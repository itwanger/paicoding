package com.github.paicoding.forum.service.user.repository.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.github.paicoding.forum.api.model.entity.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * ai用户表
 *
 * @ClassName: UserAiDO
 * @author XuYifei
 * @date 2024-07-12
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("user_ai")
public class UserAiDO extends BaseDO {

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 知识星球编号
     */
    private String starNumber;

    /**
     * 星球来源 1=java进阶之路 2=技术派
     */
    private Integer starType;

    /**
     * 当前用户绑定的邀请者
     */
    private Long inviterUserId;

    /**
     * 邀请码
     */
    private String inviteCode;

    /**
     * 当前用户邀请的人数
     */
    private Integer inviteNum;

    /**
     * 二进制使用姿势<br/>
     * 第0位： = 1 表示已绑定微信公众号<br/>
     * 第1位： = 1 表示绑定了邀请用户<br/>
     * 第2位： = 1 表示绑定了java星球<br/>
     * 第3位： = 1 表示绑定了技术派星球
     */
    private Integer strategy;

    /**
     * 0 审核中 1 试用中 2 审核通过 3 审核拒绝
     */
    private Integer state;

    /**
     * 是否删除
     */
    private Integer deleted;

}
