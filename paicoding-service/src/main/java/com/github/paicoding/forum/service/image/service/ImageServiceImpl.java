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
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.net.URI;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * @author LouZai
 * @date 2022/9/7
 */
@Slf4j
@Service
public class ImageServiceImpl implements ImageService {

    @Autowired
    private ImageUploader imageUploader;

    /**
     * 外网图片转存缓存
     */
    private Cache<String, String> imgReplaceCache = CacheBuilder
            .newBuilder()
            .maximumSize(300)
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .build();

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
            // 先获取图像摘要，根据摘要确定缓存中是否已经包含图像。
            String digest = calculateSHA256(file.getInputStream());
            String ans = imgReplaceCache.getIfPresent(digest);
            if (StringUtils.isBlank(ans)) {
                ans = imageUploader.upload(file.getInputStream(), fileType);
                imgReplaceCache.put(digest, ans);
            }
            return ans;
        } catch (IOException | NoSuchAlgorithmException e) {
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
            InputStream stream = FileReadUtil.getStreamByFileName(img);
            URI uri = URI.create(img);
            String path = uri.getPath();

            int index = path.lastIndexOf(".");
            String fileType = null;
            if (index > 0) {
                // 从url中获取文件类型
                fileType = path.substring(index + 1);
            }
            String digest = calculateSHA256(stream);
            String ans = imgReplaceCache.getIfPresent(digest);
            if (StringUtils.isBlank(ans)) {
                ans = imageUploader.upload(stream, fileType);
                imgReplaceCache.put(digest, ans);
            }
            if (StringUtils.isBlank(ans)) {
                return buildUploadFailImgUrl(img);
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
        Map<MdImgLoader.MdImg, String> imgReplaceMap =  new ConcurrentHashMap<>();
        try(AsyncUtil.CompletableFutureBridge bridge = AsyncUtil.concurrentExecutor("MdImgReplace")) {
            for (MdImgLoader.MdImg img : imgList) {
                bridge.async(() -> {
                    imgReplaceMap.put(img, saveImg(img.getUrl()));
                }, img.getUrl());
            }
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

    /**
     *  图片摘要生成
     *
     * @param inputStream
     * @return
     */
    private String calculateSHA256(InputStream inputStream) throws NoSuchAlgorithmException, IOException {

        inputStream = toByteArrayInputStream(inputStream);
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] buffer = new byte[1024];
        int bytesRead;

        // 读取 InputStream 并更新到 MessageDigest
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            md.update(buffer, 0, bytesRead);
        }

        // 获取摘要并将其转换为十六进制字符串
        byte[] digest = md.digest();
        StringBuilder hexString = new StringBuilder();
        for (byte b : digest) {
            hexString.append(String.format("%02x", b));
        }
        inputStream.reset();
        return hexString.toString();
    }

    /**
     * 转换为字节数组输入流，可以重复消费流中数据
     *
     * @param inputStream
     * @return
     * @throws IOException
     */
    public ByteArrayInputStream toByteArrayInputStream(InputStream inputStream) throws IOException {
        if (inputStream instanceof ByteArrayInputStream) {
            return (ByteArrayInputStream) inputStream;
        }

        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            BufferedInputStream br = new BufferedInputStream(inputStream);
            byte[] b = new byte[1024];
            for (int c; (c = br.read(b)) != -1; ) {
                bos.write(b, 0, c);
            }
            // 主动告知回收
            b = null;
            br.close();
            inputStream.close();
            return new ByteArrayInputStream(bos.toByteArray());
        }
    }


}
