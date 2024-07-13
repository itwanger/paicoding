package com.github.paicoding.forum.web.global.vo;

import com.github.paicoding.forum.api.model.vo.ResVo;
import com.github.paicoding.forum.api.model.vo.Status;
import com.github.paicoding.forum.api.model.vo.constants.StatusEnum;
import lombok.Data;

/**
 * @program: tech-pai
 * @description:
 * @author: XuYifei
 * @create: 2024-06-19
 */
@Data
public class ResultVo<T> extends ResVo<T> {
    private static final long serialVersionUID = -510306209659393854L;

    private GlobalVo global = null;
    private boolean redirect = false;


    public ResultVo() {
    }

    public ResultVo(Status status) {
        this.status = status;
    }

    public ResultVo(T t) {
        status = Status.newStatus(StatusEnum.SUCCESS);
        this.result = t;
    }
    public ResultVo(T t, GlobalVo globalVo) {
        status = Status.newStatus(StatusEnum.SUCCESS);
        this.global = globalVo;
        this.result = t;
    }

    public ResultVo(boolean redirect) {
        status = Status.newStatus(StatusEnum.SUCCESS);
        this.redirect = redirect;
    }

    public ResultVo(T t, boolean redirect) {
        status = Status.newStatus(StatusEnum.SUCCESS);
        this.redirect = redirect;
        this.result = t;
    }

    public static <T> ResultVo<T> ok(T t, GlobalVo globalVo) {
        return new ResultVo<T>(t, globalVo);
    }

    public static <T> ResultVo<T> ok(T t){
        return new ResultVo<>(t);
    }

    public static <T> ResultVo<T> ok(boolean redirect){
        return new ResultVo<>(redirect);
    }

    public static <T> ResultVo<T> ok(T t, boolean redirect){
        return new ResultVo<>(t, redirect);
    }


    @SuppressWarnings("unchecked")
    public static <T> ResultVo<T> fail(StatusEnum status, Object... args) {
        return new ResultVo<>(Status.newStatus(status, args));
    }

    public static <T> ResultVo<T> fail(Status status) {
        return new ResultVo<>(status);
    }
}
