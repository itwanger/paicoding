package com.github.paicoding.forum.api.model.exception;

import com.github.paicoding.forum.api.model.vo.constants.StatusEnum;

/**
 * @author XuYifei
 * @date 2024-07-12
 */
public class ExceptionUtil {

    public static ForumException of(StatusEnum status, Object... args) {
        return new ForumException(status, args);
    }

}
