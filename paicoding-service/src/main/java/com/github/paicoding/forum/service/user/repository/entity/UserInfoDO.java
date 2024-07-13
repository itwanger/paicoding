package com.github.paicoding.forum.service.user.repository.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.github.paicoding.forum.api.model.entity.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户个人信息表
 *
 * @author XuYifei
 * @date 2024-07-12
 */
@Data
@EqualsAndHashCode(callSuper = true)
// autoResultMap 必须存在，否则ip对象无法正确获取
@TableName(value = "user_info", autoResultMap = true)
public class UserInfoDO extends BaseDO {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 用户图像
     */
    private String photo;

    /**
     * 职位
     */
    private String position;

    /**
     * 公司
     */
    private String company;

    /**
     * 个人简介
     */
    private String profile;

    /**
     * 扩展字段
     */
    private String extend;

    /**
     * 删除标记
     */
    private Integer deleted;

    /**
     * 0 普通用户
     * 1 超级管理员
     */
    private Integer userRole;

    /**
     * ip信息
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private IpInfo ip;

    public IpInfo getIp() {
        if (ip == null) {
            ip = new IpInfo();
        }
        return ip;
    }
}
