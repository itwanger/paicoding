package com.github.paicoding.forum.web.front.article.rest;

import com.github.paicoding.forum.api.model.context.ReqInfoContext;
import com.github.paicoding.forum.api.model.enums.pay.ThirdPayWayEnum;
import com.github.paicoding.forum.api.model.vo.ResVo;
import com.github.paicoding.forum.api.model.vo.article.dto.ArticlePayInfoDTO;
import com.github.paicoding.forum.core.permission.Permission;
import com.github.paicoding.forum.core.permission.UserRole;
import com.github.paicoding.forum.service.article.service.ArticlePayService;
import com.github.paicoding.forum.service.pay.PayServiceFactory;
import com.github.paicoding.forum.service.pay.model.PayCallbackBo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.function.Function;

/**
 * 返回json格式数据
 *
 * @author YiHui
 * @date 2022/9/2
 */
@Slf4j
@RequestMapping(path = "article/api/pay")
@RestController
public class ArticlePayRestController {
    @Autowired
    private ArticlePayService articlePayService;

    @Autowired
    private PayServiceFactory payServiceFactory;

    /**
     * 支付解锁文章阅读
     *
     * @param articleId
     * @return
     */
    @Permission(role = UserRole.LOGIN)
    @RequestMapping(path = "toPay")
    public ResVo<ArticlePayInfoDTO> toPay(
            @RequestParam(value = "articleId") Long articleId,
            @RequestParam(value = "notes", required = false) String notes) {
        ArticlePayInfoDTO info = articlePayService.toPay(articleId, ReqInfoContext.getReqInfo().getUserId(), notes);
        return ResVo.ok(info);
    }


    /**
     * 用户自己标记为支付成功；后台将状态设置为支付中
     *
     * @param payId
     * @param succeed
     * @return
     */
    @Permission(role = UserRole.LOGIN)
    @RequestMapping(path = "paying")
    public ResVo<Boolean> payed(@RequestParam(value = "payId") Long payId, @RequestParam("succeed") Boolean succeed,
                                @RequestParam(value = "notes", required = false) String notes) {
        if (BooleanUtils.isTrue(succeed)) {
            return ResVo.ok(articlePayService.updatePaying(payId, ReqInfoContext.getReqInfo().getUserId(), notes));
        }
        return ResVo.ok(true);
    }

    /**
     * 支付回调
     * <p>
     * 请求参数： verifyCode + payId + succeed
     *
     * @return
     */
    @RequestMapping(path = "callback")
    public ResponseEntity<ResVo<Boolean>> callback(HttpServletRequest request) {
        return (ResponseEntity<ResVo<Boolean>>) payServiceFactory.getPayService(ThirdPayWayEnum.EMAIL)
                .payCallback(request, new Function<PayCallbackBo, Boolean>() {
                    @Override
                    public Boolean apply(PayCallbackBo transaction) {
                        log.info("个人收款码支付回调执行业务逻辑 {}", transaction);
                        return articlePayService.updatePayStatus(transaction.getPayId(),
                                transaction.getOutTradeNo(),
                                transaction.getPayStatus(),
                                transaction.getSuccessTime(),
                                transaction.getThirdTransactionId());
                    }
                });
    }
}
