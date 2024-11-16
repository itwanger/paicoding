package com.github.paicoding.forum.web.javabetter.network;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DownloadFileThread {
    public static void main(String[] args) throws IOException {
        URL url = new URL("https://cdn.tobebetterjavaer.com/tobebetterjavaer/images/nice-article/weixin-mianznxjsjwllsewswztwxxssc-fee87ab7-0475-429b-aba6-7a8df6841572.jpg");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("HEAD");
        int fileSize = connection.getContentLength(); // 获取文件大小
        connection.disconnect();

        System.out.println("文件大小：" + fileSize);

        int numThreads = 4;
        int chunkSize = fileSize / numThreads;
        String outputPath = "/Users/itwanger/Documents/file.png·";

        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        for (int i = 0; i < numThreads; i++) {
            int start = i * chunkSize;
            int end = (i == numThreads - 1) ? fileSize - 1 : (start + chunkSize - 1);
            executor.execute(() -> downloadChunk(String.valueOf(url), start, end, outputPath));
        }
        executor.shutdown();
    }

    public static void downloadChunk(String url, int start, int end, String outputPath) {
        try {
            URL fileUrl = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) fileUrl.openConnection();
            connection.setRequestProperty("Range", "bytes=" + start + "-" + end);

            InputStream inputStream = connection.getInputStream();
            RandomAccessFile file = new RandomAccessFile(outputPath, "rw");
            file.seek(start); // 定位到文件的相应位置

            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                file.write(buffer, 0, bytesRead);
            }

            file.close();
            inputStream.close();
            connection.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
