package com.github.paicoding.forum.test.javabetter.top.image;

import cn.hutool.core.io.file.FileReader;
import cn.hutool.core.util.IdUtil;
import lombok.SneakyThrows;

import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 把简书的图片链接转到 GitHub CDN
 *
 * @author 微信搜「沉默王二」，回复关键字 Java
 */
public class Convert {

    final static String directory = "oo/";
    final static String key = "inner-class";

    final static String pcName = "itwanger";

    final static String docPath = "/Users/"+pcName+"/Documents/GitHub/toBeBetterJavaer/docs/" + directory;
    final static String imgPath = "/Users/"+pcName+"/Documents/GitHub/toBeBetterJavaer/images/" + directory;

    final static String fileName = key + ".md";
    private static final String[] imageExtension = {".jpg", ".jpeg", ".png", ".gif"};
    private static final String imgCdnPre = "https://cdn.jsdelivr.net/gh/itwanger/toBeBetterJavaer/images/";
    private static final String[] imgCdnPres = {imgCdnPre,
            "cdn.tobebetterjavaer.com/tobebetterjavaer/images/",
            "https://itwanger-oss.oss-cn-beijing.aliyuncs.com/"};
    final static String imgCdn = "https://cdn.jsdelivr.net/gh/itwanger/toBeBetterJavaer/images/" + directory + key + "-";
    // 匹配图片的 markdown 语法
    // ![](hhhx.png)
    // ![xx](hhhx.png?ax)
    public static final String IMG_PATTERN = "\\!\\[(.*)\\]\\((.*)\\)";

    static class MyRunnable implements Runnable {
        private String originImgUrl;
        private String destinationImgPath;

        public MyRunnable(String originImgUrl, String destinationImgPath) {
            this.originImgUrl = originImgUrl;
            this.destinationImgPath = destinationImgPath;
        }

        @SneakyThrows
        @Override
        public void run() {
            URL url = new URL(originImgUrl);
            InputStream inputStream = url.openStream();
            OutputStream outputStream = new FileOutputStream(destinationImgPath);
            byte[] buffer = new byte[2048];
            int length = 0;
            while ((length = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, length);
            }
        }
    }

    public static void main(String[] args) throws IOException {
        FileReader fileReader = FileReader.create(new File(docPath + fileName),
                Charset.forName("utf-8"));

        List<String> list = fileReader.readLines();
        FileWriter writer = new FileWriter(docPath + fileName);
        Pattern pattern = Pattern.compile(IMG_PATTERN, Pattern.CASE_INSENSITIVE);

        for (String line : list) {
            Matcher matcher = pattern.matcher(line);
            if (matcher.find()) {
                // 处理图片
                boolean needConvert = true;
                for (String extItem : imgCdnPres) {
                    if (line.indexOf(extItem) != -1) {
                        needConvert = false;
                        break;
                    }
                }
                // 已经转换过的，就不需要再转了
                if (!needConvert) {
                    writer.append(line + "\n");
                    continue;
                }


                // png 还是 gif 还是 jpg
                String ext = "";
                for (String extItem : imageExtension) {
                    if (line.indexOf(extItem) != -1) {
                        ext = extItem;
                        break;
                    }
                }

                String imageName = matcher.group(1);
                String imagePath = matcher.group(2);
                System.out.println("使用分组进行替换名" + imageName + "路径"+ imagePath);

                String num = IdUtil.fastUUID();
                String destinationImgPath = imgPath + key + "-" + num + ext;

                // 1、下载到本地
                new Thread(new MyRunnable(imagePath, destinationImgPath)).start();

                // 2、修改 MD 文档
                writer.append("![" +imageName+ "](" + imgCdn + num + ext + ")\n");


            } else {
                writer.append(line + "\n");
            }
        }
        writer.flush();
    }
}
