package com.github.paicoding.forum.test.javabetter.baidu;

/**
 * 微信搜索「沉默王二」，回复 Java
 *
 * @author 沉默王二
 * @date 4/14/24
 */
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.FileWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

public class WriteUrlsToFile {
    public static void main(String[] args) {
        try {
            String sitemapUrl = "https://javabetter.cn/sitemap.xml"; // sitemap 地址
            URL url = new URL(sitemapUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                Document doc = dBuilder.parse(connection.getInputStream());

                NodeList nList = doc.getElementsByTagName("loc");
                File file = new File("urls.txt"); // 输出文件
                FileWriter writer = new FileWriter(file);

                for (int i = 0; i < nList.getLength(); i++) {
                    String loc = nList.item(i).getTextContent();
                    if (!loc.startsWith("https://www.")) {
                        loc = "https://www." + loc.substring(loc.indexOf("://") + 3);
                    }
                    writer.write(loc + "\n");
                }
                writer.close(); // 确保关闭文件流
                System.out.println("URLs have been written to " + file.getAbsolutePath());
            } else {
                System.out.println("Failed to fetch sitemap: HTTP error code " + connection.getResponseCode());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

