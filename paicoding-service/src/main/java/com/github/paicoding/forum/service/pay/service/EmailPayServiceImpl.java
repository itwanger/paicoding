package com.github.paicoding.forum.service.pay.service;

import com.github.paicoding.forum.api.model.enums.pay.ThirdPayWayEnum;
import com.github.paicoding.forum.api.model.vo.ResVo;
import com.github.paicoding.forum.api.model.vo.article.dto.PayConfirmDTO;
import com.github.paicoding.forum.api.model.vo.pay.dto.PayInfoDTO;
import com.github.paicoding.forum.api.model.vo.user.dto.BaseUserInfoDTO;
import com.github.paicoding.forum.core.util.EmailUtil;
import com.github.paicoding.forum.core.util.JsonUtil;
import com.github.paicoding.forum.core.util.SpringUtil;
import com.github.paicoding.forum.core.util.id.IdUtil;
import com.github.paicoding.forum.service.article.conveter.PayConverter;
import com.github.paicoding.forum.service.article.repository.entity.ArticlePayRecordDO;
import com.github.paicoding.forum.service.article.service.ArticlePayService;
import com.github.paicoding.forum.service.pay.PayService;
import com.github.paicoding.forum.service.pay.model.PayCallbackBo;
import com.github.paicoding.forum.service.pay.model.PrePayInfoResBo;
import com.github.paicoding.forum.service.pay.model.ThirdPayOrderReqBo;
import com.github.paicoding.forum.service.user.service.UserService;
import com.wechat.pay.java.service.refund.model.RefundNotification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.function.Function;

/**
 * 个人收款码-基于邮件的支付流程
 *
 * @author YiHui
 * @date 2024/12/9
 */
@Slf4j
@Service
public class EmailPayServiceImpl implements PayService {
    @Autowired
    private UserService userService;

    @Autowired
    private SpringTemplateEngine springTemplateEngine;

    @Autowired
    private ThirdPayHandler thirdPayFacade;

    @Override
    public boolean support(ThirdPayWayEnum payWay) {
        return payWay == ThirdPayWayEnum.EMAIL;
    }

    /**
     * 唤起支付，主要返回作者的收款码
     *
     * @param record
     * @param needRefresh true 表示需要刷新用户的收款码信息
     * @return
     */
    @Override
    public PayInfoDTO toPay(ArticlePayRecordDO record, boolean needRefresh) {
        ThirdPayOrderReqBo req = new ThirdPayOrderReqBo()
                .setOutTradeNo(record.getVerifyCode())
                .setOpenId(record.getReceiveUserId() + "")
                .setPayWay(ThirdPayWayEnum.EMAIL);

        PrePayInfoResBo bo = thirdPayFacade.createPayOrder(req);
        PayInfoDTO payInfo = new PayInfoDTO();
        payInfo.setPrePayId(bo.getPrePayId());
        payInfo.setPayQrCodeMap(PayConverter.formatPayCode(bo.getPrePayId()));
        if (needRefresh) {
            // 需要刷新的场景时，才重置失效时间
            payInfo.setPrePayExpireTime(System.currentTimeMillis() + ThirdPayWayEnum.EMAIL.getExpireTimePeriod());
        }
        return payInfo;
    }

    /**
     * 给作者发送支付确认邮件
     *
     * @param record
     */
    @Override
    public boolean paying(ArticlePayRecordDO record) {
        if (record.getNotifyTime() != null && System.currentTimeMillis() - record.getNotifyTime().getTime() < 180_000) {
            // 两次通知时间，小于10分钟，则直接幂等
            log.info("上次邮件确认时间是: {} 忽略本次通知! {}", record.getNotifyTime(), JsonUtil.toStr(record));
            return false;
        }

        try {
            record.setVerifyCode(IdUtil.genPayCode(ThirdPayWayEnum.ofPay(record.getPayWay()), record.getId()));

            PayConfirmDTO confirm = SpringUtil.getBean(ArticlePayService.class).buildPayConfirmInfo(record.getId(), record);
            Context context = new Context();
            context.setVariable("vo", confirm);
            String confirmHtmlContent = springTemplateEngine.process("PayConfirm", context);
            log.info("输出邮件内容: \n {} \n", confirmHtmlContent);

            // 给作者发送邮件通知
            BaseUserInfoDTO author = userService.queryBasicUserInfo(record.getReceiveUserId());
            EmailUtil.sendMail(String.format("【%s】收到【%s】的打赏，请确认", confirm.getTitle(), confirm.getPayUser()), author.getEmail(), confirmHtmlContent);

            // 邮件发送成功，更新通知时间 + 次数 + 验证码
            record.setNotifyTime(new Date());
            record.setNotifyCnt(record.getNotifyCnt() + 1);
            record.setUpdateTime(new Date());
        } catch (Exception e) {
            log.error("发送邮件确认通知失败: {}", record, e);
        }
        return true;
    }

    @Override
    public ResponseEntity<?> payCallback(HttpServletRequest request, Function<PayCallbackBo, Boolean> payCallback) {
        PayCallbackBo bo = thirdPayFacade.payCallback(request, ThirdPayWayEnum.EMAIL);
        boolean ans = payCallback.apply(bo);
        return ResponseEntity.ok(ResVo.ok(ans));
    }

    @Override
    public ResponseEntity<?> refundCallback(HttpServletRequest request, Function<RefundNotification, Boolean> payCallback) {
        return thirdPayFacade.refundCallback(request, ThirdPayWayEnum.EMAIL, payCallback);
    }
}
