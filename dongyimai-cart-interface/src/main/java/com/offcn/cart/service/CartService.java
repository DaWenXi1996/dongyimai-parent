package com.offcn.cart.service;

import com.offcn.group.Cart;

import java.util.List;

public interface CartService {
    /**
     * 向购物车中添加或减少商品
     * @param oldCartList
     * @param itemId
     * @param num
     * @return
     */
    public List<Cart> addGoodsToCartList(List<Cart> oldCartList, Long itemId, Integer num);

    /**
     * 根据登录用户名在缓存中查询购物车集合
     * @param username
     * @return
     */
    public List<Cart> findCartListFromRedis(String username);

    /**
     * 根据登录用户名向缓存存储购物车集合
     * @param cartList
     * @param username
     */
    public void saveCartListToRedis(List<Cart> cartList,String username);

    /**
     * 合并购物车
     * @param cartList_cookie
     * @param cartList_redis
     * @return
     */
    public List<Cart> margeCartList(List<Cart> cartList_cookie,List<Cart> cartList_redis);
}
