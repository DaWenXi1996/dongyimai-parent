package com.offcn.search.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.promeg.pinyinhelper.Pinyin;
import com.offcn.pojo.TbItem;
import com.offcn.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.*;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ItemSearchServiceImpl implements ItemSearchService {

    @Autowired
    private SolrTemplate solrTemplate;
    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public Map<String, Object> search(Map<String, Object> searchMap) {
//        Query query = new SimpleQuery();
//        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
//        query.addCriteria(criteria);
//        ScoredPage<TbItem> page = solrTemplate.queryForPage(query, TbItem.class);
//        List<TbItem> itemList = page.getContent();
        if (!StringUtils.isEmpty(searchMap.get("keywords")) && ((String) searchMap.get("keywords")).indexOf(" ") > -1) {
            String keywords = ((String) searchMap.get("keywords")).replace(" ", "");   //除去中间空格
            searchMap.put("keywords", keywords);
        }
        Map resultMap = new HashMap();
        resultMap.putAll(this.searchList(searchMap));

//        获取符合条件所有的分类模板名（category）给前端显示选择
        List categoryList = this.findCategoryList(searchMap);
        if (!CollectionUtils.isEmpty(categoryList)) {
            resultMap.put("categoryList", categoryList);
        }

        //根据分类模板名查询品牌和规格列表
        String category = (String) searchMap.get("category");
        if(!StringUtils.isEmpty(category)){
            resultMap.putAll(this.findBrandAndSpecList(category));
        }else {   //前端用户不点击分类，则默认查询分类集合第一个的内容
            if (!CollectionUtils.isEmpty(categoryList)) {
                resultMap.putAll(this.findBrandAndSpecList((String) categoryList.get(0)));
            }
        }

        return resultMap;
    }
    
    private Map<String, Object> searchList(Map<String, Object> searchMap){
        Map resultMap = new HashMap();
        HighlightQuery query = new SimpleHighlightQuery();
        HighlightOptions options = new HighlightOptions();
        options.addField("item_title");
        options.setSimplePrefix("<em style='color: red'>");
        options.setSimplePostfix("</em>");
        query.setHighlightOptions(options);
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        query.addCriteria(criteria);
        if(!StringUtils.isEmpty(searchMap.get("category"))){
            SimpleFilterQuery filterQuery = new SimpleFilterQuery();
            Criteria categoryCriteria = new Criteria("item_category").is(searchMap.get("category"));
            filterQuery.addCriteria(categoryCriteria);
            query.addFilterQuery(filterQuery);
        }
        if(!StringUtils.isEmpty(searchMap.get("price"))){
            String price = (String) searchMap.get("price");
            String[] priceArr = price.split("-");
            if (!priceArr[0].equals("0")){
                SimpleFilterQuery filterQuery = new SimpleFilterQuery();
                Criteria priceCriteria = new Criteria("item_price").greaterThanEqual(priceArr[0]);
                filterQuery.addCriteria(priceCriteria);
                query.addFilterQuery(filterQuery);
            }
            if (!priceArr[1].equals("*")){
                SimpleFilterQuery filterQuery = new SimpleFilterQuery();
                Criteria priceCriteria = new Criteria("item_price").lessThan(priceArr[1]);
                filterQuery.addCriteria(priceCriteria);
                query.addFilterQuery(filterQuery);
            }
        }
        if(!StringUtils.isEmpty(searchMap.get("brand"))){
            SimpleFilterQuery filterQuery = new SimpleFilterQuery();
            Criteria brandCriteria = new Criteria("item_brand").is(searchMap.get("brand"));
            filterQuery.addCriteria(brandCriteria);
            query.addFilterQuery(filterQuery);
        }
        if(null!=searchMap.get("spec")){
            Map<String,String> specMap= (Map<String, String>) searchMap.get("spec");
            for (String key : specMap.keySet()) {
                Criteria specCriteria = new Criteria("item_spec_"+Pinyin.toPinyin(key,"").toLowerCase()).is(specMap.get(key));
                SimpleFilterQuery filterQuery = new SimpleFilterQuery();
                filterQuery.addCriteria(specCriteria);
                query.addFilterQuery(filterQuery);
            }
        }

//        设置分页
        Integer pageNo = (Integer) searchMap.get("pageNo");     //当前页码
        if (pageNo == null) {
            pageNo = 1;
        }
        Integer pageSize = (Integer) searchMap.get("pageSize");   //每页显示记录数
        if (pageSize == null) {
            pageSize = 20;
        }
        query.setOffset((pageNo - 1) * pageSize);    //起始记录数
        query.setRows(pageSize);    //查询记录数

        String sortField = (String) searchMap.get("sortField");   //排序字段
        String sortValue = (String) searchMap.get("sortValue");     //排序规则
        if(!StringUtils.isEmpty(sortField)){
            if(sortValue.equals("ASC")){
                Sort sort = new Sort(Sort.Direction.ASC,"item_"+sortField);
                query.addSort(sort);
            }
            if(sortValue.equals("DESC")){
                Sort sort = new Sort(Sort.Direction.DESC,"item_"+sortField);
                query.addSort(sort);
            }
        }
        HighlightPage<TbItem> page = solrTemplate.queryForHighlightPage(query, TbItem.class);
        List<HighlightEntry<TbItem>> highlightEntryList = page.getHighlighted();
        for (HighlightEntry<TbItem> entry : highlightEntryList) {
            //获得SKU对象
            TbItem item = (TbItem) entry.getEntity();
            //注意：此处取得高亮结果之前需要加判空操作
            if (entry.getHighlights().size() > 0 && entry.getHighlights().get(0).getSnipplets().size() > 0) {
                List<String> snippletList = entry.getHighlights().get(0).getSnipplets();
                item.setTitle(snippletList.get(0));
            }
        }
        resultMap.put("rows", page.getContent());
        resultMap.put("total", page.getTotalElements());     //总记录数
        resultMap.put("totalPages", page.getTotalPages());   //总页数
        return resultMap;
    }

    private List<String> findCategoryList(Map<String, Object> searchMap) {
        List<String> categoryList = new ArrayList<String>();
        Query query = new SimpleQuery();
        GroupOptions groupOptions = new GroupOptions();
        groupOptions.addGroupByField("item_category");
        query.setGroupOptions(groupOptions);
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        query.addCriteria(criteria);
        GroupPage<TbItem> groupPage = solrTemplate.queryForGroupPage(query, TbItem.class);
        GroupResult<TbItem> groupResult = groupPage.getGroupResult("item_category");
        Page<GroupEntry<TbItem>> page = groupResult.getGroupEntries();
        //6.返回分组结果集合
        List<GroupEntry<TbItem>> list = page.getContent();
        for (GroupEntry entry : list) {
            categoryList.add(entry.getGroupValue());
        }
        return categoryList;
    }

    public Map<String, Object> findBrandAndSpecList(String category) {
        Map resultMap = new HashMap();
        Long typeId = (Long) redisTemplate.boundHashOps("itemCat").get(category);
        if(typeId!=null){
            List<Map> brandList = (List<Map>) redisTemplate.boundHashOps("brandList").get(typeId);
            resultMap.put("brandList",brandList);
            List<Map> specList = (List<Map>) redisTemplate.boundHashOps("specList").get(typeId);
            resultMap.put("specList",specList);
        }
        return resultMap;
    }

    @Override
    public void importItem(List<TbItem> itemList) {
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

    @Override
    public void deleteByGoodsIds(List goodsIds) {
        Query query = new SimpleQuery();
        Criteria criteria = new Criteria("item_goodsid").in(goodsIds);
        query.addCriteria(criteria);
        solrTemplate.delete(query);
        solrTemplate.commit();
        System.out.println("删除SKU成功");
    }
}
