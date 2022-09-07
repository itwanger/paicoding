package com.github.liuyueyi.forum.web.front.image.rest;

import com.github.liueyueyi.forum.api.model.vo.ResVo;
import com.github.liuyueyi.forum.service.image.impl.ImageServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.awt.image.BufferedImage;

/**
 * 返回json格式数据
 *
 * @author LouZai
 * @date 2022/9/7
 */
@RequestMapping(path = "image/")
@RestController
@Slf4j
public class ImageRestController {

    @Autowired
    private ImageServiceImpl imageServiceImpl;

    /**
     * 图片上传
     *
     * @return
     */
    @ResponseBody
    @GetMapping(path = "upload")
    public ResVo<String> upload(HttpServletRequest request) {
        String imagePath = Strings.EMPTY;
        try {
            BufferedImage img = imageServiceImpl.getImg(request);
            imagePath = imageServiceImpl.saveImg(img);
        } catch (Exception e) {
            log.error("save upload file error! e: {}", e);
        }
        return ResVo.ok(imagePath);
    }
}
