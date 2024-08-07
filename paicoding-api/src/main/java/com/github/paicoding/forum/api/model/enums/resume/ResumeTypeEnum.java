package com.github.paicoding.forum.api.model.enums.resume;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 简历处理状态
 *
 * @author YiHui
 * @date 2024/8/7
 */
@AllArgsConstructor
@Getter
public enum ResumeTypeEnum {
    UNPROCESS(0, "未处理"),
    PROCESSING(1, "处理中"),
    DONE(2, "已处理"),
    ;

    private int type;
    private String desc;

}
