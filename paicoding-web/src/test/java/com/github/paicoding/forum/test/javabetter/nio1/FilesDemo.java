package com.github.paicoding.forum.test.javabetter.nio1;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Set;

public class FilesDemo {
    public static void main(String[] args) throws IOException {
        // 创建一个Path实例
        Path path = Paths.get("logs/javabetter/itwanger4.txt");

        // 创建一个新文件
        Files.createFile(path);

        // 检查文件是否存在
        boolean exists = Files.exists(path);
        System.out.println("File exists: " + exists);

        // 删除文件
        Files.delete(path);

        path = Paths.get("fileWithPermissions.txt");

        Set<PosixFilePermission> permissions = PosixFilePermissions.fromString("rw-r-----");
        FileAttribute<Set<PosixFilePermission>> fileAttribute = PosixFilePermissions.asFileAttribute(permissions);

        Files.createFile(path, fileAttribute);
    }
}
