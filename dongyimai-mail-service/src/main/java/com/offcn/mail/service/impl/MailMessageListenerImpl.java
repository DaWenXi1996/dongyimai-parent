package com.offcn.mail.service.impl;


import com.offcn.utils.MailUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

@Component
public class MailMessageListenerImpl implements MessageListener {

    @Autowired
    private MailUtil mailUtil;

    @Override
    public void onMessage(Message message) {
        if (message instanceof TextMessage) {
            TextMessage textMessage = (TextMessage) message;
            try {
                String name = textMessage.getText();
                mailUtil.sendmail(name);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }
}
