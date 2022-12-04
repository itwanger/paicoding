package com.github.liuyueyi.forum.service.image.service;

import com.github.hui.quick.plugin.base.ImageLoadUtil;
import com.github.hui.quick.plugin.base.constants.MediaType;
import com.github.liuyueyi.forum.core.config.ImageProperties;
import com.github.liuyueyi.forum.core.util.LocalDateTimeUtil;
import com.github.liuyueyi.forum.core.util.MdImgLoader;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Random;

/**
 * @author LouZai
 * @date 2022/9/7
 */
@Slf4j
@Service
@EnableConfigurationProperties(ImageProperties.class)
public class ImageServiceImpl implements ImageService {

    @Autowired
    private ImageProperties imageProperties;

    private static final MediaType[] STATIC_IMG_TYPE = new MediaType[]{MediaType.ImagePng, MediaType.ImageJpg, MediaType.ImageWebp};


    @Override
    public String saveImg(HttpServletRequest request) {
        MultipartFile file = null;
        if (request instanceof MultipartHttpServletRequest) {
            file = ((MultipartHttpServletRequest) request).getFile("image");
        }
        if (file == null) {
            throw new IllegalArgumentException("请指定上传的图片!");
        }

        // 目前只支持 jpg, png, webp 等静态图片格式
        String contentType = file.getContentType();
        MediaType type = validateStaticImg(contentType);
        if (type == null) {
            throw new IllegalArgumentException("不支持的图片类型");
        }

        // 获取BufferedImage对象
        try {
            BufferedImage img = ImageIO.read(file.getInputStream());
            String path = saveImg(img, type);
            if (path == null) {
                throw new IllegalStateException("图片上传失败!");
            }
            return imageProperties.getCdnHost() + path;
        } catch (IOException e) {
            log.error("Parse img from httpRequest to BufferedImage error! e:", e);
            throw new IllegalArgumentException("不支持的图片类型!");
        }
    }

    @Override
    public String mdImgReplace(String content) {
        List<MdImgLoader.MdImg> imgList = MdImgLoader.loadImgs(content);
        for (MdImgLoader.MdImg img : imgList) {
            // fixme 下面可以调整为并发转存
            if ((StringUtils.isNotBlank(imageProperties.getCdnHost()) && img.getUrl().startsWith(imageProperties.getCdnHost()))
                    || !img.getUrl().startsWith("http")) {
                // 已经转存过，不需要再次转存；非http图片，不处理
                continue;
            }

            String newImg = imageProperties.getCdnHost() + saveImg(img.getUrl());
            content = StringUtils.replace(content, img.getOrigin(), "![" + img.getDesc() + "](" + newImg + ")");
        }
        return content;
    }

    /**
     * 外网图片转存
     *
     * @param img
     * @return
     */
    public String saveImg(String img) {
        try {
            BufferedImage bufferedImage = ImageLoadUtil.getImageByPath(img);
            return saveImg(bufferedImage, MediaType.ImagePng);
        } catch (Exception e) {
            log.error("外网图片转存异常! img:{}", img, e);
            return null;
        }
    }

    public String saveImg(BufferedImage bf, MediaType mediaType) {
        try {
            String path = genTmpImg(mediaType.getExt());
            File file = new File(imageProperties.getAbsTmpPath() + path);
            mkDir(file.getParentFile());
            ImageIO.write(bf, mediaType.getExt(), file);
            return path;
        } catch (Exception e) {
            log.error("save file error!", e);
            return null;
        }
    }

    /**
     * 获取文件临时名称
     *
     * @return
     */
    private String genTmpFileName() {
        Random random = new Random();
        return System.currentTimeMillis() + "_" + random.nextInt(100);
    }

    /**
     * 获取文件路径
     *
     * @param type
     * @return
     */
    public String genTmpImg(String type) {
        String time = genTmpFileName();
        return imageProperties.getWebImgPath() + LocalDateTimeUtil.getCurrentDateTime() + "/" + time + "." + type;
    }

    /**
     * 递归创建文件夹
     *
     * @param path 由目录创建的file对象
     * @throws FileNotFoundException
     */
    private void mkDir(File path) throws FileNotFoundException {
        if (path.getParentFile() == null) {
            path = path.getAbsoluteFile();
        }

        if (path.getParentFile() == null) {
            // windows 操作系统下，如果直接到最上层的分区，这里依然可能是null，所以直接返回
            return;
        }

        if (path.getParentFile().exists()) {
            modifyFileAuth(path);
            if (!path.exists() && !path.mkdir()) {
                throw new FileNotFoundException();
            }
        } else {
            mkDir(path.getParentFile());
            modifyFileAuth(path);
            if (!path.exists() && !path.mkdir()) {
                throw new FileNotFoundException();
            }
        }
    }

    /**
     * 修改文件权限，设置为可读写
     *
     * @param file
     */
    private void modifyFileAuth(File file) {
        boolean ans = file.setExecutable(true, false);
        ans = file.setReadable(true, false) && ans;
        ans = file.setWritable(true, false) && ans;
        if (log.isDebugEnabled()) {
            log.debug("create file auth : {}", ans);
        }
    }

    /**
     * 图片格式校验
     *
     * @param mime
     * @return
     */
    private MediaType validateStaticImg(String mime) {
        if (mime.contains(MediaType.ImageJpg.getExt())) {
            mime = mime.replace("jpg", "jpeg");
        }
        for (MediaType type : STATIC_IMG_TYPE) {
            if (type.getMime().equals(mime)) {
                return type;
            }
        }
        return null;
    }
}
