package com.github.paicoding.forum.api.model.vo.user.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 用户的简历信息
 *
 * @author YiHui
 * @date 2024/8/7
 */
@Data
@ApiModel("简历实体")
public class ResumeDTO {
    @ApiModelProperty("简历ID")
    private Long resumeId;
    @ApiModelProperty("用户上传的描述")
    private String mark;
    @ApiModelProperty("简历名")
    private String resumeName;
    @ApiModelProperty("简历附件")
    private String resumeUrl;
    @ApiModelProperty("用于接收回信的邮箱地址")
    private String replayEmail;
    @ApiModelProperty("回复内容")
    private String replay;
    @ApiModelProperty("更新后的简历附件")
    private String replayUrl;
    @ApiModelProperty("处理状态 0-未处理 1-处理中 2-已处理")
    private Integer type;
    @ApiModelProperty("上传时间")
    private Long createTime;
    @ApiModelProperty("最后更新时间")
    private Long updateTime;
    @ApiModelProperty("邮件状态")
    private Integer emailState;
}
