package com.github.paicoding.forum.api.model.vo.user;

import lombok.Data;

@Data
public class SearchZsxqUserReq {
    // 用户昵称
    private String name;
    // 星球编号
    private String starNumber;
    // 用户登录名
    private String userCode;

    private Integer state;
    // 分页
    private Long pageNumber;
    private Long pageSize;
}
