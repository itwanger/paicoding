package com.github.paicoding.forum.service.pay.service.integration.email;

import com.github.paicoding.forum.api.model.enums.pay.PayStatusEnum;
import com.github.paicoding.forum.api.model.enums.pay.ThirdPayWayEnum;
import com.github.paicoding.forum.api.model.vo.user.dto.BaseUserInfoDTO;
import com.github.paicoding.forum.service.pay.model.PayCallbackBo;
import com.github.paicoding.forum.service.pay.model.PrePayInfoResBo;
import com.github.paicoding.forum.service.pay.model.ThirdPayOrderReqBo;
import com.github.paicoding.forum.service.pay.service.integration.ThirdPayIntegrationApi;
import com.github.paicoding.forum.service.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.Objects;

/**
 * 个人收款码，基于微信的支付方式
 *
 * @author YiHui
 * @date 2024/12/6
 */
@Slf4j
@Service
public class EmailPayIntegration implements ThirdPayIntegrationApi {
    @Autowired
    private UserService userService;

    @Override
    public boolean support(ThirdPayWayEnum payWay) {
        return payWay == ThirdPayWayEnum.EMAIL;
    }

    @Override
    public PrePayInfoResBo createOrder(ThirdPayOrderReqBo payReq) {
        PrePayInfoResBo resBo = new PrePayInfoResBo();
        resBo.setPayWay(ThirdPayWayEnum.EMAIL);
        resBo.setOutTradeNo(payReq.getOutTradeNo());

        BaseUserInfoDTO receiveUserInfo = userService.queryBasicUserInfo(Long.parseLong(payReq.getOpenId()));
        resBo.setPrePayId(receiveUserInfo.getPayCode());
        resBo.setExpireTime(System.currentTimeMillis() + ThirdPayWayEnum.EMAIL.getExpireTimePeriod());
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
    public PayCallbackBo payCallback(HttpServletRequest request) {
        String outTradeNo = request.getParameter("verifyCode");
        Long payId = Long.parseLong(request.getParameter("payId"));
        PayStatusEnum payStatus = Objects.equals("true", request.getParameter("succeed")) ? PayStatusEnum.SUCCEED : PayStatusEnum.FAIL;
        return new PayCallbackBo().setPayId(payId).setOutTradeNo(outTradeNo).setPayStatus(payStatus)
                .setSuccessTime(System.currentTimeMillis());
    }
}
