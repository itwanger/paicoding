package com.github.paicoding.forum.service.config.repository.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.github.paicoding.forum.api.model.entity.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 评论表
 *
 * @author XuYifei
 * @date 2024-07-12
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("global_conf")
public class GlobalConfigDO extends BaseDO {
    private static final long serialVersionUID = -6122208316544171301L;

    // 配置项名称
    @TableField("`key`")
    private String key;
    // 配置项值
    private String value;
    // 备注
    private String comment;
    // 删除
    private Integer deleted;
}
