package com.github.paicoding.forum.service.pay.service.integration.email;

import com.github.paicoding.forum.api.model.enums.pay.PayStatusEnum;
import com.github.paicoding.forum.api.model.enums.pay.ThirdPayWayEnum;
import com.github.paicoding.forum.service.pay.model.PayCallbackBo;
import com.github.paicoding.forum.service.pay.model.PrePayInfoResBo;
import com.github.paicoding.forum.service.pay.model.ThirdPayOrderReqBo;
import com.github.paicoding.forum.service.pay.service.integration.ThirdPayIntegrationApi;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.common.Booleans;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.function.Function;

/**
 * 个人收款码，基于微信的支付方式
 *
 * @author YiHui
 * @date 2024/12/6
 */
@Slf4j
@Service
public class EmailPayIntegration implements ThirdPayIntegrationApi {
    @Override
    public boolean support(ThirdPayWayEnum payWay) {
        return payWay == ThirdPayWayEnum.EMAIL;
    }

    @Override
    public PrePayInfoResBo createOrder(ThirdPayOrderReqBo payReq) {
        PrePayInfoResBo resBo = new PrePayInfoResBo();
        resBo.setPayWay(ThirdPayWayEnum.EMAIL);
        resBo.setOutTradeNo(payReq.getOutTradeNo());
        return resBo;
    }

    @Override
    public void closeOrder(String outTradeNo) {
    }

    @Override
    public PayCallbackBo queryOrder(String outTradeNo) {
        return new PayCallbackBo().setOutTradeNo(outTradeNo);
    }

    @Override
    public ResponseEntity<?> payCallback(HttpServletRequest request, Function<PayCallbackBo, Boolean> payCallback) throws IOException {
        String outTradeNo = request.getParameter("verifyCode");
        Long payId = Long.parseLong(request.getParameter("payId"));
        PayStatusEnum payStatus = Booleans.isTrue(request.getParameter("succeed")) ? PayStatusEnum.SUCCEED : PayStatusEnum.FAIL;
        PayCallbackBo bo = new PayCallbackBo().setPayId(payId).setOutTradeNo(outTradeNo).setPayStatus(payStatus)
                .setSuccessTime(System.currentTimeMillis());
        try {
            // TODO 处理你的业务逻辑
            boolean ans = payCallback.apply(bo);
            if (ans) {
                // 处理成功，返回 200 OK 状态码
                return ResponseEntity.status(HttpStatus.OK).build();
            } else {
                // 处理异常，返回 500 服务器内部异常 状态码
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        } catch (Exception e) {
            log.error("个人收款码回调失败=" + e.getMessage(), e);
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    @Override
    public <T> ResponseEntity<?> refundCallback(HttpServletRequest request, Function<T, Boolean> refundCallback) throws IOException {
        return ResponseEntity.status(HttpStatus.OK).build();
    }


}
