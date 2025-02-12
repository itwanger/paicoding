package com.github.paicoding.forum.api.model.vo.shortlink;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
/**
 * 短链接接口参数
 *
 * @author betasecond
 * @date 2025-02-13
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShortLinkReq {
    private String originalUrl;
    private String username;
    private String thirdPartyUserId;
    private String userAgent;
    private String loginMethod;
}
