package com.github.paicoding.forum.test.javabetter.top.image;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;

public class Yasuo {
    public static void main(String[] args) {

        try {
            File input = new File("ceshi.jpg");
            BufferedImage image = ImageIO.read(input);


            Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpg");
            ImageWriter writer = (ImageWriter) writers.next();

            File compressedImageFile = new File("bbcompress.jpg");
            OutputStream os = new FileOutputStream(compressedImageFile);
            ImageOutputStream ios = ImageIO.createImageOutputStream(os);
            writer.setOutput(ios);


            ImageWriteParam param = writer.getDefaultWriteParam();

            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionQuality(0.01f);

            writer.write(null, new IIOImage(image, null, null), param);

            os.close();
            ios.close();
            writer.dispose();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
