package com.github.liuyueyi.forum.service.article.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author YiHui
 * @date 2022/7/24
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TagDTO implements Serializable {
    private static final long serialVersionUID = -8614833588325787479L;

    private Long categoryId;

    private Long tagId;

    private String tag;

    public TagDTO(Long tagId) {
        this.tagId = tagId;
    }
}
