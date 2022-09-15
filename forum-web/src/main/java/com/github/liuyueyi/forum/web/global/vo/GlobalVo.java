package com.github.liuyueyi.forum.web.global.vo;

import com.github.liueyueyi.forum.api.model.vo.user.dto.BaseUserInfoDTO;
import com.github.liuyueyi.forum.web.config.GlobalViewConfig;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author YiHui
 * @date 2022/9/3
 */
@Data
public class GlobalVo {
    /**
     * 网站相关配置
     */
    private GlobalViewConfig siteInfo;
    /**
     * 环境
     */
    private String env;

    /**
     * 是否已登录
     */
    private Boolean isLogin;

    /**
     * 登录用户信息
     */
    private BaseUserInfoDTO user;

    /**
     * 消息通知数量
     */
    private Integer msgNum;


    private String currentDomain;
}
