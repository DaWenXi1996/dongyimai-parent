package com.offcn.page.service.impl;

import com.offcn.page.service.ItemPageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;


@Component
public class PageMessageListenerImpl implements MessageListener {

    @Autowired
    private ItemPageService itemPageService;

    public void onMessage(Message message) {
        if(message instanceof TextMessage){
            TextMessage textMessage = (TextMessage) message;
            try {
                String text = textMessage.getText();

                Long id = Long.parseLong(text);
                System.out.println("接收的商品ID："+id);
                itemPageService.genItemHtml(id);
                System.out.println("接收消息队列的消息成功，完成生成页面操作");
            } catch (JMSException e) {
                e.printStackTrace();
            }
        }
    }
}
