package com.github.paicoding.forum.service.image.service;

import jakarta.servlet.http.HttpServletRequest;

/**
 * @author XuYifei
 * @date 2024-07-12
 */
public interface ImageService {
    /**
     * 图片转存
     * @param content
     * @return
     */
    String mdImgReplace(String content);


    /**
     * 外网图片转存
     *
     * @param img
     * @return
     */
    String saveImg(String img);

    /**
     * 保存图片
     *
     * @param request
     * @return
     */
    String saveImg(HttpServletRequest request);
}
