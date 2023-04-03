package com.nowcoder.community.util;/**
 * @author DB1412
 * @create 2023-03-13 21:50
 */

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

/**
 *@ClassName MailClient
 *@Description 提供了发邮件的功能，将发邮件的事让新浪去完成
 *@Author DB1412
 *@Date 2023-03-13 21:50
 */

@Component
public class MailClient {

//    记录日志
    private static final Logger LOGGER = LoggerFactory.getLogger(MailClient.class);

    @Autowired
    private JavaMailSender mailSender;//发邮件的核心组件

    @Value("${spring.mail.username}")
    private String from;

    public void sendMail(String to, String subject, String content) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message);
            helper.setFrom(from);//设置邮件发送人
            helper.setTo(to);//设置接收人
            helper.setSubject(subject);//邮件主题
            helper.setText(content, true);//邮件内容，认为是HTML文件格式
            mailSender.send(helper.getMimeMessage());//发送
        } catch (MessagingException e) {
            LOGGER.error("发送邮件失败:" + e.getMessage());
        }
    }
}
