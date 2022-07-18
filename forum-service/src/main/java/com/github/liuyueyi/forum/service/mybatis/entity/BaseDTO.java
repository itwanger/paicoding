package com.github.liuyueyi.forum.service.mybatis.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.util.Date;

@Data
public class BaseDTO {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer deleted;

    private Date createTime;

    private Date updateTime;
}
