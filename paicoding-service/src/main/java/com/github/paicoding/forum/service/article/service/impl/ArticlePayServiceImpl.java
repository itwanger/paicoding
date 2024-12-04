package com.github.paicoding.forum.service.article.service.impl;

import com.github.paicoding.forum.api.model.enums.NotifyTypeEnum;
import com.github.paicoding.forum.api.model.enums.pay.PayStatusEnum;
import com.github.paicoding.forum.api.model.enums.pay.ThirdPayWayEnum;
import com.github.paicoding.forum.api.model.exception.ExceptionUtil;
import com.github.paicoding.forum.api.model.vo.article.dto.ArticlePayInfoDTO;
import com.github.paicoding.forum.api.model.vo.article.dto.PayConfirmDTO;
import com.github.paicoding.forum.api.model.vo.constants.StatusEnum;
import com.github.paicoding.forum.api.model.vo.user.dto.BaseUserInfoDTO;
import com.github.paicoding.forum.api.model.vo.user.dto.SimpleUserInfoDTO;
import com.github.paicoding.forum.core.util.DateUtil;
import com.github.paicoding.forum.core.util.EmailUtil;
import com.github.paicoding.forum.core.util.JsonUtil;
import com.github.paicoding.forum.core.util.TransactionUtil;
import com.github.paicoding.forum.core.util.id.IdUtil;
import com.github.paicoding.forum.service.article.conveter.PayConverter;
import com.github.paicoding.forum.service.article.repository.dao.ArticlePayDao;
import com.github.paicoding.forum.service.article.repository.entity.ArticleDO;
import com.github.paicoding.forum.service.article.repository.entity.ArticlePayRecordDO;
import com.github.paicoding.forum.service.article.service.ArticlePayService;
import com.github.paicoding.forum.service.article.service.ArticleReadService;
import com.github.paicoding.forum.service.notify.help.MsgNotifyHelper;
import com.github.paicoding.forum.service.pay.ThirdPayService;
import com.github.paicoding.forum.service.pay.model.PrePayInfoResBo;
import com.github.paicoding.forum.service.pay.model.ThirdPayOrderReqBo;
import com.github.paicoding.forum.service.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * @author YiHui
 * @date 2024/10/29
 */
@Slf4j
@Service
public class ArticlePayServiceImpl implements ArticlePayService {
    @Autowired
    private ArticleReadService articleReadService;
    @Autowired
    private UserService userService;

    @Autowired
    private ArticlePayDao articlePayDao;

    @Autowired
    private SpringTemplateEngine springTemplateEngine;

    @Value("${view.site.host:https://paicoding.com}")
    private String host;

    @Autowired
    private ThirdPayService thirdPayService;

    @Override
    public boolean hasPayed(Long article, Long currentUerId) {
        ArticlePayRecordDO dbRecord = articlePayDao.queryRecordByArticleId(article, currentUerId);
        if (dbRecord == null) {
            return false;
        }

        return PayStatusEnum.SUCCEED.getStatus().equals(dbRecord.getPayStatus());
    }

    /**
     * 唤起支付
     *
     * @param articleId     文章
     * @param currentUserId 当前用户
     */
    public ArticlePayInfoDTO toPay(Long articleId, Long currentUserId, String notes, ThirdPayWayEnum payWay) {
        ArticlePayRecordDO dbRecord = articlePayDao.queryRecordByArticleId(articleId, currentUserId);
        if (dbRecord == null) {
            // 不存在时，创建一个
            dbRecord = createPayRecord(articleId, currentUserId, notes, payWay);
        } else if (payWay.wxPay() && !dbRecord.getPayStatus().equals(PayStatusEnum.PAYING.getStatus())) {
            // 在线支付场景，如果没有 prePayInfo 或者已经失效了，则需要重新生成
            if (dbRecord.getPrePayId() == null
                    || dbRecord.getPrePayExpireTime() == null
                    || System.currentTimeMillis() >= dbRecord.getPrePayExpireTime().getTime()) {
                dbRecord.setVerifyCode(IdUtil.genPayCode(payWay, dbRecord.getId()));
                ThirdPayOrderReqBo req = new ThirdPayOrderReqBo();
                req.setTotal(dbRecord.getPayAmount());
                req.setOutTradeNo(dbRecord.getVerifyCode());
                PrePayInfoResBo res = thirdPayService.createPayOrder(req, payWay);
                if (res != null) {
                    dbRecord.setPrePayId(res.getPrePayId());
                    dbRecord.setPrePayExpireTime(new Date(res.getExpireTime()));
                    if (PayStatusEnum.FAIL.getStatus().equals(dbRecord.getPayStatus())) {
                        // 支付失败，重新支付时，重置支付状态
                        dbRecord.setPayStatus(PayStatusEnum.NOT_PAY.getStatus());
                    }
                }
                articlePayDao.updateById(dbRecord);
            }
        } else if (PayStatusEnum.FAIL.getStatus().equals(dbRecord.getPayStatus())) {
            // 个人收款码支付场景：对于原是支付失败的场景，再次支付时，重置相关信息
            dbRecord.setNotifyTime(null);
            dbRecord.setPayStatus(PayStatusEnum.NOT_PAY.getStatus());
            articlePayDao.updateById(dbRecord);
        }

        // 收款用户信息
        ArticlePayInfoDTO dto = PayConverter.toPay(dbRecord);
        if (!payWay.wxPay()) {
            BaseUserInfoDTO receiveUserInfo = userService.queryBasicUserInfo(dbRecord.getReceiveUserId());
            dto.setPayQrCodeMap(PayConverter.formatPayCode(receiveUserInfo.getPayCode()));
        }
        return dto;
    }

    private ArticlePayRecordDO createPayRecord(Long articleId, Long currentUserId, String notes, ThirdPayWayEnum payWay) {
        // fixme 这里需要做分布式防止重复写入
        ArticleDO articleDO = articleReadService.queryBasicArticle(articleId);
        if (articleDO == null) {
            throw ExceptionUtil.of(StatusEnum.RECORDS_NOT_EXISTS, articleId);
        }

        ArticlePayRecordDO record = new ArticlePayRecordDO();
        record.setArticleId(articleId);
        record.setReceiveUserId(articleDO.getUserId());
        record.setPayUserId(currentUserId);
        record.setPayStatus(PayStatusEnum.NOT_PAY.getStatus());
        record.setNotifyTime(null);
        record.setNotifyCnt(1);
        record.setNotes(notes == null ? "" : notes);
        record.setId(IdUtil.genId());
        record.setVerifyCode(IdUtil.genPayCode(payWay, record.getId()));
        record.setPayWay(payWay.getPay());
        record.setPayAmount(articleDO.getPayAmount());
        if (payWay.wxPay()) {
            ThirdPayOrderReqBo req = new ThirdPayOrderReqBo();
            req.setTotal(articleDO.getPayAmount());
            req.setDescription(String.format("支付解锁阅读《%s》", articleDO.getTitle()));
            req.setOutTradeNo(record.getVerifyCode());
            PrePayInfoResBo res = thirdPayService.createPayOrder(req, payWay);
            if (res != null) {
                record.setPrePayId(res.getPrePayId());
                record.setPrePayExpireTime(new Date(res.getExpireTime()));
            }
        }
        articlePayDao.save(record);
        return record;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updatePaying(Long payId, Long currentUserId, String notes) {
        ArticlePayRecordDO record = articlePayDao.selectForUpdate(payId);
        if (!record.getPayUserId().equals(currentUserId)) {
            // 用户不一致，不支持更新
            throw ExceptionUtil.of(StatusEnum.FORBID_ERROR);
        }

        // 更新为支付中
        if (PayStatusEnum.NOT_PAY.getStatus().equals(record.getPayStatus())) {
            record.setPayStatus(PayStatusEnum.PAYING.getStatus());
            record.setUpdateTime(new Date());
            if (StringUtils.isNotBlank(notes)) {
                // 更新备注信息
                record.setNotes(notes);
            }

            // 事务提交之后，发送一个给用户确认的邮件
            TransactionUtil.registryAfterCommitOrImmediatelyRun(() -> sendPayConfirmEmail(record));
            boolean ans = articlePayDao.updateById(record);
            if (ans) {
                // 发布一个用户支付的通知
                MsgNotifyHelper.publish(NotifyTypeEnum.PAYING, record);
            }
        } else if (StringUtils.isNotBlank(notes) && Objects.equals(notes, record.getNotes())) {
            // 备注信息不同时，更新并发送邮件通知
            record.setUpdateTime(new Date());
            record.setNotes(notes);
            // 事务提交之后，发送一个给用户确认的邮件
            TransactionUtil.registryAfterCommitOrImmediatelyRun(() -> sendPayConfirmEmail(record));
            return articlePayDao.updateById(record);
        } else {
            // 直接发送通知邮箱
            sendPayConfirmEmail(record);
        }

        return true;
    }

    /**
     * 更新支付状态
     *
     * @param payId
     * @param payStatus
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updatePayStatus(Long payId, String verifyCode, PayStatusEnum payStatus,
                                   Long payTime, String transactionId) {
        ArticlePayRecordDO dbRecord = articlePayDao.selectForUpdate(payId);
        if (dbRecord == null || !Objects.equals(dbRecord.getVerifyCode(), verifyCode)) {
            throw ExceptionUtil.of(StatusEnum.RECORDS_NOT_EXISTS, "支付记录:" + payId);
        }
        if (Objects.equals(payStatus.getStatus(), dbRecord.getPayStatus())) {
            // 幂等
            return true;
        }

        if (PayStatusEnum.SUCCEED.getStatus().equals(dbRecord.getPayStatus())) {
            // 已经是支付成功，则不更新
            return true;
        } else {
            // 更新原来的支付状态为最新的结果
            dbRecord.setPayStatus(payStatus.getStatus());
            dbRecord.setPayCallbackTime(new Date(payTime));
            dbRecord.setUpdateTime(new Date());
            dbRecord.setThirdTransCode(transactionId);
            boolean ans = articlePayDao.updateById(dbRecord);
            if (ans) {
                MsgNotifyHelper.publish(NotifyTypeEnum.PAY, dbRecord);
            }
            return ans;
        }
    }

    public PayConfirmDTO buildPayConfirmInfo(Long payId, ArticlePayRecordDO record) {
        if (record == null) {
            record = articlePayDao.getById(payId);
        }

        // 文章
        ArticleDO article = articleReadService.queryBasicArticle(record.getArticleId());
        // 支付用户
        BaseUserInfoDTO pay = userService.queryBasicUserInfo(record.getPayUserId());

        PayConfirmDTO confirm = new PayConfirmDTO();
        confirm.setTitle(article.getTitle());
        confirm.setArticleUrl(String.format("%s/article/detail/%s", host, article.getId()));
        confirm.setNotifyCnt(record.getNotifyCnt());
        confirm.setPayTime(DateUtil.format(DateUtil.DB_FORMAT, record.getNotifyTime().getTime()));
        confirm.setPayUser(pay.getUserName());
        confirm.setMark(record.getNotes());
        confirm.setReceiveUserId(record.getReceiveUserId());
        confirm.setCallback(host + "/article/api/pay/callback?payId=" + record.getId() + "&verifyCode=" + record.getVerifyCode());
        return confirm;
    }


    /**
     * 发送支付确认邮件
     *
     * @param record
     */
    public void sendPayConfirmEmail(ArticlePayRecordDO record) {
        ThirdPayWayEnum payWay = ThirdPayWayEnum.ofPay(record.getPayWay());
        if (payWay != ThirdPayWayEnum.EMAIL) {
            return;
        }

        if (record.getNotifyTime() != null && System.currentTimeMillis() - record.getNotifyTime().getTime() < 180_000) {
            // 两次通知时间，小于10分钟，则直接幂等
            log.info("上次邮件确认时间是: {} 忽略本次通知! {}", record.getNotifyTime(), JsonUtil.toStr(record));
            return;
        }

        try {
            // 更新通知时间 + 次数 + 验证码
            record.setNotifyTime(new Date());
            record.setNotifyCnt(record.getNotifyCnt() + 1);
            record.setVerifyCode(RandomStringUtils.randomAlphanumeric(16));

            PayConfirmDTO confirm = buildPayConfirmInfo(record.getId(), record);
            Context context = new Context();
            context.setVariable("vo", confirm);
            String confirmHtmlContent = springTemplateEngine.process("PayConfirm", context);
            log.info("输出邮件内容: \n {} \n", confirmHtmlContent);

            // 作者
            BaseUserInfoDTO author = userService.queryBasicUserInfo(record.getReceiveUserId());
            EmailUtil.sendMail(String.format("【%s】收到【%s】的打赏，请确认", confirm.getTitle(), confirm.getPayUser()), author.getEmail(), confirmHtmlContent);

            // 邮件发送成功，更新邮件通知时间
            record.setUpdateTime(new Date());
            articlePayDao.updateById(record);
        } catch (Exception e) {
            log.error("发送邮件确认通知失败: {}", record, e);
        }
    }


    /**
     * 查询文章的打上用户列表
     *
     * @param articleId
     * @return
     */
    public List<SimpleUserInfoDTO> queryPayUsers(Long articleId) {
        List<Long> users = articlePayDao.querySucceedPayUsersByArticleId(articleId);
        if (CollectionUtils.isEmpty(users)) {
            return Collections.emptyList();
        }

        return userService.batchQuerySimpleUserInfo(users);
    }


}
