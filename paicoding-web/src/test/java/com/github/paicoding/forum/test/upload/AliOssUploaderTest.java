package com.github.paicoding.forum.test.upload;

import com.github.hui.quick.plugin.base.awt.GraphicUtil;
import com.github.hui.quick.plugin.base.file.FileReadUtil;
import com.github.paicoding.forum.core.common.CommonConstants;
import com.github.paicoding.forum.core.config.ImageProperties;
import com.github.paicoding.forum.core.config.OssProperties;
import com.github.paicoding.forum.service.image.oss.impl.AliOssWrapper;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author XuYifei
 * @date 2024-07-12
 */
public class AliOssUploaderTest {
    public static void main(String[] args) throws Exception {
        AliOssWrapper aliOss = initOss();

        Map<String, List<String>> map = CommonConstants.HOMEPAGE_TOP_PIC_MAP;
        Map<String, String> res = new HashMap<>();
        for (List<String> sub : map.values()) {
            for (String str : sub) {
                if (str.startsWith(aliOss.getProperties().getOss().getHost())) {
                    continue;
                }

                res.put(str, uploadWebp2jpg(str, aliOss));
            }
        }
        System.out.println("--------------\n\n");

        for (Map.Entry<String, String> entry : res.entrySet()) {
            System.out.println(entry.getKey() + "  ===  " + entry.getValue());
        }
        System.out.println("\n\n--------------");
    }

    /**
     * 初始化阿里云配置
     *
     * @return
     * @throws Exception
     */
    private static AliOssWrapper initOss() throws Exception {
        // Endpoint以华东1（杭州）为例，其它Region请按实际情况填写。
        String endpoint = "x";
        // 阿里云账号AccessKey拥有所有API的访问权限，风险很高。强烈建议您创建并使用RAM用户进行API访问或日常运维，请登录RAM控制台创建RAM用户。
        String accessKeyId = "x";
        String accessKeySecret = "x";
        String bucketName = "x";
        String host = "https://cdn.paicoding.com/";

        AliOssWrapper aliOss = new AliOssWrapper();
        ImageProperties properties = new ImageProperties();
        OssProperties oss = new OssProperties();
        oss.setAk(accessKeyId);
        oss.setSk(accessKeySecret);
        oss.setBucket(bucketName);
        oss.setEndpoint(endpoint);
        oss.setBucket(bucketName);
        oss.setHost(host);
        oss.setPrefix("paicoding/");
        properties.setOss(oss);

        aliOss.setProperties(properties);
        aliOss.afterPropertiesSet();
        return aliOss;
    }

    /**
     * 将webp 格式图片转码为 jpg 之后上传到 阿里云oss
     *
     * @param str
     * @param aliOss
     * @return
     * @throws IOException
     */
    private static String uploadWebp2jpg(String str, AliOssWrapper aliOss) throws IOException {
        InputStream stream = FileReadUtil.getStreamByFileName(str);
        BufferedImage img = ImageIO.read(stream);
        img = GraphicUtil.createImg(img.getWidth(), img.getHeight(), 0, 0, BufferedImage.TYPE_INT_RGB, img, null);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(img, "jpg", outputStream);

        String ans = aliOss.upload(outputStream.toByteArray(), "jpg");
        System.out.println(str + " === " + ans);
        return ans;
    }
}
