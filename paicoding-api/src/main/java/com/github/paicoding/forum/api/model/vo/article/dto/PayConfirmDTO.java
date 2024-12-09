package com.github.paicoding.forum.api.model.vo.article.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @author YiHui
 * @date 2024/10/31
 */
@Data
public class PayConfirmDTO implements Serializable {
    private static final long serialVersionUID = 5470985727304836957L;

    /**
     * 文章标题
     */
    private String title;

    /**
     * 访问地址
     */
    private String articleUrl;

    /**
     * 支付用户
     */
    private String payUser;

    /**
     * 打赏时间
     */
    private String payTime;

    /**
     * 支付金额
     */
    private String payAmount;

    /**
     * 支付方式
     */
    private String payWay;

    /**
     * 通知次数
     */
    private Integer notifyCnt;

    /**
     * 备注文案
     */
    private String mark;

    /**
     * 回调地址
     */
    private String callback;

    /**
     * 确认用户
     */
    private Long receiveUserId;

}
