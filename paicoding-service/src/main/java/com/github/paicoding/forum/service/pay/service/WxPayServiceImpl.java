package com.github.paicoding.forum.service.pay.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.paicoding.forum.api.model.context.ReqInfoContext;
import com.github.paicoding.forum.api.model.enums.pay.ThirdPayWayEnum;
import com.github.paicoding.forum.core.net.HttpRequestHelper;
import com.github.paicoding.forum.core.util.RandUtil;
import com.github.paicoding.forum.service.pay.ThirdPayService;
import com.github.paicoding.forum.service.pay.config.WxPayConfig;
import com.github.paicoding.forum.service.pay.model.PrePayInfoResBo;
import com.github.paicoding.forum.service.pay.model.ThirdPayOrderReqBo;
import com.wechat.pay.java.core.Config;
import com.wechat.pay.java.core.RSAAutoCertificateConfig;
import com.wechat.pay.java.core.exception.ValidationException;
import com.wechat.pay.java.core.notification.NotificationConfig;
import com.wechat.pay.java.core.notification.NotificationParser;
import com.wechat.pay.java.core.notification.RequestParam;
import com.wechat.pay.java.core.util.PemUtil;
import com.wechat.pay.java.service.payments.h5.H5Service;
import com.wechat.pay.java.service.payments.h5.model.H5Info;
import com.wechat.pay.java.service.payments.h5.model.SceneInfo;
import com.wechat.pay.java.service.payments.jsapi.JsapiService;
import com.wechat.pay.java.service.payments.jsapi.model.Amount;
import com.wechat.pay.java.service.payments.jsapi.model.CloseOrderRequest;
import com.wechat.pay.java.service.payments.jsapi.model.Payer;
import com.wechat.pay.java.service.payments.jsapi.model.PrepayRequest;
import com.wechat.pay.java.service.payments.jsapi.model.PrepayResponse;
import com.wechat.pay.java.service.payments.jsapi.model.QueryOrderByOutTradeNoRequest;
import com.wechat.pay.java.service.payments.model.Transaction;
import com.wechat.pay.java.service.refund.RefundService;
import com.wechat.pay.java.service.refund.model.RefundNotification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.Signature;
import java.util.Base64;
import java.util.function.Function;


@Slf4j
@Service
@ConditionalOnBean(WxPayConfig.class)
public class WxPayServiceImpl implements ThirdPayService {
    @Autowired
    private WxPayConfig wxPayConfig;

    /**
     * jsApi微信支付 -- 适用于小程序、公众号等方式的支付场景: 需要拿到用户的openId
     */
    @Override
    public PrePayInfoResBo jsApiOrder(ThirdPayOrderReqBo payReq) throws Exception {
        log.info("微信支付 >>>>>>>>>>>>>>>>> 请求：{}", JSONObject.toJSON(payReq));
        PrepayRequest request = new PrepayRequest();
        Amount amount = new Amount();
        amount.setTotal(payReq.getTotal());
        Payer payer = new Payer();

        payer.setOpenid(payReq.getOpenId());
        request.setAmount(amount);
        request.setPayer(payer);
        request.setAppid(wxPayConfig.getAppId());
        request.setMchid(wxPayConfig.getMerchantId());
        request.setDescription(payReq.getDescription());

        request.setNotifyUrl(wxPayConfig.getPayNotifyUrl());
        request.setOutTradeNo(payReq.getOutTradeNo());

        PrepayResponse response = getJsapiService().prepay(request);
        log.info("微信支付 >>>>>>>>>>>> 返回: {}", response.getPrepayId());
        return genToPayPrePayInfo(response.getPrepayId(), payReq.getOutTradeNo(), ThirdPayWayEnum.WX_JSAPI);
    }

    /**
     * h5支付，生成微信支付收银台中间页，适用于拿不到微信给与的用户 OpenId 场景
     *
     * @return
     */
    public PrePayInfoResBo h5ApiOrder(ThirdPayOrderReqBo payReq) {
        log.info("微信支付 >>>>>>>>>>>>>>>>> 请求：{}", JSONObject.toJSON(payReq));
        com.wechat.pay.java.service.payments.h5.model.PrepayRequest request = new com.wechat.pay.java.service.payments.h5.model.PrepayRequest();
        com.wechat.pay.java.service.payments.h5.model.Amount amount = new com.wechat.pay.java.service.payments.h5.model.Amount();
        amount.setTotal(payReq.getTotal());
        amount.setCurrency("CNY");

        request.setAmount(amount);

        request.setAppid(wxPayConfig.getAppId());
        request.setMchid(wxPayConfig.getMerchantId());
        request.setDescription(payReq.getDescription());

        request.setNotifyUrl(wxPayConfig.getPayNotifyUrl());
        request.setOutTradeNo(payReq.getOutTradeNo());

        SceneInfo sceneInfo = new SceneInfo();
        sceneInfo.setPayerClientIp(ReqInfoContext.getReqInfo().getClientIp());
        H5Info h5Info = new H5Info();
        h5Info.setAppName("技术派");
        h5Info.setAppUrl("https://paicoding.com");
        h5Info.setType("PC");
        sceneInfo.setH5Info(h5Info);
        request.setSceneInfo(sceneInfo);

        log.info("微信支付 >>>>>>>>>>>> 请求: {}", JSON.toJSONString(request));
        com.wechat.pay.java.service.payments.h5.model.PrepayResponse response = getH5Service().prepay(request);
        log.info("微信支付 >>>>>>>>>>>> 返回: {}", response.getH5Url());
        return genToPayPrePayInfo(response.getH5Url(), payReq.getOutTradeNo(), ThirdPayWayEnum.WX_H5);
    }

    /**
     * 唤起支付
     * <a href="https://pay.weixin.qq.com/docs/merchant/apis/jsapi-payment/jsapi-transfer-payment.html">JSAPI调起支付</a>
     *
     * @param prePayId   唤起微信支付的订单
     * @param outTradeNo 传递给微信的唯一业务单号
     * @return
     */
    @Override
    public PrePayInfoResBo genToPayPrePayInfo(String prePayId, String outTradeNo, ThirdPayWayEnum payWay) {
        try {
            String timeStamp = String.valueOf(System.currentTimeMillis() / 1000);
            //随机字符串,要求小于32位
            String nonceStr = RandUtil.random(30);
            String packageStr = "prepay_id=" + prePayId;

            // 不能去除'.append("\n")'，否则失败
            String signStr = wxPayConfig.getAppId() + "\n" +
                    timeStamp + "\n" +
                    nonceStr + "\n" +
                    packageStr + "\n";

            byte[] message = signStr.getBytes(StandardCharsets.UTF_8);

            Signature sign = Signature.getInstance("SHA256withRSA");
            sign.initSign(PemUtil.loadPrivateKeyFromString(wxPayConfig.getPrivateKeyContent()));
            sign.update(message);
            String signStrBase64 = Base64.getEncoder().encodeToString(sign.sign());

            // 拼装返回结果
            PrePayInfoResBo prePay = new PrePayInfoResBo();
            prePay.setOutTradeNo(outTradeNo);
            prePay.setAppId(wxPayConfig.getAppId());
            prePay.setNonceStr(nonceStr);
            prePay.setPrePackage(packageStr);
            prePay.setSignType("RSA");
            prePay.setTimeStamp(timeStamp);
            prePay.setPaySign(signStrBase64);
            prePay.setPrePayId(prePayId);
            return prePay;
        } catch (Exception e) {
            log.error("唤醒支付签名异常: {} - {}", prePayId, outTradeNo, e);
            return null;
        }
    }


    /**
     * 微信回调
     *
     * @param request
     * @return
     * @throws IOException
     */
    @Transactional
    @Override
    public ResponseEntity<?> callback(HttpServletRequest request, Function<Transaction, Boolean> payCallback) throws IOException {
        RequestParam requestParam = new RequestParam.Builder()
                .serialNumber(request.getHeader("Wechatpay-Serial"))
                .nonce(request.getHeader("Wechatpay-Nonce"))
                .timestamp(request.getHeader("Wechatpay-Timestamp"))
                .signature(request.getHeader("Wechatpay-Signature"))
                .body(HttpRequestHelper.readReqData(request))
                .build();
        log.info("微信回调v3 >>>>>>>>>>>>>>>>> {}", JSONObject.toJSONString(requestParam));

        NotificationConfig config = new RSAAutoCertificateConfig.Builder()
                .merchantId(wxPayConfig.getMerchantId())
                .privateKey(wxPayConfig.getPrivateKeyContent())
                .merchantSerialNumber(wxPayConfig.getMerchantSerialNumber())
                .apiV3Key(wxPayConfig.getApiV3Key())
                .build();

        NotificationParser parser = new NotificationParser(config);

        try {
            // 验签、解密并转换成 Transaction（返回参数对象）
            Transaction transaction = parser.parse(requestParam, Transaction.class);
            log.info("微信支付回调 成功，解析: {}", JSON.toJSONString(transaction));
            // TODO 处理你的业务逻辑
            boolean ans = payCallback.apply(transaction);
            if (ans) {
                // 处理成功，返回 200 OK 状态码
                return ResponseEntity.status(HttpStatus.OK).build();
            } else {
                // 处理异常，返回 500 服务器内部异常 状态码
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        } catch (ValidationException e) {
            log.error("微信支付回调v3java失败=" + e.getMessage(), e);
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    /**
     * 关闭微信支付
     */
    @Override
    public void closePay(String outTradeNo) {
        CloseOrderRequest closeRequest = new CloseOrderRequest();
        closeRequest.setMchid(wxPayConfig.getMerchantId());
        closeRequest.setOutTradeNo(outTradeNo);
        getJsapiService().closeOrder(closeRequest);
    }

    @Override
    public Transaction queryWxPayOrderOutTradeNo(String transNo) {
        QueryOrderByOutTradeNoRequest request = new QueryOrderByOutTradeNoRequest();
        request.setMchid(wxPayConfig.getMerchantId());
        request.setOutTradeNo(transNo);
        Transaction transaction = getJsapiService().queryOrderByOutTradeNo(request);
        // TODO 处理你的业务逻辑
        return transaction;
    }


    /**
     * 创建小程序支付服务
     *
     * @return
     */
    protected JsapiService getJsapiService() {
        Config config =
                new RSAAutoCertificateConfig.Builder()
                        .merchantId(wxPayConfig.getMerchantId())
                        .privateKey(wxPayConfig.getPrivateKeyContent())
                        .merchantSerialNumber(wxPayConfig.getMerchantSerialNumber())
                        .apiV3Key(wxPayConfig.getApiV3Key())
                        .build();
        config.createSigner().getAlgorithm();
        return new JsapiService.Builder().config(config).build();
    }

    protected H5Service getH5Service() {
        Config config =
                new RSAAutoCertificateConfig.Builder()
                        .merchantId(wxPayConfig.getMerchantId())
                        .privateKey(wxPayConfig.getPrivateKeyContent())
                        .merchantSerialNumber(wxPayConfig.getMerchantSerialNumber())
                        .apiV3Key(wxPayConfig.getApiV3Key())
                        .build();
        config.createSigner().getAlgorithm();
        return new H5Service.Builder().config(config).build();
    }

    /**
     * 退款服务
     *
     * @return
     */
    protected RefundService getRefundService() {
        Config config =
                new RSAAutoCertificateConfig.Builder()
                        .merchantId(wxPayConfig.getMerchantId())
                        .privateKey(wxPayConfig.getPrivateKeyContent())
                        .merchantSerialNumber(wxPayConfig.getMerchantSerialNumber())
                        .apiV3Key(wxPayConfig.getApiV3Key())
                        .build();
        config.createSigner().getAlgorithm();
        return new RefundService.Builder().config(config).build();
    }


    /**
     * 微信回调
     *
     * @param request
     * @return
     */
    @Transactional
    @Override
    public ResponseEntity<?> refundCallback(HttpServletRequest request, Function<RefundNotification, Boolean> payCallback) {
        RequestParam requestParam = new RequestParam.Builder()
                .serialNumber(request.getHeader("Wechatpay-Serial"))
                .nonce(request.getHeader("Wechatpay-Nonce"))
                .timestamp(request.getHeader("Wechatpay-Timestamp"))
                .signature(request.getHeader("Wechatpay-Signature"))
                .body(HttpRequestHelper.readReqData(request))
                .build();
        log.info("微信退款回调v3 >>>>>>>>>>>>>>>>> {}", JSONObject.toJSONString(requestParam));

        NotificationConfig config = new RSAAutoCertificateConfig.Builder()
                .merchantId(wxPayConfig.getMerchantId())
                .privateKey(wxPayConfig.getPrivateKeyContent())
                .merchantSerialNumber(wxPayConfig.getMerchantSerialNumber())
                .apiV3Key(wxPayConfig.getApiV3Key())
                .build();

        NotificationParser parser = new NotificationParser(config);

        try {
            // 验签、解密并转换成 Transaction（返回参数对象）
            RefundNotification refundNotify = parser.parse(requestParam, RefundNotification.class);
            log.info("微信退款回调 成功，解析: {}", JSON.toJSONString(refundNotify));
            boolean ans = payCallback.apply(refundNotify);
            if (ans) {
                // 处理成功，返回 200 OK 状态码
                return ResponseEntity.status(HttpStatus.OK).build();
            } else {
                // 处理异常，返回 500 服务器内部异常 状态码
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        } catch (ValidationException e) {
            log.error("微信退款回调v3java失败=" + e.getMessage(), e);
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

}
