package com.github.liueyueyi.forum.api.model.vo.sidebar;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 侧边推广信息
 *
 * @author YiHui
 * @date 2022/9/6
 */
@Data
@Accessors(chain = true)
public class SideBarItemDto {

    private String title;

    private String url;

    private Long time;
}
