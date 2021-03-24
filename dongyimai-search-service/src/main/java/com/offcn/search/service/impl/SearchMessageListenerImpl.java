package com.offcn.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.offcn.pojo.TbItem;
import com.offcn.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import java.util.List;


@Component
public class SearchMessageListenerImpl implements MessageListener {

    @Autowired
    private ItemSearchService itemSearchService;

    public void onMessage(Message message) {
        if(message instanceof TextMessage){
            //1.类型转换
            TextMessage textMessage = (TextMessage) message;
            //2.将JSON结构的字符串转换成对象
            try {
                List<TbItem> itemList = JSON.parseArray(textMessage.getText(), TbItem.class);
                //3.调用服务，完成导入Solr操作
                itemSearchService.importItem(itemList);
                System.out.println("接收消息队列消息成功，完成导入solr操作");
            } catch (JMSException e) {
                e.printStackTrace();
            }

        }

    }
}
