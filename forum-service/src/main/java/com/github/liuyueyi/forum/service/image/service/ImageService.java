package com.github.liuyueyi.forum.service.image.service;

import javax.servlet.http.HttpServletRequest;
import java.awt.image.BufferedImage;

/**
 * @author LouZai
 * @date 2022/9/7
 */
public interface ImageService {

    /**
     * 获取图片
     *
     * @param request
     * @return
     */
    BufferedImage getImg(HttpServletRequest request);

    /**
     * 图片本地保存
     *
     * @param bf
     * @return
     */
    String saveImg(BufferedImage bf);
}
