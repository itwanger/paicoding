package com.github.liuyueyi.forum.service.image.service;

import javax.servlet.http.HttpServletRequest;

/**
 * @author LouZai
 * @date 2022/9/7
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
     * 上传图片转存
     *
     * @param request
     * @return
     */
    String saveImg(HttpServletRequest request);
}
