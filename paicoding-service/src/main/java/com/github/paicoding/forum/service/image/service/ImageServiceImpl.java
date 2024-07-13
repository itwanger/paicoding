package com.github.paicoding.forum.service.image.service;

import com.github.hui.quick.plugin.base.constants.MediaType;
import com.github.hui.quick.plugin.base.file.FileReadUtil;
import com.github.paicoding.forum.api.model.exception.ExceptionUtil;
import com.github.paicoding.forum.api.model.vo.constants.StatusEnum;
import com.github.paicoding.forum.core.async.AsyncExecute;
import com.github.paicoding.forum.core.async.AsyncUtil;
import com.github.paicoding.forum.core.mdc.MdcDot;
import com.github.paicoding.forum.core.util.MdImgLoader;
import com.github.paicoding.forum.service.image.oss.ImageUploader;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Maps;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author XuYifei
 * @date 2024-07-12
 */
@Slf4j
@Service
public class ImageServiceImpl implements ImageService {

    @Autowired
    private ImageUploader imageUploader;

    /**
     * 外网图片转存缓存
     */
    private LoadingCache<String, String> imgReplaceCache = CacheBuilder.newBuilder().maximumSize(300).expireAfterWrite(5, TimeUnit.MINUTES).build(new CacheLoader<String, String>() {
        @Override
        public String load(String img) {
            try {
                InputStream stream = FileReadUtil.getStreamByFileName(img);
                URI uri = URI.create(img);
                String path = uri.getPath();
                int index = path.lastIndexOf(".");
                String fileType = null;
                if (index > 0) {
                    // 从url中获取文件类型
                    fileType = path.substring(index + 1);
                }
                return imageUploader.upload(stream, fileType);
            } catch (Exception e) {
                log.error("外网图片转存异常! img:{}", img, e);
                return "";
            }
        }
    });

    @Override
    public String saveImg(HttpServletRequest request) {
        MultipartFile file = null;
        if (request instanceof MultipartHttpServletRequest) {
            file = ((MultipartHttpServletRequest) request).getFile("image");
        }
        if (file == null) {
            throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "缺少需要上传的图片");
        }

        // 目前只支持 jpg, png, webp 等静态图片格式
        String fileType = validateStaticImg(file.getContentType());
        if (fileType == null) {
            throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "图片只支持png,jpg,gif");
        }

        try {
            return imageUploader.upload(file.getInputStream(), fileType);
        } catch (IOException e) {
            log.error("Parse img from httpRequest to BufferedImage error! e:", e);
            throw ExceptionUtil.of(StatusEnum.UPLOAD_PIC_FAILED);
        }
    }

    /**
     * 外网图片转存
     *
     * @param img
     * @return
     */
    @Override
    public String saveImg(String img) {
        if (imageUploader.uploadIgnore(img)) {
            // 已经转存过，不需要再次转存；非http图片，不处理
            return img;
        }

        try {
            String ans = imgReplaceCache.get(img);
            if (StringUtils.isBlank(ans)) {
//                return buildUploadFailImgUrl(img);
                URL url = new URL(img);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);
                return imageUploader.upload(connection.getInputStream(), connection.getContentType());
            }
            return ans;
        } catch (Exception e) {
            log.error("外网图片转存异常! img:{}", img, e);
            return buildUploadFailImgUrl(img);
        }
    }


    /**
     * 外网图片自动转存，添加了执行日志，超时限制；避免出现因为超时导致发布文章异常
     *
     * @param content
     * @return
     */
    @Override
    @MdcDot
    @AsyncExecute(timeOutRsp = "#content")
    public String mdImgReplace(String content) {
        List<MdImgLoader.MdImg> imgList = MdImgLoader.loadImgs(content);
        if (CollectionUtils.isEmpty(imgList)) {
            return content;
        }

        if (imgList.size() == 1) {
            // 只有一张图片时，没有必要走异步，直接转存并返回
            MdImgLoader.MdImg img = imgList.get(0);
            String newImg = saveImg(img.getUrl());
            return StringUtils.replace(content, img.getOrigin(), "![" + img.getDesc() + "](" + newImg + ")");
        }

        // 超过1张图片时，做并发的图片转存，提升性能
        Map<MdImgLoader.MdImg, String> imgReplaceMap = Maps.newHashMapWithExpectedSize(imgList.size());
        try(AsyncUtil.CompletableFutureBridge bridge = AsyncUtil.concurrentExecutor("MdImgReplace")) {
            for (MdImgLoader.MdImg img : imgList) {
                bridge.async(() -> {
                    imgReplaceMap.put(img, saveImg(img.getUrl()));
                }, img.getUrl());
            }
            bridge.allExecuted();
        }

        // 图片替换
        for (Map.Entry<MdImgLoader.MdImg, String> entry : imgReplaceMap.entrySet()) {
            MdImgLoader.MdImg img = entry.getKey();
            String newImg = entry.getValue();
            content = StringUtils.replace(content, img.getOrigin(), "![" + img.getDesc() + "](" + newImg + ")");
        }
        return content;
    }

    private String buildUploadFailImgUrl(String img) {
        return img.contains("saveError") ? img : img + "?&cause=saveError!";
    }

    /**
     * 图片格式校验
     *
     * @param mime
     * @return
     */
    private String validateStaticImg(String mime) {
        if ("svg".equalsIgnoreCase(mime)) {
            // fixme 上传文件保存到服务器本地时，做好安全保护, 避免上传了要给攻击性的脚本
            return "svg";
        }

        if (mime.contains(MediaType.ImageJpg.getExt())) {
            mime = mime.replace("jpg", "jpeg");
        }
        for (MediaType type : ImageUploader.STATIC_IMG_TYPE) {
            if (type.getMime().equals(mime)) {
                return type.getExt();
            }
        }
        return null;
    }
}
