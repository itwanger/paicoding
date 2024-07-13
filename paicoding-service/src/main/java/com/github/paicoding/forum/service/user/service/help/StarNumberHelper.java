package com.github.paicoding.forum.service.user.service.help;

import com.github.paicoding.forum.service.user.service.conf.AiConfig;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;

/**
 * 密码加密器，后续接入SpringSecurity之后，可以使用 PasswordEncoder 进行替换
 *
 * @author XuYifei
 * @date 2024-07-12
 */
@Component
public class StarNumberHelper {
    @Resource
    private AiConfig aiConfig;

    public Boolean checkStarNumber(String starNumber) {
        // 判断编号是否在 0 - maxStarNumber 之间
        return Integer.parseInt(starNumber) >= 0 && Integer.parseInt(starNumber) <= aiConfig.getMaxNum().getStarNumber();
    }

}
