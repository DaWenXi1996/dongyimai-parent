package com.offcn.utils;

import com.alibaba.fastjson.JSON;
import com.github.promeg.pinyinhelper.Pinyin;
import com.offcn.mapper.TbItemMapper;
import com.offcn.pojo.TbItem;
import com.offcn.pojo.TbItemExample;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.Query;
import org.springframework.data.solr.core.query.SimpleQuery;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 导入solr数据的工具类
 */
@Component
public class SolrUtil {

    @Autowired
    private TbItemMapper itemMapper;

    @Autowired
    private SolrTemplate solrTemplate;

    public void importItemList(){
        //1.执行查询SKU列表数据
        TbItemExample tbItemExample = new TbItemExample();
        TbItemExample.Criteria criteria = tbItemExample.createCriteria();
        criteria.andStatusEqualTo("1");    //审核通过
        List<TbItem> itemList = itemMapper.selectByExample(tbItemExample);
        for(TbItem item:itemList){
            //{"机身内存":"16G","网络":"联通3G"}
            Map<String,String> specMap = JSON.parseObject(item.getSpec(),Map.class);
            Map<String,String> pinyinMap = new HashMap<String, String>();
            for(String key:specMap.keySet()){
                //将key 做拼音转换  item_spec_jishenneicun
                pinyinMap.put( Pinyin.toPinyin(key,"").toLowerCase(),specMap.get(key));
            }
            item.setSpecMap(pinyinMap);
            System.out.println(item.getTitle()+"---"+item.getPrice());
        }
        solrTemplate.saveBeans(itemList);
        solrTemplate.commit();
        System.out.println("导入成功");
    }

    public static void main(String[] args) {
        ApplicationContext context = new ClassPathXmlApplicationContext("classpath*:spring/applicationContext-*.xml");
        SolrUtil solrUtil = (SolrUtil) context.getBean("solrUtil");
        solrUtil.deleteAll();
        solrUtil.importItemList();
    }


    public void deleteAll(){
        Query query = new SimpleQuery("*:*");
        solrTemplate.delete(query);
        solrTemplate.commit();
        System.out.println("删除成功");
    }
}
