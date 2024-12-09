package com.github.paicoding.forum.service.pay.service;

import com.github.paicoding.forum.api.model.enums.pay.PayStatusEnum;
import com.github.paicoding.forum.api.model.enums.pay.ThirdPayWayEnum;
import com.github.paicoding.forum.api.model.vo.pay.dto.PayInfoDTO;
import com.github.paicoding.forum.core.util.PriceUtil;
import com.github.paicoding.forum.core.util.StrUtil;
import com.github.paicoding.forum.service.article.conveter.PayConverter;
import com.github.paicoding.forum.service.article.repository.entity.ArticlePayRecordDO;
import com.github.paicoding.forum.service.pay.PayService;
import com.github.paicoding.forum.service.pay.model.PayCallbackBo;
import com.github.paicoding.forum.service.pay.model.PrePayInfoResBo;
import com.github.paicoding.forum.service.pay.model.ThirdPayOrderReqBo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * 在线支付流程
 *
 * @author YiHui
 * @date 2024/12/9
 */
@Service
public class OnlinePayServiceImpl implements PayService {
    @Autowired
    private ThirdPayFacade thirdPayFacade;

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

        ThirdPayOrderReqBo req = new ThirdPayOrderReqBo();
        req.setTotal(record.getPayAmount());
        req.setOutTradeNo(record.getVerifyCode());
        req.setDescription(StrUtil.pickWxSupportTxt(record.getNotes()));
        req.setPayWay(ThirdPayWayEnum.ofPay(record.getPayWay()));
        PrePayInfoResBo res = thirdPayFacade.createPayOrder(req);

        PayInfoDTO payInfo = new PayInfoDTO();
        if (res != null) {
            record.setPrePayId(res.getPrePayId());
            record.setPrePayExpireTime(new Date(res.getExpireTime()));
            if (PayStatusEnum.FAIL.getStatus().equals(record.getPayStatus())) {
                // 支付失败，重新支付时，重置支付状态
                record.setPayStatus(PayStatusEnum.NOT_PAY.getStatus());
            }

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
        PayCallbackBo bo = thirdPayFacade.getPayService(ThirdPayWayEnum.ofPay(dbRecord.getPayWay()))
                .queryOrder(dbRecord.getVerifyCode());
        if (bo.getPayStatus() == PayStatusEnum.SUCCEED || bo.getPayStatus() == PayStatusEnum.FAIL) {
            // 实际结果是支付成功/支付失败时，刷新下record对应的内容
            // 更新原来的支付状态为最新的结果
            dbRecord.setPayStatus(bo.getPayStatus().getStatus());
            dbRecord.setPayCallbackTime(new Date(bo.getSuccessTime()));
            dbRecord.setUpdateTime(new Date());
            dbRecord.setThirdTransCode(bo.getThirdTransactionId());
            return true;
        }

        // 依然返回true，将支付状态设置为true
        return true;
    }

}
