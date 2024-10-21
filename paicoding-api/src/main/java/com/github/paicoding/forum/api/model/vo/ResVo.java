package com.github.paicoding.forum.api.model.vo;

import com.github.paicoding.forum.api.model.vo.constants.StatusEnum;
import lombok.Data;

import java.io.Serializable;

/**
 * @author XuYifei
 * @date 2024-07-12
 */
@Data
public class ResVo<T> implements Serializable {
    private static final long serialVersionUID = -510306209659393854L;
    protected Status status;

    protected T result;


    public ResVo() {
    }

    public ResVo(Status status) {
        this.status = status;
    }

    public ResVo(T t) {
        status = Status.newStatus(StatusEnum.SUCCESS);
        this.result = t;
    }

    public static <T> ResVo<T> ok(T t) {
        return new ResVo<T>(t);
    }

    @SuppressWarnings("unchecked")
    public static <T> ResVo<T> fail(StatusEnum status, Object... args) {
        return new ResVo<>(com.github.paicoding.forum.api.model.vo.Status.newStatus(status, args));
    }

    public static <T> ResVo<T> fail(Status status) {
        return new ResVo<>(status);
    }
}
