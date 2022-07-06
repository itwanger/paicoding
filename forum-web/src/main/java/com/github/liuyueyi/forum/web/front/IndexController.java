package com.github.liuyueyi.forum.web.front;

import com.github.liuyueyi.forum.core.util.MapUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author YiHui
 * @date 2022/7/6
 */
@Controller
public class IndexController {

    @GetMapping(path = {"/", "", "/index"})
    public String index(Model model, HttpServletRequest request) {
        String activeTab = request.getParameter("type");
        model.addAttribute("categories", categories(activeTab));
        model.addAttribute("homeCarouselList", homeCarouselList());
        model.addAttribute("sideBarItems", sideBarItems());
        model.addAttribute("author", author());
        model.addAttribute("currentDomain", "article");
        return "index";
    }

    // 分类，使用db中的进行替换
    private List<String> DEFAULT_CATEGORIES = Arrays.asList("后端", "前端", "数据库");

    private List<Map<String, Object>> categories(String active) {
        List<Map<String, Object>> list = new ArrayList<>();
        boolean hit = DEFAULT_CATEGORIES.contains(active);
        list.add(MapUtils.create("name", "全部", "selected", !hit, "refCount", 0));
        for (int i = 0; i < DEFAULT_CATEGORIES.size(); i++) {
            list.add(MapUtils.create("name", DEFAULT_CATEGORIES.get(i), "selected", hit && DEFAULT_CATEGORIES.get(i).equals(active), "refCount", i + 1));
        }
        return list;
    }

    private List<Map<String, Object>> homeCarouselList() {
        List<Map<String, Object>> list = new ArrayList<>();
        list.add(MapUtils.create("imgUrl", "https://spring.hhui.top/spring-blog/imgs/220425/logo.jpg", "name", "spring社区", "actionUrl", "https://spring.hhui.top/"));
        list.add(MapUtils.create("imgUrl", "https://spring.hhui.top/spring-blog/imgs/220422/logo.jpg", "name", "一灰灰", "actionUrl", "https://blog.hhui.top/"));
        return list;
    }


    private List<Map<String, Object>> sideBarItems() {
        List<Map<String, Object>> res = new ArrayList<>();
        res.add(MapUtils.create("title", "公告", "desc", "简单的公告内容"));
        res.add(MapUtils.create("title", "标签云", "desc", "java, web, html"));
        return res;
    }

    /**
     * 作者信息
     *
     * @return
     */
    private Map<String, Object> author() {
        return MapUtils.create("uid", 1L,
                "avatar", "https://spring.hhui.top/spring-blog/css/images/avatar.jpg",
                "uname", "一灰灰",
                "desc", "码农界资深搬运工",
                "items", Arrays.asList("java", "python", "js"));
    }
}
