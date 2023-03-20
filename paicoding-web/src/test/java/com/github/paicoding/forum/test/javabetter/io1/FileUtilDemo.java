package com.github.paicoding.forum.test.javabetter.io1;

import cn.hutool.core.io.FileUtil;

import java.io.File;

public class FileUtilDemo {
    public static void main(String[] args) {
        // 请写出 Hutool 工具包中 FileUtil 类的使用方法
        File file = FileUtil.file("FileUtilDemo.java");
        // 读取文件
        FileUtil.readLines(file, "UTF-8").forEach(System.out::println);
        // 把 file 复制到另外一个地方
        File dest = FileUtil.file("FileUtilDemo2.java");
        FileUtil.copyFile(file, dest);
        // 把 file 移动到另外一个地方
        FileUtil.move(file, dest, true);
        // 删除 file
        FileUtil.del(file);
        // 重命名
        FileUtil.rename(file, "FileUtilDemo3.java", true);



    }
}
