package com.github.paicoding.forum.service.sensitive.service.impl;

import com.github.paicoding.forum.api.model.vo.user.dto.BaseUserInfoDTO;
import com.github.paicoding.forum.core.permission.UserRole;
import com.github.paicoding.forum.service.chatai.bot.AiBots;
import com.github.paicoding.forum.service.sensitive.service.SensitiveBypassService;
import com.github.paicoding.forum.service.user.service.AuthorWhiteListService;
import com.github.paicoding.forum.service.user.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 敏感词处理豁免服务实现
 *
 * @author Codex
 * @date 2026/3/25
 */
@Service
public class SensitiveBypassServiceImpl implements SensitiveBypassService {

    @Autowired
    private AuthorWhiteListService authorWhiteListService;

    @Autowired
    private AiBots aiBots;

    @Autowired
    private UserService userService;

    @Override
    public boolean shouldBypassByUserId(Long userId) {
        if (userId == null) {
            return false;
        }
        if (aiBots.aiBots(userId)) {
            return true;
        }
        if (authorWhiteListService.authorInArticleWhiteList(userId)) {
            return true;
        }
        BaseUserInfoDTO user = userService.queryBasicUserInfo(userId);
        return user != null && StringUtils.equalsIgnoreCase(user.getRole(), UserRole.ADMIN.name());
    }
}
