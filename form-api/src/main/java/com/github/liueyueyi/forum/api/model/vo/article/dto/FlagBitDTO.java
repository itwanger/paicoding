package com.github.liueyueyi.forum.api.model.vo.article.dto;

import com.github.liueyueyi.forum.api.model.vo.user.dto.ArticleFootCountDTO;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 文章信息
 * <p>
 * DTO 定义返回给web前端的实体类 (VO)
 *
 * @author YiHui
 * @date 2022/7/24
 */
@Data
public class FlagBitDTO {

    /**
     * 标记位
     */
    private Integer flagBit;

    /**
     * 是否正向操作
     */
    private Boolean forward;


    public FlagBitDTO(Integer flagBit, Boolean forward) {
        this.flagBit =  flagBit;
        this.forward = forward;
    }
}
