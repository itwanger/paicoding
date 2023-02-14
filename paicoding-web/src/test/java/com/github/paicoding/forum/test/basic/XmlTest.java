package com.github.paicoding.forum.test.basic;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.github.paicoding.forum.api.model.vo.user.wx.WxImgTxtItemVo;
import com.github.paicoding.forum.api.model.vo.user.wx.WxImgTxtMsgResVo;
import com.github.paicoding.forum.service.sitemap.model.SiteMapVo;
import com.github.paicoding.forum.service.sitemap.model.SiteUrlVo;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author YiHui
 * @date 2023/2/14
 */
public class XmlTest {

    @Test
    public void test2xml() throws JsonProcessingException {
        WxImgTxtMsgResVo vo = new WxImgTxtMsgResVo();
        vo.setFromUserName("一会");
        vo.setToUserName("dd");
        vo.setCreateTime(System.currentTimeMillis() / 1000);
        vo.setArticleCount(1);

        List<WxImgTxtItemVo> itemList = new ArrayList<>();
        WxImgTxtItemVo item = new WxImgTxtItemVo();
        item.setTitle("haha");
        item.setDescription("miaos");
        item.setPicUrl("123");
        item.setUrl("456");
        itemList.add(item);
        vo.setArticles(itemList);

        XmlMapper mapper = new XmlMapper();
        String ans = mapper.writeValueAsString(vo);
        System.out.println(ans);
    }

    @Test
    public void testSiteMap() throws JsonProcessingException {
        SiteUrlVo vo = new SiteUrlVo();
        vo.setLoc("https://paicoding.com/article/detail/169");
        vo.setLastMod("2023-02-13");

        SiteUrlVo vo2 = new SiteUrlVo();
        vo2.setLoc("https://paicoding.com");
        vo2.setLastMod("2023-02-13");

        SiteMapVo result = new SiteMapVo();
        result.setUrl(Arrays.asList(vo, vo2));
        XmlMapper mapper = new XmlMapper();
        String ans = mapper.writeValueAsString(result);
        ans = ans.replaceAll("><", ">\n<");
        System.out.println(ans);
    }
}
