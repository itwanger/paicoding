package com.github.paicoding.forum.web.common.image.rest;

import com.github.paicoding.forum.api.model.vo.ResVo;
import com.github.paicoding.forum.api.model.vo.constants.StatusEnum;
import com.github.paicoding.forum.service.image.service.ImageService;
import com.github.paicoding.forum.web.common.image.vo.OssVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartHttpServletRequest;

/**
 * @author YiHui
 * @date 2024/8/7
 */
@Slf4j
@RestController
@RequestMapping(path = "oss")
public class OssRestController {
    @Autowired
    private ImageService imageService;

    /**
     * 图片上传
     *
     * @return
     */

    @PostMapping(path = "upload")
    public ResVo<OssVo> upload(MultipartHttpServletRequest request) {
        OssVo vo = new OssVo();
        try {
            String imagePath = imageService.saveFile(request);
            vo.setOssPath(imagePath);
            vo.setOssName(request.getFile("file").getOriginalFilename());
        } catch (Exception e) {
            log.error("save upload file error!", e);
            return ResVo.fail(StatusEnum.UPLOAD_PIC_FAILED);
        }
        return ResVo.ok(vo);
    }

}
