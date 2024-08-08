package com.github.paicoding.forum.api.model.vo.user.dto;

import lombok.Data;

/**
 * 用户的简历信息
 *
 * @author YiHui
 * @date 2024/8/7
 */
@Data
public class UserResumeInfoDTO {
    /**
     * 简历信息
     */
    private ResumeDTO resume;
    /**
     * 用户信息
     */
    private SimpleUserInfoDTO user;
}
