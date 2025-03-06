package com.github.paicoding.forum.api.model.vo.pay.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Map;

/**
 * 用于支付的相关信息
 *
 * @author YiHui
 * @date 2024/12/9
 */
@Data
public class PayInfoDTO implements Serializable {
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
