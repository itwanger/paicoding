package com.github.paicoding.forum.test.pwd;

import org.junit.Test;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;

/**
 * @author XuYifei
 * @date 2024-07-12
 */
public class PwdGenTest {

    @Test
    public void testGen() {
        String salt = "tech_π";
        int saltIndex = 3;
        String plainPwd = "123456";
        if (plainPwd.length() > saltIndex) {
            plainPwd = plainPwd.substring(0, saltIndex) + salt + plainPwd.substring(saltIndex);
        } else {
            plainPwd = plainPwd + salt;
        }
        System.out.println(DigestUtils.md5DigestAsHex(plainPwd.getBytes(StandardCharsets.UTF_8)));

    }
}
