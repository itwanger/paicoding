package com.github.paicoding.forum.test.javabetter.top.image;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.file.FileNameUtil;
import cn.hutool.core.io.file.FileReader;
import nu.pattern.OpenCV;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.opencv.imgcodecs.Imgcodecs;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Convert2jpg {

    public static void main(String[] args) throws IOException {
        OpenCV.loadShared();
        // 转成 jpg 图片并压缩
        File file = new File("/Users/itwanger/Documents/Github/Untitled-1.bat");
        FileReader fileReader = FileReader.create(file, StandardCharsets.UTF_8);
        List<String> lines = fileReader.readLines();
        String ext = ".jpg";

        Map<String,String> needReplaceImageNames = new HashMap<>();
        for (String line: lines) {
            Mat sourceImage = Imgcodecs.imread(line);
            String extName = FileUtil.extName(line);
            if ("".equals(extName)) {
                String name = FileUtil.getName(line);
                needReplaceImageNames.put(name,name+ext);
                MatOfInt  dstImageParam = new MatOfInt(
                        Imgcodecs.IMWRITE_PNG_COMPRESSION, 9);

                if (!sourceImage.empty()) {
//                    Imgcodecs.imwrite(line+ext, sourceImage, dstImageParam);
//                    FileUtil.del(line);
                }
            } else if ("jpeg".equals(extName)) {
                MatOfInt dstImageParam = new MatOfInt(Imgcodecs.IMWRITE_JPEG_QUALITY, 50);
//                Imgcodecs.imwrite(line, sourceImage, dstImageParam);
            }
        }

        // 替换 md 中的路径，末尾) -> .jpg)
        List<File> files = FileUtil.loopFiles("/Users/itwanger/Documents/GitHub/toBeBetterJavaer/docs/");
        for (File md: files) {
            if (FileNameUtil.isType(md.getName(), "md")) {
                String mdName = FileNameUtil.mainName(md);
                if (contains(needReplaceImageNames,mdName)) {
                    FileReader mdReader = FileReader.create(md, Charset.forName("utf-8"));
                    String content = mdReader.readString();
                    System.out.println(content);
                    for(String key : needReplaceImageNames.keySet()) {
                        content = content.replace(key, needReplaceImageNames.get(key));
                    }
                    FileWriter writer = new FileWriter(md);
                    writer.write(content);
                    writer.flush();
                }
            }
        }
    }
    // 判断文件名是否在 key的 map 中，否则跳过
    public static boolean contains(Map<String,String> needReplaceImageNames, String mdName) {
        for(String key : needReplaceImageNames.keySet()) {
            if (key.indexOf(mdName) != -1) {
                return true;
            }
        }
        return false;
    }
}
