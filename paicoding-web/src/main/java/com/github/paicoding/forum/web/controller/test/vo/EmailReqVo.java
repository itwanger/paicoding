package com.github.paicoding.forum.web.controller.test.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 邮件发送验证
 *
 * @author XuYifei
 * @date 2024-07-12
 */
@Data
public class EmailReqVo implements Serializable {
    private static final long serialVersionUID = -8560585303684975482L;

    private String to;

    private String title;

    private String content;

}
