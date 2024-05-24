package com.github.paicoding.forum.test.javabetter.bing;

/**
 * 微信搜索「沉默王二」，回复 Java
 *
 * @author 沉默王二
 * @date 4/25/24
 */
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class IndexNowSubmitter {

    public static void main(String[] args) {
        try {
            // 从 sitemap.xml 获取 URL 列表
            List<String> urls = fetchUrlsFromSitemap("https://javabetter.cn/sitemap.xml");

            // 创建 HttpClient 实例
            CloseableHttpClient httpClient = HttpClients.createDefault();

            // 创建 HTTP POST 请求
            HttpPost post = new HttpPost("https://api.indexnow.org/IndexNow");
            post.setHeader("Content-Type", "application/json; charset=utf-8");

            // 构建 JSON 请求体
            StringBuilder jsonBuilder = new StringBuilder("{")
                    .append("\"host\": \"www.javabetter.cn\",")
                    .append("\"key\": \"caad1b23a8414505b4e825cc12cedf9e\",")
                    .append("\"keyLocation\": \"https://www.javabetter.cn/caad1b23a8414505b4e825cc12cedf9e.txt\",")
                    .append("\"urlList\": [");
            for (int i = 0; i < urls.size(); i++) {
                jsonBuilder.append("\"").append(urls.get(i)).append("\"");
                if (i < urls.size() - 1) {
                    jsonBuilder.append(", ");
                }
            }
            jsonBuilder.append("]}");

            // 设置请求体
            StringEntity entity = new StringEntity(jsonBuilder.toString());
            post.setEntity(entity);

            // 执行请求并获取响应
            HttpResponse response = httpClient.execute(post);

            // 处理响应
            String responseString = EntityUtils.toString(response.getEntity(), "UTF-8");
            System.out.println("Response: " + responseString);

            // 关闭 HttpClient
            httpClient.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static List<String> fetchUrlsFromSitemap(String sitemapUrl) throws Exception {
        List<String> urls = new ArrayList<>();
        URL url = new URL(sitemapUrl);
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(url.openStream());

        NodeList nodeList = doc.getElementsByTagName("loc");
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) node;
                urls.add(element.getTextContent());
            }
        }
        return urls;
    }
}
