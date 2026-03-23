package com.github.paicoding.forum.api.model.vo.chat;

import lombok.Data;

import java.io.Serializable;

/**
 * 聊天页可选模型
 *
 * @author Codex
 * @date 2026/3/23
 */
@Data
public class ChatSourceOptionDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer code;
    private String value;
    private String name;
}
