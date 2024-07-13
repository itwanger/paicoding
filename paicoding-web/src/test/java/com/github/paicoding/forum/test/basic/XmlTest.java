package com.github.paicoding.forum.test.basic;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.github.paicoding.forum.api.model.vo.user.wx.WxImgTxtItemVo;
import com.github.paicoding.forum.api.model.vo.user.wx.WxImgTxtMsgResVo;
import com.github.paicoding.forum.service.sitemap.model.SiteMapVo;
import com.github.paicoding.forum.service.sitemap.model.SiteUrlVo;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author XuYifei
 * @date 2024-07-12
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

    public static XMLReader getInstance() throws Exception {
        // javax.xml.parsers.SAXParserFactory 原生api获取factory
        SAXParserFactory factory = SAXParserFactory.newInstance();
        // javax.xml.parsers.SAXParser 原生api获取parse
        SAXParser saxParser = factory.newSAXParser();
        // 获取xml
        return saxParser.getXMLReader();
    }

    public static class MyHander extends DefaultHandler {

        private LinkedHashMap<String, String> sqlMap = new LinkedHashMap<>();

        private String currentId;

        @Override
        public void startElement(String uri, String localName, String qName, org.xml.sax.Attributes attributes) throws SAXException {
            if (qName.equals("changeSet")) {
                // attributes 是element的属性，类似id这种
                currentId = attributes.getValue("id");
            } else if (qName.equals("sqlFile")) {
                sqlMap.put(currentId, attributes.getValue("path"));
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) {
            currentId = "";
        }

        public LinkedHashMap<String, String> getSqlMap() {
            return sqlMap;
        }
    }

    @Test
    public void testParseXml() throws Exception {
        XMLReader xmlReader = getInstance();
        // 注册自定义解析器
        MyHander myHander = new MyHander();
        xmlReader.setContentHandler(myHander);
        // 解析xml
        xmlReader.parse(new ClassPathResource("liquibase/changelog/000_initial_schema.xml").getFile().getPath());
        // 获取解析结果
        LinkedHashMap<String, String> res = myHander.getSqlMap();
        System.out.println(res);
    }
}
