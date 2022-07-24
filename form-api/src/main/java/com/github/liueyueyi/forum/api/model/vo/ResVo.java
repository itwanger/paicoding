package com.github.liueyueyi.forum.api.model.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * @author YiHui
 * @date 2022/7/6
 */
@Data
public class ResVo<T> implements Serializable {
    private static final long serialVersionUID = -510306209659393854L;

    private Status status;
    private T result;


    public ResVo() {
    }

    public ResVo(int code, String msg) {
        status = new Status(code, msg);
    }


    public ResVo(T t) {
        status = Status.newStatus(0, "ok");
        this.result = t;
    }


    public static <T> ResVo<T> successReturn(T t) {
        return new ResVo<T>(t);
    }

    @SuppressWarnings("unchecked")
    public static <T> ResVo<T> errorReturn(Status status, String... msgs) {
        String msg;
        if (msgs.length > 0) {
            msg = String.format(status.getMsg(), msgs);
        } else {
            msg = status.getMsg();
        }
        return new ResVo<T>(status.getCode(), msg);
    }
}
