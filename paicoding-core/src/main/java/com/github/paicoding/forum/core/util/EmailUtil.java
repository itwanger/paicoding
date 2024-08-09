package com.github.paicoding.forum.core.util;

import com.github.hui.quick.plugin.base.file.FileReadUtil;
import com.github.paicoding.forum.core.async.AsyncUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.io.IOUtils;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import javax.mail.internet.MimeMessage;

/**
 * @author YiHui
 * @date 2023/3/19
 */
@Slf4j
public class EmailUtil {
    private static volatile String from;

    public static String getFrom() {
        if (from == null) {
            synchronized (EmailUtil.class) {
                if (from == null) {
                    from = SpringUtil.getConfig("spring.mail.from", "xhhuiblog@163.com");
                }
            }
        }
        return from;
    }

    /**
     * springboot-email封装的发送邮件
     *
     * @param title   邮件标题
     * @param to      接收人
     * @param content 内容
     * @return 返回是否发送成功
     */
    public static boolean sendMail(String title, String to, String content) {
        try {
            log.info("开始发送邮件 {} - {}", title, to);
            JavaMailSender javaMailSender = SpringUtil.getBean(JavaMailSender.class);
            MimeMessage mimeMailMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMailMessage, true);
            mimeMessageHelper.setFrom(getFrom());
            mimeMessageHelper.setTo(to);
            mimeMessageHelper.setSubject(title);
            //邮件内容，第二个参数设置为true，支持html模板
            mimeMessageHelper.setText(content, true);
            // 解决 JavaMailSender no object DCH for MIME type multipart/mixed 问题
            // 详情参考：[Email发送失败问题记录 - 一灰灰Blog](https://blog.hhui.top/hexblog/2021/10/28/211028-Email%E5%8F%91%E9%80%81%E5%A4%B1%E8%B4%A5%E9%97%AE%E9%A2%98%E8%AE%B0%E5%BD%95/)
            Thread.currentThread().setContextClassLoader(EmailUtil.class.getClassLoader());
            javaMailSender.send(mimeMailMessage);
            return true;
        } catch (Exception e) {
            log.warn("sendEmail error {} - {}", title, to, e);
            return false;
        } finally {
            log.info("完成发送邮件 {} - {}", title, to);
        }
    }

    /**
     * 发送邮件
     *
     * @param title
     * @param to
     * @param content
     * @param attachmentName
     * @param attachment
     * @return
     */
    public static boolean sendMail(String title, String to, String content, String attachmentName, String attachment) {
        try {
            JavaMailSender javaMailSender = SpringUtil.getBean(JavaMailSender.class);
            MimeMessage mimeMailMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMailMessage, true);
            mimeMessageHelper.setFrom(getFrom());
            mimeMessageHelper.setTo(to);
            mimeMessageHelper.setSubject(title);
            //邮件内容，第二个参数设置为true，支持html模板
            mimeMessageHelper.setText(content, true);
            Thread.currentThread().setContextClassLoader(EmailUtil.class.getClassLoader());

            // 发送附件
            mimeMessageHelper.addAttachment(attachmentName, new ByteArrayResource(IOUtils.toByteArray(FileReadUtil.getStreamByFileName(attachment))));
            javaMailSender.send(mimeMailMessage);
            return true;
        } catch (Exception e) {
            log.warn("sendEmail error {} - {}", title, to, e);
            return false;
        }
    }

}
