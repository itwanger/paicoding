package com.github.liuyueyi.forum.web.front;

import com.github.liuyueyi.forum.core.util.MapUtils;
import com.github.liuyueyi.forum.service.article.CategoryService;
import com.github.liuyueyi.forum.service.article.dto.CategoryDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author YiHui
 * @date 2022/7/6
 */
@Controller
public class IndexController {
    @Autowired
    private CategoryService categoryService;

    @GetMapping(path = {"/", "", "/index"})
    public String index(Model model, HttpServletRequest request) {
        String activeTab = request.getParameter("category");
        model.addAttribute("categories", categories(activeTab));
        model.addAttribute("homeCarouselList", homeCarouselList());
        model.addAttribute("sideBarItems", sideBarItems());
        model.addAttribute("currentDomain", "article");
        return "index";
    }

    /**
     * 返回分类列表
     *
     * @param active
     * @return
     */
    private List<CategoryDTO> categories(String active) {
        List<CategoryDTO> list = categoryService.loadAllCategories(false);
        list.add(0, CategoryDTO.DEFAULT_CATEGORY);
        list.forEach(s -> s.setSelected(s.getCategory().equals(active)));
        return list;
    }

    /**
     * 轮播图
     *
     * @return
     */
    private List<Map<String, Object>> homeCarouselList() {
        List<Map<String, Object>> list = new ArrayList<>();
        list.add(MapUtils.create("imgUrl", "https://spring.hhui.top/spring-blog/imgs/220425/logo.jpg", "name", "spring社区", "actionUrl", "https://spring.hhui.top/"));
        list.add(MapUtils.create("imgUrl", "https://spring.hhui.top/spring-blog/imgs/220422/logo.jpg", "name", "一灰灰", "actionUrl", "https://blog.hhui.top/"));
        return list;
    }


    /**
     * 侧边栏信息
     *
     * @return
     */
    private List<Map<String, Object>> sideBarItems() {
        List<Map<String, Object>> res = new ArrayList<>();
        res.add(MapUtils.create("title", "公告", "desc", "简单的公告内容"));
        res.add(MapUtils.create("title", "标签云", "desc", "java, web, html"));
        return res;
    }
}
