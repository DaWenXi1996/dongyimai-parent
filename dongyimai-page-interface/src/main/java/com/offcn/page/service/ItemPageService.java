package com.offcn.page.service;

public interface ItemPageService {

    /**
     * 根据goodsid生成静态页面
     * @param goodsId
     * @return
     */
    public boolean  genItemHtml(Long goodsId);

    /**
     * 删除商品详情页
     * @param goodsId
     */
    public void deleteItemHtml(Long goodsId);
}
