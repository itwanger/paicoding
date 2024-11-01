
package com.github.paicoding.forum.service.article.repository.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.github.paicoding.forum.api.model.entity.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 文章支付记录
 *
 * @author YiHui
 * @date 2024-10-29
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("article_pay_record")
public class ArticlePayRecordDO extends BaseDO {
    private static final long serialVersionUID = 1L;

    /**
     * 支付用户
     */
    private Long payUserId;

    /**
     * 收款用户
     */
    private Long receiveUserId;

    /**
     * 文章
     */
    private Long articleId;

    /**
     * 支付状态
     */
    private Integer payStatus;

    /**
     * 邮件通知用户的时间
     */
    private Date notifyTime;

    /**
     * 通知确认次数
     */
    private Integer notifyCnt;

    /**
     * 备注信息
     */
    private String notes;

    /**
     * 用于验证合法性的code
     */
    private String verifyCode;
}
