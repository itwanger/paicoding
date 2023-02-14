package com.github.paicoding.forum.test.basic;

import com.github.paicoding.forum.core.util.ArticleUtil;
import com.github.paicoding.forum.core.util.MdImgLoader;
import org.junit.Test;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ImgPickerTest {

    @Test
    public void testLoad() {
        // markdown中的图片识别
        String pattern = "!\\[(.*?)\\]\\((.*?)\\)";
        String text = "hello ![](https://text.jpg) world!![描述](http))!图片";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(text);
        while (m.find()) {
            String ans = m.group(0);
            System.out.println(ans);
        }
    }

    @Test
    public void testMdImgLoad() {
        String text = "hello ![](https://text.jpg) world!![描述](http))!图片";
        List<MdImgLoader.MdImg> list = MdImgLoader.loadImgs(text);
        System.out.println(list);
    }


    @Test
    public void testArticleSummaryGen() {
        String txt = "hello 这是要给简单的测试 ![](https://text.jpg) <br/> 这是一个超链 [我的链接](https://www.hhui.top) 哈哈哈 <b>加粗</b>见到了附件123132131!";
        String ans = ArticleUtil.pickSummary(txt);
        System.out.println(ans);
    }

}
