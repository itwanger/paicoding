package com.github.liuyueyi.forum.core.model.res;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author YiHui
 * @date 2022/7/6
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Status {

    private int code;

    private String msg;

    public static Status newStatus(int code, String msg) {
        return new Status(code, msg);
    }
}
