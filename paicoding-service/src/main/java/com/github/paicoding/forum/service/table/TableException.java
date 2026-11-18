package com.github.paicoding.forum.service.table;

import com.github.paicoding.forum.api.model.exception.ForumException;

public class TableException extends ForumException {
    private final int httpStatus;

    public TableException(int httpStatus, String message) {
        super(httpStatus, message);
        this.httpStatus = httpStatus;
    }

    public int getHttpStatus() {
        return httpStatus;
    }
}
