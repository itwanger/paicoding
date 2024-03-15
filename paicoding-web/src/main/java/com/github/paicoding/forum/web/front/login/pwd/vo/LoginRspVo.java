package com.github.paicoding.forum.web.front.login.pwd.vo;

import com.github.paicoding.forum.api.model.vo.user.dto.BaseUserInfoDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 等候返回结果
 *
 * @author YiHui
 * @date 2024/3/15
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginRspVo implements Serializable {
    private static final long serialVersionUID = 5579135364013239907L;

    private String token;

    private BaseUserInfoDTO user;
}
