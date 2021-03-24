package com.offcn.search.service;

import com.offcn.pojo.TbItem;

import java.util.List;
import java.util.Map;

public interface ItemSearchService {

    /**
     * 搜索
     * @param searchMap
     * @return
     */
    public Map<String,Object> search(Map<String, Object> searchMap);

    /**
     * 增量导入solr数据
     * @param itemList
     */
    public void importItem(List<TbItem> itemList);

    /**
     * 在solr中删除SKU信息
     * @param goodsIds
     */
    public void deleteByGoodsIds(List goodsIds);
}
