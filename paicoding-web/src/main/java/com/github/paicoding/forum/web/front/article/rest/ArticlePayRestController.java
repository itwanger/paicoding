package com.github.paicoding.forum.web.front.article.rest;

import com.github.paicoding.forum.api.model.context.ReqInfoContext;
import com.github.paicoding.forum.api.model.enums.pay.PayStatusEnum;
import com.github.paicoding.forum.api.model.enums.pay.ThirdPayWayEnum;
import com.github.paicoding.forum.api.model.vo.ResVo;
import com.github.paicoding.forum.api.model.vo.article.dto.ArticlePayInfoDTO;
import com.github.paicoding.forum.core.permission.Permission;
import com.github.paicoding.forum.core.permission.UserRole;
import com.github.paicoding.forum.service.article.service.ArticlePayService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
            @RequestParam(value = "notes", required = false) String notes,
            @RequestParam(value = "payWay", required = false) String payWay) {
        ThirdPayWayEnum pay = StringUtils.isBlank(payWay) ? ThirdPayWayEnum.WX_NATIVE : ThirdPayWayEnum.ofPay(payWay);
        ArticlePayInfoDTO info = articlePayService.toPay(articleId, ReqInfoContext.getReqInfo().getUserId(), notes, pay);
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
     *
     * @param verifyCode
     * @param payId
     * @param succeed
     * @return
     */
    @RequestMapping(path = "callback")
    public ResVo<Boolean> callback(@RequestParam("verifyCode") String verifyCode,
                                   @RequestParam("payId") Long payId,
                                   @RequestParam("succeed") Boolean succeed) {
        PayStatusEnum payStatus = BooleanUtils.isTrue(succeed) ? PayStatusEnum.SUCCEED : PayStatusEnum.FAIL;
        Boolean ans = articlePayService.updatePayStatus(payId,
                verifyCode,
                payStatus,
                System.currentTimeMillis(),
                "");
        return ResVo.ok(ans);
    }
}
