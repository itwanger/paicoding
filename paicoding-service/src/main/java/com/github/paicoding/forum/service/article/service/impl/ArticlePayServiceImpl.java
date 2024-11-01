package com.github.paicoding.forum.service.article.service.impl;

import com.github.paicoding.forum.api.model.enums.pay.PayStatusEnum;
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
import com.github.paicoding.forum.service.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
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
    public ArticlePayInfoDTO toPay(Long articleId, Long currentUserId, String notes) {
        ArticlePayRecordDO dbRecord = articlePayDao.queryRecordByArticleId(articleId, currentUserId);
        if (dbRecord == null) {
            // 不存在时，创建一个
            dbRecord = createPayRecord(articleId, currentUserId, notes);
        } else if (PayStatusEnum.FAIL.getStatus().equals(dbRecord.getPayStatus())) {
            // 对于原是支付失败的场景，再次支付时，充值相关信息
            dbRecord.setNotifyTime(null);
            dbRecord.setPayStatus(PayStatusEnum.NOT_PAY.getStatus());
            articlePayDao.updateById(dbRecord);
        }

        // 收款用户信息
        ArticlePayInfoDTO dto = PayConverter.toPay(dbRecord);
        BaseUserInfoDTO receiveUserInfo = userService.queryBasicUserInfo(dbRecord.getReceiveUserId());
        dto.setPayQrCodeMap(PayConverter.formatPayCode(receiveUserInfo.getPayCode()));
        return dto;
    }

    private ArticlePayRecordDO createPayRecord(Long articleId, Long currentUserId, String notes) {
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
        record.setVerifyCode(RandomStringUtils.randomAlphanumeric(16));
        record.setId(IdUtil.genId());
        articlePayDao.save(record);
        return record;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updatePaying(Long payId, Long currentUserId) {
        ArticlePayRecordDO record = articlePayDao.selectForUpdate(payId);
        if (!record.getPayUserId().equals(currentUserId)) {
            // 用户不一致，不支持更新
            throw ExceptionUtil.of(StatusEnum.FORBID_ERROR);
        }

        // 更新为支付中
        if (PayStatusEnum.NOT_PAY.getStatus().equals(record.getPayStatus())) {
            record.setPayStatus(PayStatusEnum.PAYING.getStatus());
            record.setUpdateTime(new Date());

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
    public boolean updatePayStatus(Long payId, String verifyCode, Integer payStatus) {
        ArticlePayRecordDO dbRecord = articlePayDao.selectForUpdate(payId);
        if (dbRecord == null || !Objects.equals(dbRecord.getVerifyCode(), verifyCode)) {
            throw ExceptionUtil.of(StatusEnum.RECORDS_NOT_EXISTS, "支付记录:" + payId);
        }
        if (Objects.equals(payStatus, dbRecord.getPayStatus())) {
            // 幂等
            return true;
        }

        if (PayStatusEnum.SUCCEED.getStatus().equals(dbRecord.getPayStatus())) {
            // 已经是支付成功，则不更新
            return true;
        } else {
            // 更新原来的支付状态为最新的结果
            dbRecord.setPayStatus(payStatus);
            dbRecord.setUpdateTime(new Date());
            return articlePayDao.updateById(dbRecord);
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
        if (record.getNotifyTime() != null && System.currentTimeMillis() - record.getNotifyTime().getTime() < 600_000) {
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
            EmailUtil.sendMail(String.format("【%s】收到【%s】的打赏，请确认", confirm.getTitle(), confirm.getPayTime()), author.getEmail(), confirmHtmlContent);

            // 邮件发送成功，更新邮件通知时间
            record.setUpdateTime(new Date());
            articlePayDao.updateById(record);
        } catch (Exception e) {
            log.error("发送邮件确认通知失败: {}", record, e);
        }
    }


    /**
     * 查询文章的打上用户列表
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
