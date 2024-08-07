package com.github.paicoding.forum.api.model.vo.user;

import com.github.paicoding.forum.api.model.vo.PageParam;
import lombok.Data;

/**
 * 简历查询
 *
 * @author YiHui
 * @date 2024/8/7
 */
@Data
public class UserResumeReq extends PageParam {
    /**
     * 用户检索
     */
    private Long userId;
    /**
     * 用户名检索
     */
    private String uname;
    /**
     * 状态过滤
     */
    private Integer type;

    /**
     * 1 表示根据时间倒排
     * 0 or null 表示根据时间正向排序
     */
    private Integer sort;
}
