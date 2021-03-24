package com.offcn.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.offcn.cart.service.CartService;
import com.offcn.entity.Result;
import com.offcn.group.Cart;
import com.offcn.utils.CookieUtil;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
@RequestMapping("/cart")
public class CartController {

    @Reference
    private CartService cartService;

    /**
     *
     *
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "/findCartList")
    public List<Cart> findCartList(HttpServletRequest request, HttpServletResponse response){
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        String cartListStr = CookieUtil.getCookieValue(request, "cartListStr", "UTF-8");
        if (StringUtils.isEmpty(cartListStr)){
            cartListStr = "[]";
        }
        List<Cart> cartList_cookie = JSON.parseArray(cartListStr, Cart.class);
        if (username.equals("anonymousUser")) {//判断用户未登录
            return cartList_cookie;
        } else {
            List<Cart> cartList_redis = cartService.findCartListFromRedis(username);
            if (CollectionUtils.isEmpty(cartList_cookie)){
                return cartList_redis;
            } else {
                cartList_redis = cartService.margeCartList(cartList_cookie,cartList_redis);
                cartService.saveCartListToRedis(cartList_redis,username);
                CookieUtil.deleteCookie(request,response,"cartListStr");
                return cartList_redis;
            }
        }
    }

    @RequestMapping("/addGoodsToCartList")
    @CrossOrigin(origins = "http://localhost:9105",allowCredentials = "true")
    public Result addGoodsToCartList(Long itemId, Integer num, HttpServletRequest request, HttpServletResponse response){
        try {
            List<Cart> oldCartList = this.findCartList(request, response);
            List<Cart> newCartList = cartService.addGoodsToCartList(oldCartList, itemId, num);
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            if(username.equals("anonymousUser")){
                //3.将更新过的购物车集合重新放回到Cookie
                CookieUtil.setCookie(request, response, "cartListStr", JSON.toJSONString(newCartList), 60 * 60 * 24, "UTF-8");
            }else{
                cartService.saveCartListToRedis(newCartList,username);
            }
            return new Result(true,"添加购物车成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"添加购物车失败");
        }
    }
}
