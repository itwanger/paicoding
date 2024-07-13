package com.github.paicoding.forum.test.javabetter.io1;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * 微信搜索「沉默王二」，回复 Java
 *
 * @author 沉默王二
 * @date 3/27/23
 */
public class TongzhishuDemo {
    public static void main(String[] args) throws IOException {
        // 打开图片并渲染数据
        BufferedImage image = ImageIO.read(new File("docs/imgs/itwanger/tongzhishu.jpeg"));
        Graphics2D g2d = image.createGraphics();
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.PLAIN, 24));
        g2d.drawString("沉默王二", 600, 230);
        g2d.drawString("家里蹲大学", 800, 340);
        g2d.dispose();
        ImageIO.write(image, "jpg", new File("docs/imgs/itwanger/tongzhishu1.jpeg"));
    }
}
