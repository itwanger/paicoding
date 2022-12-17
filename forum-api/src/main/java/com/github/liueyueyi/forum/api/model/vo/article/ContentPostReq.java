package com.github.liueyueyi.forum.api.model.vo.article;

import com.github.liueyueyi.forum.api.model.enums.PushStatusEnum;
import lombok.Data;

import java.io.Serializable;
import java.util.Set;

/**
 * 发布文章请求参数
 *
 * @author YiHui
 * @date 2022/7/24
 */
@Data
public class ContentPostReq implements Serializable {
    /**
     * 正文内容
     */
    private String content;
}