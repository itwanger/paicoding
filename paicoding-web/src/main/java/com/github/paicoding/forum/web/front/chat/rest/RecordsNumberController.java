package com.github.paicoding.forum.web.front.chat.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.github.paicoding.forum.api.model.vo.ResVo;
import com.github.paicoding.forum.service.chatai.service.RecordsNumberService;

import lombok.extern.slf4j.Slf4j;

/**
 * chatgpt 记录次数
 *
 * @ClassName: RecordsNumberController
 * @Author: ygl
 * @Date: 2023/6/24 11:06
 * @VersRe: 1.0
 */
@RestController
@Slf4j
@RequestMapping("/chat")
public class RecordsNumberController {

    @Autowired
    RecordsNumberService recordsNumberService;

    /**
     * 获取次数
     *
     * @return
     */
    @PostMapping("/getRecordsNumber")
    private ResVo<Integer> getRecordsNumber() {

        Integer result = recordsNumberService.getRecordsNumber();

        return ResVo.ok(result);
    }

    /**
     * 次数减1
     *
     * @return
     */
    @PostMapping("/decrRecordsNumber")
    private ResVo<Integer> decrRecordsNumber() {

        Integer result = recordsNumberService.decrRecordsNumber();

        return ResVo.ok(result);
    }


}
