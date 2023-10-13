package com.github.paicoding.forum.test.javabetter.top.image;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import nu.pattern.OpenCV;

public class OpenCVYasuoSingleFile {
    private final static String docPath = System.getProperty("user.home")
            + "/Documents/Github/toBeBetterJavaer/images/";


    public static void main(String[] args) {
        OpenCV.loadShared();
        String filename = docPath+"logo.png";
        System.out.println(DateUtil.formatDate(FileUtil.lastModifiedTime(filename)));
        if ( FileUtil.lastModifiedTime(filename).after(DateUtil.parse("2022年05月17日"))) {
            System.out.println("要修改");
        }
//        Mat sourceImage = Imgcodecs.imread(filename);
//
//        MatOfInt dstImageParam = new MatOfInt(Imgcodecs.IMWRITE_JPEG_QUALITY, 50);
//        dstImageParam = new MatOfInt(Imgcodecs.IMWRITE_PNG_COMPRESSION, 9);
//        Imgcodecs.imwrite(filename, sourceImage, dstImageParam);

    }
}
