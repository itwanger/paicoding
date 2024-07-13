package com.github.paicoding.forum.api.model.vo.user.wx;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Data;
import lombok.ToString;

/**
 * 返回的数据结构体
 * <p>
 * @author XuYifei
 * @date 2024-07-12
 */
@Data
@ToString(callSuper = true)
@JacksonXmlRootElement(localName = "xml")
public class WxTxtMsgResVo extends BaseWxMsgResVo {
    @JacksonXmlProperty(localName = "Content")
    private String content;

    public WxTxtMsgResVo() {
        setMsgType("text");
    }
}
