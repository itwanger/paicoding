package com.github.paicoding.forum.web.controller.chatv2;

import com.github.paicoding.forum.api.model.context.ReqInfoContext;
import com.github.paicoding.forum.api.model.vo.ResVo;
import com.github.paicoding.forum.api.model.vo.chatv2.QuotaRechargeReqVO;
import com.github.paicoding.forum.api.model.vo.chatv2.UserModelQuotaVO;
import com.github.paicoding.forum.api.model.vo.constants.StatusEnum;
import com.github.paicoding.forum.core.permission.Permission;
import com.github.paicoding.forum.core.permission.UserRole;
import com.github.paicoding.forum.service.chatv2.service.TokenQuotaService;
import com.github.paicoding.forum.service.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Token 配额管理 REST 控制器
 *
 * @author XuYifei
 * @date 2025-11-17
 */
@Slf4j
@RestController
@RequestMapping("/chatv2/api/quota")
@RequiredArgsConstructor
public class TokenQuotaController {

    private final TokenQuotaService tokenQuotaService;
    private final UserService userService;

    /**
     * 获取当前用户所有模型的配额信息
     */
    @GetMapping("/my-quotas")
    @Permission(role = UserRole.LOGIN)
    public ResVo<List<UserModelQuotaVO>> getMyQuotas() {
        Long userId = ReqInfoContext.getReqInfo().getUserId();
        List<UserModelQuotaVO> quotas = tokenQuotaService.getAllModelQuotas(userId);
        return ResVo.ok(quotas);
    }

    /**
     * 获取当前用户指定模型的配额信息
     */
    @GetMapping("/my-quota/{modelId}")
    @Permission(role = UserRole.LOGIN)
    public ResVo<UserModelQuotaVO> getMyQuota(@PathVariable String modelId) {
        Long userId = ReqInfoContext.getReqInfo().getUserId();
        UserModelQuotaVO quota = tokenQuotaService.getUserModelQuotaVO(userId, modelId);

        if (quota == null) {
            return ResVo.fail(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "配额信息不存在");
        }

        return ResVo.ok(quota);
    }

    /**
     * 检查当前用户对指定模型的配额是否充足
     */
    @GetMapping("/check")
    @Permission(role = UserRole.LOGIN)
    public ResVo<Boolean> checkQuota(@RequestParam String modelId,
                                      @RequestParam(required = false) Integer estimatedTokens) {
        Long userId = ReqInfoContext.getReqInfo().getUserId();
        boolean sufficient = tokenQuotaService.checkQuota(userId, modelId, estimatedTokens);
        return ResVo.ok(sufficient);
    }

    /**
     * 管理员：为指定用户充值配额
     */
    @PostMapping("/recharge")
    @Permission(role = UserRole.ADMIN)
    public ResVo<Void> rechargeQuota(@RequestBody QuotaRechargeReqVO request) {
        Long operatorId = ReqInfoContext.getReqInfo().getUserId();
        String operatorName = ReqInfoContext.getReqInfo().getUser().getUserName();

        // 验证参数
        if (request.getUserId() == null || request.getModelId() == null ||
                request.getRechargeAmount() == null || request.getRechargeAmount() <= 0) {
            return ResVo.fail(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "参数错误");
        }

        // 检查用户是否存在
        if (userService.queryBasicUserInfo(request.getUserId()) == null) {
            return ResVo.fail(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "用户不存在");
        }

        try {
            tokenQuotaService.rechargeQuota(
                    request.getUserId(),
                    request.getModelId(),
                    request.getRechargeAmount(),
                    operatorId,
                    operatorName,
                    request.getReason(),
                    request.getRemark()
            );

            log.info("Quota recharged by admin: operatorId={}, userId={}, modelId={}, amount={}",
                    operatorId, request.getUserId(), request.getModelId(), request.getRechargeAmount());

            return ResVo.ok(null);
        } catch (Exception e) {
            log.error("Failed to recharge quota", e);
            return ResVo.fail(StatusEnum.UNEXPECT_ERROR, "充值失败: " + e.getMessage());
        }
    }

    /**
     * 管理员：查询指定用户的配额信息
     */
    @GetMapping("/user/{userId}/quotas")
    @Permission(role = UserRole.ADMIN)
    public ResVo<List<UserModelQuotaVO>> getUserQuotas(@PathVariable Long userId) {
        // 检查用户是否存在
        if (userService.queryBasicUserInfo(userId) == null) {
            return ResVo.fail(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "用户不存在");
        }

        List<UserModelQuotaVO> quotas = tokenQuotaService.getAllModelQuotas(userId);
        return ResVo.ok(quotas);
    }

    /**
     * 管理员：为新用户初始化配额
     */
    @PostMapping("/init/{userId}")
    @Permission(role = UserRole.ADMIN)
    public ResVo<Void> initUserQuota(@PathVariable Long userId) {
        // 检查用户是否存在
        if (userService.queryBasicUserInfo(userId) == null) {
            return ResVo.fail(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "用户不存在");
        }

        try {
            tokenQuotaService.initUserQuota(userId);
            log.info("User quota initialized by admin: userId={}", userId);
            return ResVo.ok(null);
        } catch (Exception e) {
            log.error("Failed to initialize user quota", e);
            return ResVo.fail(StatusEnum.UNEXPECT_ERROR, "初始化失败: " + e.getMessage());
        }
    }
}
