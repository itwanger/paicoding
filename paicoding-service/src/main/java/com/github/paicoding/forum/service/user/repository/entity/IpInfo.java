package com.github.paicoding.forum.service.user.repository.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * ip信息
 *
 * @author XuYifei
 * @date 2024-07-12
 */
@Data
public class IpInfo implements Serializable {
    private static final long serialVersionUID = -4612222921661930429L;

    private String firstIp;

    private String firstRegion;

    private String latestIp;

    private String latestRegion;
}