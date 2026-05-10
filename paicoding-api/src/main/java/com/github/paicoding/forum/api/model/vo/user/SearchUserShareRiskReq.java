package com.github.paicoding.forum.api.model.vo.user;

import lombok.Data;

/**
 * 疑似共享账号查询
 *
 * @author Codex
 * @date 2026/4/25
 */
@Data
public class SearchUserShareRiskReq {
    private String loginName;
    private String starNumber;
    private Integer recentDays;
    private Integer minKickoutCount;
    private Integer minDeviceCount;
    private Integer minIpCount;
    private Long pageNumber;
    private Long pageSize;
}
