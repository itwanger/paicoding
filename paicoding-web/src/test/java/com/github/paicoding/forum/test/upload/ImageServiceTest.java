package com.github.paicoding.forum.test.upload;

import com.github.paicoding.forum.service.image.service.ImageServiceImpl;
import com.github.paicoding.forum.test.BasicTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.WebApplicationContext;

/**
 * @author eventime
 * @date 2025/2/11
 */

public class ImageServiceTest extends BasicTest {
    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ImageServiceImpl imageService;

    @Test
    public void testSaveImage() throws Exception {
        //  测试转存外链图像, 相同图像是否转存为同一个地址
        System.out.println(imageService.saveImg("https://www.baidu.com/img/flexible/logo/pc/peak-result.png"));
        System.out.println(imageService.saveImg("https://www.baidu.com/img/flexible/logo/pc/peak-result.png"));
    }

    @Test
    public void testAsyncSaveImage() throws Exception {
        String content = "这是一段测试的 Markdown 内容，其中包含一张图片。\n" +
                "![百度logo](https://www.baidu.com/img/flexible/logo/pc/peak-result.png)\n" +
                "![百度logo](https://www.baidu.com/img/flexible/logo/pc/peak-result.png)\n" +
                "![百度logo](https://www.baidu.com/img/flexible/logo/pc/peak-result.png)\n" +
                "图片显示的是百度的 logo。";
        content = imageService.mdImgReplace(content);
        System.out.println(content);
    }


}
