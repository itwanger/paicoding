package com.github.paicoding.forum.api.model.enums.resume;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 简历邮件回复状态
 *
 * @author YiHui
 * @date 2024/8/7
 */
@AllArgsConstructor
@Getter
public enum ResumeEmailStateEnum {
    NOT_REPLAY(-1, "未回复"),
    UPLOAD_REPLAY(0, "上传-已回复"),
    PROCESSING_REPLAY(1, "处理中-已回复"),
    DONE_REPLAY(2, "已处理-已回复"),
    ;

    private int state;
    private String desc;

}
