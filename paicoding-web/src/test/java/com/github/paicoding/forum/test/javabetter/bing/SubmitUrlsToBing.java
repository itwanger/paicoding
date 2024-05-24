package com.github.paicoding.forum.test.javabetter.bing;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * 微信搜索「沉默王二」，回复 Java
 *
 * @author 沉默王二
 * @date 4/25/24
 */
public class SubmitUrlsToBing {
    public static void main(String[] args) {
        try {
            // 指定 sitemap.xml 的 URL
            String sitemapUrl = "https://javabetter.cn/sitemap.xml";
            URL url = new URL(sitemapUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            // 检查响应代码
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                Document doc = dBuilder.parse(connection.getInputStream());

                // 获取所有 <loc> 元素，它们包含了 URL
                NodeList nList = doc.getElementsByTagName("loc");
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < nList.getLength(); i++) {
                    String loc = nList.item(i).getTextContent();
                    sb.append(loc).append("\n");
                }

                // 提交 URL 到百度
                String postUrl = "http://data.zz.baidu.com/urls?site=https://www.javabetter.cn&token=dmbQj2wFYFLPNz7I";
                URL obj = new URL(postUrl);
                HttpURLConnection postConnection = (HttpURLConnection) obj.openConnection();
                postConnection.setRequestMethod("POST");
                postConnection.setRequestProperty("Content-Type", "text/plain");

                // 发送 POST 请求
                postConnection.setDoOutput(true);
                DataOutputStream wr = new DataOutputStream(postConnection.getOutputStream());
                wr.writeBytes(sb.toString());
                wr.flush();
                wr.close();

                int responseCodePost = postConnection.getResponseCode();
                BufferedReader in = new BufferedReader(new InputStreamReader(postConnection.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                // 打印结果
                System.out.println("POST Response Code :: " + responseCodePost);
                System.out.println("Response content :: " + response.toString());
            } else {
                System.out.println("GET request not worked");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
