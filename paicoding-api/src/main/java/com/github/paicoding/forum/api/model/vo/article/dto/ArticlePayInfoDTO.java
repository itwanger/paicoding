package com.github.paicoding.forum.api.model.vo.article.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Map;

/**
 * 文章支付信息
 *
 * @author YiHui
 * @date 2024/10/29
 */
@Data
public class ArticlePayInfoDTO implements Serializable {
    /**
     * 支付id
     */
    private Long payId;

    /**
     * 文章id
     */
    private Long articleId;

    /**
     * 支付用户
     */
    private Long payUserId;

    /**
     * 支付状态
     */
    private Integer payStatus;

    /**
     * 收款用户
     */
    private Long receiveUserId;

    /**
     * 收款用户对应的各渠道的收款码
     */
    private Map<String, String> payQrCodeMap;

    /**
     * 支付方式
     */
    private String payWay;

    /**
     * 支付金额
     */
    private String payAmount;

    /**
     * 支付信息
     */
    private String prePayId;

    /**
     * 失效时间
     */
    private Long prePayExpireTime;
}
