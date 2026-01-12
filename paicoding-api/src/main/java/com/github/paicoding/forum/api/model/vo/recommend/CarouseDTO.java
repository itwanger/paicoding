package com.github.paicoding.forum.api.model.vo.recommend;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.github.paicoding.forum.api.model.util.cdn.CdnImgSerializer;
import com.github.paicoding.forum.api.model.util.cdn.CdnUtil;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @author YiHui
 * @date 2022/9/7
 */
@Data
@Accessors(chain = true)
public class CarouseDTO implements Serializable {

    private static final long serialVersionUID = 1048555496974144842L;
    /**
     * 说明
     */
    private String name;
    /**
     * 图片地址
     */
    @JsonSerialize(using = CdnImgSerializer.class)
    private String imgUrl;
    /**
     * 跳转地址
     */
    private String actionUrl;

    public CarouseDTO setImgUrl(String imgUrl) {
        this.imgUrl = CdnUtil.autoTransCdn(imgUrl);
        return this;
    }
}
