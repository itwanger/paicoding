package com.github.paicoding.forum.web.front.test.rest

import com.github.paicoding.forum.api.model.context.ReqInfoContext
import com.github.paicoding.forum.api.model.vo.user.dto.BaseUserInfoDTO
import com.github.paicoding.forum.core.util.EmailUtil
import com.github.paicoding.forum.web.hook.interceptor.GlobalViewInterceptor
import com.sayweee.spock.mockfree.annotation.MockStatic
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import spock.lang.Specification

@SuppressWarnings("all")
class TestControllerTest extends Specification {

    def "test email"() {
        given: "prepare beans"
        def baseUserInfo = new BaseUserInfoDTO()
        baseUserInfo.setRole(role)
        def reqInfo = new ReqInfoContext.ReqInfo()
        reqInfo.setUserId(111L)
        reqInfo.setUser(baseUserInfo)
        if (role != null) {
            ReqInfoContext.addReqInfo(reqInfo)
        } else {
            ReqInfoContext.clear()
        }
        MockMvc mockMvc = MockMvcBuilders
                .addInterceptors(new GlobalViewInterceptor())
                .build()
        when: "execute email"
        def result = mockMvc.perform(MockMvcRequestBuilders
                .get("/test/email")
                .param("to", "admin@test.com"))
                .andExpect { it.getResponse().getStatus() == status }
                .andReturn()
                .getResponse()
                .getContentAsString()
        then: "verify result"
        result.contains(keyText)
        where: "param role and result"
        role     | keyText  | status
        "ADMIN"  | "true"   | 200
        "NORMAL" | ""       | 403
        null     | "未登录" | 200
    }

    @MockStatic(EmailUtil)
    public static boolean sendMail(String title, String to, String content) {
        return true
    }

}
