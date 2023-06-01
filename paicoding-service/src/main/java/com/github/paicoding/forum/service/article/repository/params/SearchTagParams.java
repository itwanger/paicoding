package com.github.paicoding.forum.service.article.repository.params;

import com.github.paicoding.forum.api.model.vo.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 微信搜索「沉默王二」，回复 Java
 *
 * @author 沉默王二
 * @date 5/29/23
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SearchTagParams extends PageParam {
    // 标签名称
    private String tag;
}
