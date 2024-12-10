package com.github.paicoding.forum.service.pay.service.integration.wx;

import com.alibaba.fastjson.JSONObject;
import com.github.paicoding.forum.api.model.context.ReqInfoContext;
import com.github.paicoding.forum.api.model.enums.pay.ThirdPayWayEnum;
import com.github.paicoding.forum.core.util.JsonUtil;
import com.github.paicoding.forum.service.pay.config.WxPayConfig;
import com.github.paicoding.forum.service.pay.model.PayCallbackBo;
import com.github.paicoding.forum.service.pay.model.PrePayInfoResBo;
import com.github.paicoding.forum.service.pay.model.ThirdPayOrderReqBo;
import com.wechat.pay.java.core.Config;
import com.wechat.pay.java.core.RSAAutoCertificateConfig;
import com.wechat.pay.java.service.payments.h5.H5Service;
import com.wechat.pay.java.service.payments.h5.model.*;
import com.wechat.pay.java.service.payments.model.Transaction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;

/**
 * @author YiHui
 * @date 2024/12/4
 */
@Slf4j
@Service
@ConditionalOnBean(WxPayConfig.class)
public class H5WxPayIntegration extends AbsWxPayIntegration {
    private H5Service h5Service;

    public H5WxPayIntegration(WxPayConfig wxPayConfig) {
        this.wxPayConfig = wxPayConfig;
        Config config = new RSAAutoCertificateConfig.Builder()
                .merchantId(wxPayConfig.getMerchantId())
                .privateKey(wxPayConfig.getPrivateKeyContent())
                .merchantSerialNumber(wxPayConfig.getMerchantSerialNumber())
                .apiV3Key(wxPayConfig.getApiV3Key())
                .build();
        h5Service = new H5Service.Builder().config(config).build();
    }

    @Override
    public boolean support(ThirdPayWayEnum payWay) {
        return ThirdPayWayEnum.WX_H5 == payWay;
    }

    /**
     * h5支付，生成微信支付收银台中间页，适用于拿不到微信给与的用户 OpenId 场景
     *
     * @return
     */
    public String createPayOrder(ThirdPayOrderReqBo payReq) {
        PrepayRequest request = new PrepayRequest();
        request.setAppid(wxPayConfig.getAppId());
        request.setMchid(wxPayConfig.getMerchantId());
        request.setDescription(payReq.getDescription());
        request.setNotifyUrl(wxPayConfig.getPayNotifyUrl());
        request.setOutTradeNo(payReq.getOutTradeNo());

        Amount amount = new Amount();
        amount.setTotal(payReq.getTotal());
        amount.setCurrency("CNY");
        request.setAmount(amount);

        SceneInfo sceneInfo = new SceneInfo();
        sceneInfo.setPayerClientIp(ReqInfoContext.getReqInfo().getClientIp());
        H5Info h5Info = new H5Info();
        h5Info.setAppName("技术派");
        h5Info.setAppUrl("https://paicoding.com");
        h5Info.setType("PC");
        sceneInfo.setH5Info(h5Info);
        request.setSceneInfo(sceneInfo);

        log.info("微信h5下单, 微信请求参数: {}", JsonUtil.toStr(request));
        PrepayResponse response = h5Service.prepay(request);
        log.info("微信支付 >>>>>>>>>>>> 返回: {}", response.getH5Url());
        return response.getH5Url();
    }

    @Override
    public void closeOrder(String outTradeNo) {
        CloseOrderRequest closeRequest = new CloseOrderRequest();
        closeRequest.setMchid(wxPayConfig.getMerchantId());
        closeRequest.setOutTradeNo(outTradeNo);
        h5Service.closeOrder(closeRequest);
    }

    @Override
    public PayCallbackBo queryOrder(String outTradeNo) {
        QueryOrderByOutTradeNoRequest request = new QueryOrderByOutTradeNoRequest();
        request.setMchid(wxPayConfig.getMerchantId());
        request.setOutTradeNo(outTradeNo);
        Transaction transaction = h5Service.queryOrderByOutTradeNo(request);
        return toBo(transaction);
    }

}
