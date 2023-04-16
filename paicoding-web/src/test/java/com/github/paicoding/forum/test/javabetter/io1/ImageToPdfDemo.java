package com.github.paicoding.forum.test.javabetter.io1;

import com.itextpdf.text.Document;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.FileOutputStream;
import java.io.IOException;

/**
 * 微信搜索「沉默王二」，回复 Java
 *
 * @author 沉默王二
 * @date 3/27/23
 */
public class ImageToPdfDemo {
    public static void main(String[] args) throws IOException {
        String imagePath = "docs/imgs/itwanger/tongzhishu1.jpeg"; // 图片文件的路径
        String pdfPath = "docs/imgs/itwanger/tongzhishu1.pdf"; // 生成的PDF文件的路径

        // 创建一个Document对象
        Document document = new Document();

        try {
            // 创建一个PdfWriter对象，用于将数据写入PDF文件
            PdfWriter.getInstance(document, new FileOutputStream(pdfPath));
            // 打开Document对象
            document.open();
            // 加载图片
            Image image = Image.getInstance(imagePath);
            // 设置图片的位置和大小
            image.setAbsolutePosition(0, 0);
            image.scaleToFit(document.getPageSize().getWidth(), document.getPageSize().getHeight());
            // 将图片添加到Document对象中
            document.add(image);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 关闭Document对象
            document.close();
        }
    }
}
