package com.offcn.sms.service.impl;

import com.offcn.utils.SmsUtil;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageListener;

@Component
public class SmsMessageListenerImpl implements MessageListener {

    @Autowired
    private SmsUtil smsUtil;

    @Override
    public void onMessage(Message message) {
        if(message instanceof MapMessage){
            MapMessage mapMessage = (MapMessage) message;
            try {
                String mobile = mapMessage.getString("mobile");
                String param = mapMessage.getString("param");
                System.out.println(mobile);
                System.out.println(param);
                HttpResponse response = smsUtil.sendMessage(mobile,param);
                System.out.println(EntityUtils.toString(response.getEntity()));
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }
}
