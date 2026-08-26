package com.github.paicoding.forum.api.model.vo.config;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 顶部导航配置，items 的数组顺序就是页面展示顺序。
 */
@Data
public class NavbarConfigDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<NavbarItemDTO> items = new ArrayList<>();
}
