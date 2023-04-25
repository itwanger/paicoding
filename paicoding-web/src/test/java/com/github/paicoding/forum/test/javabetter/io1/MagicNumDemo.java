package com.github.paicoding.forum.test.javabetter.io1;

import com.github.hui.quick.plugin.base.constants.MediaType;
import com.github.hui.quick.plugin.base.file.FileReadUtil;

import java.io.*;

/**
 * 微信搜索「沉默王二」，回复 Java
 *
 * @author 沉默王二
 * @date 4/23/23
 */
public class MagicNumDemo {
    public static void main(String[] args) throws IOException {
        // 获取文件的输入流 ByteArrayInputStream
        FileInputStream fileInputStream = new FileInputStream("docs/imgs/init_00.jpg");
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        byte[] buffer = new byte[4096];
        int bytesRead;

        while ((bytesRead = fileInputStream.read(buffer)) != -1) {
            byteArrayOutputStream.write(buffer, 0, bytesRead);
        }

        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
        String magicNum = FileReadUtil.getMagicNum(byteArrayInputStream);
        System.out.println("文件魔数 " + magicNum);

        // 根据魔数判断文件类型
        MediaType type = MediaType.typeOfMagicNum(magicNum);
        System.out.println("文件类型 " + type);

    }
}
