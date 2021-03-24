package com.offcn.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.offcn.cart.service.CartService;
import com.offcn.group.Cart;
import com.offcn.mapper.TbItemMapper;
import com.offcn.pojo.TbItem;
import com.offcn.pojo.TbOrderItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private TbItemMapper tbItemMapper;
    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public List<Cart> addGoodsToCartList(List<Cart> oldCartList, Long itemId, Integer num) {
        TbItem tbItem = tbItemMapper.selectByPrimaryKey(itemId);
        if (tbItem == null){
            throw new RuntimeException("商品不存在");
        }
        if (!tbItem.getStatus().equals("1")){
            throw new RuntimeException("商品未审核通过");
        }
        String sellerId = tbItem.getSellerId();
        String sellerName = tbItem.getSeller();
//        判断购物车中有没有这个店铺
        Cart cart = this.searchCartBySellerId(oldCartList, sellerId);
        if (cart == null){     //没有这个店铺
            cart = new Cart();
            cart.setSellerId(sellerId);
            cart.setSellerName(sellerName);
            List<TbOrderItem> orderItemList = new ArrayList<TbOrderItem>();    //订单详情集合
            TbOrderItem tbOrderItem = this.setOrderItem(tbItem,num);
            orderItemList.add(tbOrderItem);
            cart.setOrderItemList(orderItemList);
            oldCartList.add(cart);
        } else {      //有这个店铺
            List<TbOrderItem> orderItemList = cart.getOrderItemList();
//            判断店铺中有没有这个商品
            TbOrderItem tbOrderItem = this.searchOrderItemByItemId(orderItemList, itemId);
            if (tbOrderItem == null){       //没有这个商品
                tbOrderItem = this.setOrderItem(tbItem,num);
                cart.getOrderItemList().add(tbOrderItem);
            } else {        //有这个商品
                tbOrderItem.setNum(tbOrderItem.getNum()+num);
                tbOrderItem.setTotalFee(tbOrderItem.getPrice().multiply(new BigDecimal(tbOrderItem.getNum())));  //总金额
                if(tbOrderItem.getNum() == 0){       //如果这个商品数量变为零把商品从店铺中移除
                    orderItemList.remove(tbOrderItem);
                }
                if(orderItemList.size() == 0){       //如果这个店铺的商品数量变为零把店铺中从购物车中移除
                    oldCartList.remove(cart);
                }
            }
        }
        List<Cart> newCartList = new ArrayList<Cart>();
        newCartList = oldCartList;
        return newCartList;
    }

    private Cart searchCartBySellerId(List<Cart> cartList, String sellerId) {
        for (Cart cart : cartList) {
            if (cart.getSellerId().equals(sellerId)) {
                return cart;
            }
        }
        return null;
    }

    private TbOrderItem setOrderItem(TbItem tbItem,Integer num){
        if(num<1){
            throw new RuntimeException("购买数量非法");
        }
        TbOrderItem tbOrderItem = new TbOrderItem();
        tbOrderItem.setItemId(tbItem.getId());
        tbOrderItem.setGoodsId(tbItem.getGoodsId());
        tbOrderItem.setTitle(tbItem.getTitle());
        tbOrderItem.setPrice(tbItem.getPrice());
        tbOrderItem.setNum(num);
        tbOrderItem.setTotalFee(tbOrderItem.getPrice().multiply(new BigDecimal(num)));
        tbOrderItem.setPicPath(tbItem.getImage());
        tbOrderItem.setSellerId(tbItem.getSellerId());
        return tbOrderItem;
    }

    private TbOrderItem searchOrderItemByItemId(List<TbOrderItem> orderItemList,Long itemId){
        for(TbOrderItem orderItem:orderItemList){
            if(orderItem.getItemId().longValue() == itemId.longValue()){
                return orderItem;
            }
        }
        return null;
    }

    @Override
    public List<Cart> findCartListFromRedis(String username) {
        List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps("cartList").get(username);
        if (CollectionUtils.isEmpty(cartList)){
            cartList = new ArrayList<Cart>();
        }
        return cartList;
    }

    @Override
    public void saveCartListToRedis(List<Cart> cartList, String username) {
        redisTemplate.boundHashOps("cartList").put(username,cartList);
    }

    @Override
    public List<Cart> margeCartList(List<Cart> cartList_cookie, List<Cart> cartList_redis) {
        for (Cart cart : cartList_cookie) {
            for (TbOrderItem tbOrderItem : cart.getOrderItemList()) {
                cartList_redis = this.addGoodsToCartList(cartList_redis,tbOrderItem.getItemId(),tbOrderItem.getNum());
            }
        }
        return cartList_redis;
    }
}
