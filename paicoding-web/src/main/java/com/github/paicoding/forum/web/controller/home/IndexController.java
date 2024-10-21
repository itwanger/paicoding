package com.github.paicoding.forum.web.controller.home;

import com.github.paicoding.forum.web.controller.home.helper.IndexRecommendHelper;
import com.github.paicoding.forum.web.controller.home.vo.HomeVo;
import com.github.paicoding.forum.web.global.BaseViewController;
import com.github.paicoding.forum.web.global.vo.ResultVo;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
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


    @GetMapping(path = {"/new/", "/new/index", "/new/login"})
    @ResponseBody
    public ResultVo<HomeVo> fakeIndex(HttpServletRequest request) {

        String activeTab = request.getParameter("category");
        // 这里没有给出文章列表，返回了除文章以外的所有数据
        HomeVo vo = indexRecommendHelper.buildHomeVo(activeTab);

        return ResultVo.ok(vo);
    }

}
