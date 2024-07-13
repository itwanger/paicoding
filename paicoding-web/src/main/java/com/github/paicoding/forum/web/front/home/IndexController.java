package com.github.paicoding.forum.web.front.home;

import com.github.paicoding.forum.web.front.home.helper.IndexRecommendHelper;
import com.github.paicoding.forum.web.front.home.vo.HomeVo;
import com.github.paicoding.forum.web.front.home.vo.IndexVo;
import com.github.paicoding.forum.web.global.BaseViewController;
import com.github.paicoding.forum.web.global.vo.ResultVo;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author XuYifei
 * @date 2024-07-12
 */
@Controller
public class IndexController extends BaseViewController {
    @Autowired
    private IndexRecommendHelper indexRecommendHelper;

    @GetMapping(path = {"/", "", "/index", "/login"})
    public String index(Model model, HttpServletRequest request) {
        String activeTab = request.getParameter("category");
        IndexVo vo = indexRecommendHelper.buildIndexVo(activeTab);
        model.addAttribute("vo", vo);
        return "views/home/index";
    }

    @GetMapping(path = {"/new/", "/new/index", "/new/login"})
    @ResponseBody
    public ResultVo<HomeVo> fakeIndex(HttpServletRequest request) {

        String activeTab = request.getParameter("category");
        // 这里没有给出文章列表，返回了除文章以外的所有数据
        HomeVo vo = indexRecommendHelper.buildHomeVo(activeTab);

        return ResultVo.ok(vo);
    }

}
