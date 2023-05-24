package com.github.paicoding.forum.test.javabetter.top.image;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.file.FileNameUtil;
import nu.pattern.OpenCV;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.opencv.imgcodecs.Imgcodecs;

import java.io.File;
import java.util.List;

public class OpenCVYasuoDicNoTypeOrJpeggifFile {
    private final static String [] docPaths = {
            "/Users/maweiqing/Documents/GitHub/toBeBetterJavaer/docs/",
            "/Users/itwanger/Documents/Github/toBeBetterJavaer/images/itwanger/",
            "/Users/itwanger/Documents/GitHub/toBeBetterJavaer/images/",
    };

    public static void main(String[] args) {
        OpenCV.loadShared();
        String docPath = docPaths[2];

        List<File> files = FileUtil.loopFiles(docPath);
        for (File file: files) {
            if (FileNameUtil.isType(file.getName(), "DS_Store","ico","jpg","png")) {
                continue;
            }
            if (FileUtil.size(file)<50*1000) {
                continue;
            }
            Mat sourceImage = Imgcodecs.imread(file.getAbsolutePath());
            String extName = FileUtil.extName(file);
            String ext = "";
            MatOfInt dstImageParam = null;
            if ("".equals(extName)) {
                System.out.println(file.getAbsolutePath());
                dstImageParam = new MatOfInt(
                        Imgcodecs.IMWRITE_PNG_COMPRESSION, 9);
                ext = ".jpg";
//                FileUtil.del(file);
            } else if ("jpeg".equals(extName)) {
                dstImageParam = new MatOfInt(Imgcodecs.IMWRITE_JPEG_QUALITY, 50);
            } else if ("gif".equals(extName)) {
                System.out.println(file.getAbsolutePath());
            }
            if (dstImageParam != null) {
//                Imgcodecs.imwrite(file.getAbsolutePath()+ext, sourceImage, dstImageParam);
            }
        }
    }
}
