package com.github.paicoding.forum.service.user.service.conf;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 登录风控配置
 *
 * @author Codex
 * @date 2026/4/23
 */
@Data
@Component
@ConfigurationProperties(prefix = "paicoding.login.risk")
public class LoginRiskProperties {
    /**
     * 单账号允许的并发设备数
     */
    private Integer maxActiveDevices = 2;

    /**
     * 会话心跳同步到数据库的最小间隔，单位秒
     */
    private Integer touchSyncSeconds = 300;
}
