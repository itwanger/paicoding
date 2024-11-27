package com.github.paicoding.forum.web.javabetter.baidu;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

public class ExtractUrls {
    public static void main(String[] args) {
        String sitemapUrl = "https://javabetter.cn/sitemap.xml";
        String outputFilePath = "urls.txt";

        try {
            // 发送 HTTP 请求获取 sitemap
            URL url = new URL(sitemapUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            // 解析 XML
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(conn.getInputStream());

            // 提取所有 <loc> 元素的内容
            NodeList locNodes = doc.getElementsByTagName("loc");
            BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath));
            for (int i = 0; i < locNodes.getLength(); i++) {
                String loc = locNodes.item(i).getTextContent();
                writer.write(loc);
                writer.newLine();
            }
            writer.close();

            System.out.println("提取了 " + locNodes.getLength() + " 个 URL 并写入 urls.txt 文件");

            // 使用 curl 提交 URL
            ProcessBuilder pb = new ProcessBuilder(
                    "curl", "-H", "Content-Type:text/plain", "--data-binary", "@" + outputFilePath,
                    "http://data.zz.baidu.com/urls?site=https://javabetter.cn&token=dmbQj2wFYFLPNz7I"
            );
            pb.redirectErrorStream(true);
            Process process = pb.start();
            BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                System.out.println(line);
            }
            in.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
