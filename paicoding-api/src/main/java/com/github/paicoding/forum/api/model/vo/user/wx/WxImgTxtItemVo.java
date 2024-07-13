package com.github.paicoding.forum.api.model.vo.user.wx;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Data;

/**
 * 返回的数据结构体
 * <p>
 *
 * @author XuYifei
 * @date 2024-07-12
 */
@Data
@JacksonXmlRootElement(localName = "item")
public class WxImgTxtItemVo {

    @JacksonXmlProperty(localName = "Title")
    private String title;
    @JacksonXmlProperty(localName = "Description")
    private String description;
    @JacksonXmlProperty(localName = "PicUrl")
    private String picUrl;
    @JacksonXmlProperty(localName = "Url")
    private String url;
}
