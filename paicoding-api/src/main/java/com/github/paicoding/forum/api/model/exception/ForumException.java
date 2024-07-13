package com.github.paicoding.forum.api.model.exception;

import com.github.paicoding.forum.api.model.vo.Status;
import com.github.paicoding.forum.api.model.vo.constants.StatusEnum;
import lombok.Getter;

/**
 * 业务异常
 *
 * @author XuYifei
 * @date 2024-07-12
 */
public class ForumException extends RuntimeException {
    @Getter
    private Status status;

    public ForumException(Status status) {
        this.status = status;
    }

    public ForumException(int code, String msg) {
        this.status = Status.newStatus(code, msg);
    }

    public ForumException(StatusEnum statusEnum, Object... args) {
        this.status = Status.newStatus(statusEnum, args);
    }

}
