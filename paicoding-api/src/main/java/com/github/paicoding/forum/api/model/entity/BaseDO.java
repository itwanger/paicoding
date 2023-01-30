package com.github.paicoding.forum.api.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class BaseDO implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Date createTime;

    private Date updateTime;
}
