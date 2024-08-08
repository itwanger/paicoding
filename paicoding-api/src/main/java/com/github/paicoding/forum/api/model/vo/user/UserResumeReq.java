package com.github.paicoding.forum.api.model.vo.user;

import com.github.paicoding.forum.api.model.vo.PageParam;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * 简历查询
 *
 * @author YiHui
 * @date 2024/8/7
 */
@Data
@ApiModel("用户简历信息查询请求参数")
public class UserResumeReq extends PageParam {
    /**
     * 用户检索
     */
    @ApiModelProperty("用户id")
    private Long userId;

    /**
     * 用户批量查询
     */
    private List<Long> users;
    /**
     * 用户名检索
     */
    @ApiModelProperty("用户名（精确查询）")
    private String uname;
    /**
     * 状态过滤
     */
    @ApiModelProperty("状态：0-未处理 1-处理中 2-已处理")
    private Integer type;

    /**
     * 1 表示根据时间倒排
     * 0 or null 表示根据时间正向排序
     */
    @ApiModelProperty("0/null-根据提交时间正向排序 1-根据时间倒排")
    private Integer sort;
}
