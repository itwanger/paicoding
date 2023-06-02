package com.github.paicoding.forum.test.javabetter.top.copydown;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.http.HttpUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.DataNode;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class GetWeixinFengmiantu {
    public static final String fileSeparator = System.getProperty("file.separator");
    public static final String destination = System.getProperty("user.home")
            +fileSeparator+"Documents" +fileSeparator+
            "weixin" +fileSeparator;
    public static final String url = "https://mp.weixin.qq.com/s/9f706T20JfNII78YK5n4pA";
    public static final String imageKey = "msg_cdn_url";
    public static void main(String[] args) {
        Document doc = null;
        try {
            doc = Jsoup.connect(url).get();
        } catch (IOException e) {
            log.error("jsoup error{}", e);
        }

        for (Element scripts : doc.getElementsByTag("script")) {
            for (DataNode dataNode : scripts.dataNodes()) {
                // find data which contains
                if (dataNode.getWholeData().contains(imageKey)) {
                    log.info("contains");

                    // 封面图
                    Pattern pattern = Pattern.compile("var\\s+" + imageKey + "\\s+=\\s+\"(.*)\";");
                    Matcher matcher = pattern.matcher(dataNode.getWholeData());
                    if (matcher.find()) {
                        String msg_cdn_url = matcher.group(1);
                        log.info("find msg_cdn_url success {}", msg_cdn_url);

                        if (StringUtils.isNotBlank(msg_cdn_url)) {
                            long size = HttpUtil.downloadFile(msg_cdn_url,
                                    FileUtil.file(destination + DateUtil.now() + ".jpg"));
                            log.info("cover image size{}", size);
                        }
                    }
                }
            }
        }
    }
}
