package com.github.paicoding.forum.web.controller.category.rest;

import com.github.paicoding.forum.api.model.vo.article.dto.CategoryDTO;
import com.github.paicoding.forum.service.article.service.CategoryService;
import com.github.paicoding.forum.web.global.vo.ResultVo;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @program: tech-pai
 * @description:
 * @author: XuYifei
 * @create: 2024-07-05
 */

@RestController
@RequestMapping("/api/category/")
public class CategoryRestController {


    @Resource
    private CategoryService categoryService;

    /**
     * 获取所有的分类标签
     * @return
     */
    @GetMapping(path = "list/all")
    public ResultVo<List<CategoryDTO>> listAll() {
        List<CategoryDTO> categoryDTOS = categoryService.loadAllCategories();
        return ResultVo.ok(categoryDTOS);
    }
}
