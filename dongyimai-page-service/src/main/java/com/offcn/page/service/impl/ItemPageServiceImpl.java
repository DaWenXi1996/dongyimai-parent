package com.offcn.page.service.impl;

import com.offcn.mapper.TbGoodsDescMapper;
import com.offcn.mapper.TbGoodsMapper;
import com.offcn.mapper.TbItemCatMapper;
import com.offcn.mapper.TbItemMapper;
import com.offcn.page.service.ItemPageService;
import com.offcn.pojo.*;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ItemPageServiceImpl implements ItemPageService {

    @Autowired
    private FreeMarkerConfigurer freeMarkerConfigurer;
    @Autowired
    private TbGoodsMapper tbGoodsMapper;
    @Autowired
    private TbGoodsDescMapper tbGoodsDescMapper;
    @Autowired
    private TbItemMapper tbItemMapper;
    @Autowired
    private TbItemCatMapper tbItemCatMapper;
    @Value("${pagedir}")
    private String pagedir;

    @Override
    public boolean genItemHtml(Long goodsId) {
        try {
            Configuration configuration = freeMarkerConfigurer.getConfiguration();
            Template template = configuration.getTemplate("item.ftl");
            Map<String,Object> dataSource = new HashMap<String,Object>();
            TbGoods tbGoods = tbGoodsMapper.selectByPrimaryKey(goodsId);
            TbGoodsDesc tbGoodsDesc = tbGoodsDescMapper.selectByPrimaryKey(goodsId);
            TbItemCat itemCat1 = tbItemCatMapper.selectByPrimaryKey(tbGoods.getCategory1Id());
            TbItemCat itemCat2 = tbItemCatMapper.selectByPrimaryKey(tbGoods.getCategory2Id());
            TbItemCat itemCat3 = tbItemCatMapper.selectByPrimaryKey(tbGoods.getCategory3Id());
            TbItemExample tbItemExample = new TbItemExample();
            TbItemExample.Criteria criteria = tbItemExample.createCriteria();
            criteria.andGoodsIdEqualTo(goodsId);
            criteria.andStatusEqualTo("1");
            tbItemExample.setOrderByClause("is_default desc");
            List<TbItem> tbItemList = tbItemMapper.selectByExample(tbItemExample);
            dataSource.put("goods",tbGoods);
            dataSource.put("goodsDesc",tbGoodsDesc);
            dataSource.put("itemCat1",itemCat1);
            dataSource.put("itemCat2",itemCat2);
            dataSource.put("itemCat3",itemCat3);
            dataSource.put("itemList",tbItemList);
            FileWriter fileOut = new FileWriter(pagedir + goodsId + ".html");
            template.process(dataSource,fileOut);
            fileOut.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void deleteItemHtml(Long goodsId) {
        System.out.println("删除商品的ID："+goodsId);
        new File(pagedir+goodsId+".html").delete();
    }
}
