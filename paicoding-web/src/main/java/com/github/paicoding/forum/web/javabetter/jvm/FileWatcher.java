package com.github.paicoding.forum.web.javabetter.jvm;

import java.io.IOException;
import java.nio.file.*;

import static java.nio.file.StandardWatchEventKinds.*;

class FileWatcher {

    public static void watchDirectoryPath(Path path) {
        // 检查路径是否是有效目录
        if (!isDirectory(path)) {
            System.err.println("Provided path is not a directory: " + path);
            return;
        }

        System.out.println("Starting to watch path: " + path);

        // 获取文件系统的 WatchService
        try (WatchService watchService = path.getFileSystem().newWatchService()) {
            // 注册目录监听服务，监听创建、修改和删除事件
            path.register(watchService, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE);

            while (true) {
                WatchKey key;
                try {
                    // 阻塞直到有事件发生
                    key = watchService.take();
                } catch (InterruptedException e) {
                    System.out.println("WatchService interrupted, stopping directory watch.");
                    Thread.currentThread().interrupt();
                    break;
                }

                // 处理事件
                for (WatchEvent<?> event : key.pollEvents()) {
                    processEvent(event);
                }

                // 重置 key，如果失败则退出
                if (!key.reset()) {
                    System.out.println("WatchKey no longer valid. Exiting watch loop.");
                    break;
                }
            }
        } catch (IOException e) {
            System.err.println("An error occurred while setting up the WatchService: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static boolean isDirectory(Path path) {
        return Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS);
    }

    private static void processEvent(WatchEvent<?> event) {
        WatchEvent.Kind<?> kind = event.kind();

        // 处理事件类型
        if (kind == OVERFLOW) {
            System.out.println("Event overflow occurred. Some events might have been lost.");
            return;
        }

        @SuppressWarnings("unchecked")
        Path fileName = ((WatchEvent<Path>) event).context();
        System.out.println("Event: " + kind.name() + ", File affected: " + fileName);
    }

    public static void main(String[] args) {
        // 设置监控路径为当前目录
        Path pathToWatch = Paths.get(".");
        watchDirectoryPath(pathToWatch);
    }
}
