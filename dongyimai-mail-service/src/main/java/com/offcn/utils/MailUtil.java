package com.offcn.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.File;

@Component
public class MailUtil {

    @Autowired
    private JavaMailSenderImpl mailSender;

    public void sendmail(String name) {

        MimeMessage mimeMessage = mailSender.createMimeMessage();

        try {
            //创建助手类
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage,true,"GBK");
            helper.setFrom("1761086092@qq.com");
            helper.setTo("1761086092@qq.com");
            helper.setSubject("ceshifujian");
            helper.setText("<h1>欢迎"+name+"使用东易买</h1><img src='cid:aaa'>",true);
            //读取本地文件
            File file1 = new File("C:\\Users\\17610\\Pictures\\Camera Roll\\1.jpg");
            //helper.addAttachment("111.jpg",file1);
            helper.addInline("aaa",file1);

        } catch (MessagingException e) {
            e.printStackTrace();
        }
        //发送邮件
        mailSender.send(mimeMessage);
        System.out.println("邮件发送成功");
    }
}
