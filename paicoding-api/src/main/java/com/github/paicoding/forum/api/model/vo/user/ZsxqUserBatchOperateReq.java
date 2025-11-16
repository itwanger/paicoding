package com.github.paicoding.forum.api.model.vo.user;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class ZsxqUserBatchOperateReq implements Serializable {
    // ids
    private List<Long> ids;
    // 状态
    private Integer status;
}
