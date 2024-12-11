package com.github.paicoding.forum.service.pay.service;

import com.github.paicoding.forum.api.model.enums.pay.PayStatusEnum;
import com.github.paicoding.forum.api.model.enums.pay.ThirdPayWayEnum;
import com.github.paicoding.forum.api.model.vo.pay.dto.PayInfoDTO;
import com.github.paicoding.forum.core.util.JsonUtil;
import com.github.paicoding.forum.core.util.PriceUtil;
import com.github.paicoding.forum.core.util.StrUtil;
import com.github.paicoding.forum.service.article.conveter.PayConverter;
import com.github.paicoding.forum.service.article.repository.entity.ArticlePayRecordDO;
import com.github.paicoding.forum.service.pay.PayService;
import com.github.paicoding.forum.service.pay.model.PayCallbackBo;
import com.github.paicoding.forum.service.pay.model.PrePayInfoResBo;
import com.github.paicoding.forum.service.pay.model.ThirdPayOrderReqBo;
import com.wechat.pay.java.service.refund.model.RefundNotification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.function.Function;

/**
 * 在线支付流程
 *
 * @author YiHui
 * @date 2024/12/9
 */
@Slf4j
@Service
public class OnlinePayServiceImpl implements PayService {
    @Autowired
    private ThirdPayHandler thirdPayFacade;

    @Override
    public boolean support(ThirdPayWayEnum payWay) {
        return payWay != ThirdPayWayEnum.EMAIL;
    }

    @Override
    public PayInfoDTO toPay(ArticlePayRecordDO record, boolean needRefresh) {
        if (!needRefresh) {
            // 不需要刷新时，直接根据数据库中缓存的进行返回
            PayInfoDTO payInfo = new PayInfoDTO();
            payInfo.setPrePayId(PayConverter.genQrCode(record.getPrePayId()));
            payInfo.setPayWay(record.getPayWay());
            payInfo.setPrePayExpireTime(record.getPrePayExpireTime().getTime());
            payInfo.setPayAmount(PriceUtil.toYuanPrice(record.getPayAmount()));
            return payInfo;
        }

        // 需要像微信重新创建支付订单，并且将结果反写到支付记录中
        ThirdPayOrderReqBo req = new ThirdPayOrderReqBo();
        req.setTotal(record.getPayAmount());
        req.setOutTradeNo(record.getVerifyCode());
        req.setDescription(StrUtil.pickWxSupportTxt(record.getNotes()));
        req.setPayWay(ThirdPayWayEnum.ofPay(record.getPayWay()));
        PrePayInfoResBo res = thirdPayFacade.createPayOrder(req);

        PayInfoDTO payInfo = new PayInfoDTO();
        if (res != null) {
            // 回写微信支付信息到支付记录中，用于下次唤起支付使用
            record.setPrePayId(res.getPrePayId());
            record.setPrePayExpireTime(new Date(res.getExpireTime()));

            payInfo.setPrePayId(PayConverter.genQrCode(res.getPrePayId()));
            payInfo.setPayWay(record.getPayWay());
            payInfo.setPrePayExpireTime(res.getExpireTime());
            payInfo.setPayAmount(PriceUtil.toYuanPrice(record.getPayAmount()));
        }
        return payInfo;
    }

    /**
     * 主动查询一下支付状态
     *
     * @param dbRecord
     * @return
     */
    @Override
    public boolean paying(ArticlePayRecordDO dbRecord) {
        // 主动查询一下支付状态
        try {
            PayCallbackBo bo = thirdPayFacade.queryOrder(dbRecord.getVerifyCode(), ThirdPayWayEnum.ofPay(dbRecord.getPayWay()));
            if (bo.getPayStatus() == PayStatusEnum.SUCCEED || bo.getPayStatus() == PayStatusEnum.FAIL) {
                // 实际结果是支付成功/支付失败时，刷新下record对应的内容
                // 更新原来的支付状态为最新的结果
                dbRecord.setPayStatus(bo.getPayStatus().getStatus());
                dbRecord.setPayCallbackTime(new Date(bo.getSuccessTime()));
                dbRecord.setUpdateTime(new Date());
                dbRecord.setThirdTransCode(bo.getThirdTransactionId());
            }
        } catch (Exception e) {
            log.error("查询三方支付状态出现异常: {}", JsonUtil.toStr(dbRecord), e);
        }

        // 依然返回true，将支付状态设置为true
        return true;
    }

    @Override
    public ResponseEntity<?> payCallback(HttpServletRequest request, Function<PayCallbackBo, Boolean> payCallback) {
        try {
            PayCallbackBo bo = thirdPayFacade.payCallback(request, ThirdPayWayEnum.WX_NATIVE);
            boolean ans = payCallback.apply(bo);
            if (ans) {
                // 处理成功，返回 200 OK 状态码
                return ResponseEntity.status(HttpStatus.OK).build();
            } else {
                // 处理异常，返回 500 服务器内部异常 状态码
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        } catch (Exception e) {
            log.error("微信支付回调v3java失败={}", e.getMessage(), e);
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();

    }

    @Override
    public ResponseEntity<?> refundCallback(HttpServletRequest request, Function<RefundNotification, Boolean> payCallback) {
        return thirdPayFacade.refundCallback(request, ThirdPayWayEnum.WX_NATIVE, payCallback);
    }
}
