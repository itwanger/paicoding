package com.github.paicoding.forum.api.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author XuYifei
 * @date 2024-07-12
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NextPageHtmlVo implements Serializable {
    private String html;
    private Boolean hasMore;
}
