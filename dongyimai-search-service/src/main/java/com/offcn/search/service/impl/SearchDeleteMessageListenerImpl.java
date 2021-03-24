package com.offcn.search.service.impl;

import com.offcn.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import java.util.Arrays;


@Component
public class SearchDeleteMessageListenerImpl implements MessageListener {

    @Autowired
    private ItemSearchService itemSearchService;

    public void onMessage(Message message) {
        if(message instanceof ObjectMessage){
            ObjectMessage objectMessage = (ObjectMessage) message;
            try {
                Long[] ids = (Long[]) objectMessage.getObject();
                System.out.println("删除商品的ID："+ids);
                itemSearchService.deleteByGoodsIds(Arrays.asList(ids));
                System.out.println("接收消息队列的消息成功，完成删除操作");
            } catch (JMSException e) {
                e.printStackTrace();
            }
        }
    }
}
