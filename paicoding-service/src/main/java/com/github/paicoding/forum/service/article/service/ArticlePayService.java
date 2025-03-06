package com.github.paicoding.forum.service.article.service;

import com.github.paicoding.forum.api.model.enums.pay.PayStatusEnum;
import com.github.paicoding.forum.api.model.vo.article.dto.ArticlePayInfoDTO;
import com.github.paicoding.forum.api.model.vo.article.dto.PayConfirmDTO;
import com.github.paicoding.forum.api.model.vo.user.dto.SimpleUserInfoDTO;
import com.github.paicoding.forum.service.article.repository.entity.ArticlePayRecordDO;

import java.util.List;

/**
 * @author YiHui
 * @date 2024/10/29
 */
public interface ArticlePayService {

    /**
     * 用户是否已经支付过了
     *
     * @param article
     * @param currentUerId
     * @return
     */
    boolean hasPayed(Long article, Long currentUerId);

    /**
     * 唤起支付
     *
     * @param articleId     文章
     * @param currentUserId 当前用户
     * @param notes         备注
     */
    ArticlePayInfoDTO toPay(Long articleId, Long currentUserId, String notes);

    /**
     * 更新为支付中，由用户告诉后端，表明自己已经支付成功了
     *
     * @param payId         支付id
     * @param currentUserId 当前登录用户
     * @param notes         备注
     * @return true 表示更新成功
     */
    boolean updatePaying(Long payId, Long currentUserId, String notes);

    /**
     * 支付状态更新
     *
     * @param payId         支付id
     * @param verifyCode    验证码
     * @param payStatus     支付状态
     * @param payTime       支付成功时间
     * @param transactionId 三方交易流水号
     * @return
     */
    boolean updatePayStatus(Long payId, String verifyCode, PayStatusEnum payStatus, Long payTime, String transactionId);

    /**
     * 构建支付结果回调的基础信息
     *
     * @param payId  支付id
     * @param record 支付记录
     * @return
     */
    PayConfirmDTO buildPayConfirmInfo(Long payId, ArticlePayRecordDO record);


    /**
     * 查询文章的打赏用户
     *
     * @param articleId 文章id
     * @return
     */
    List<SimpleUserInfoDTO> queryPayUsers(Long articleId);
}
