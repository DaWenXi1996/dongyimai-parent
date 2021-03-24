package com.offcn.shop.service;

import com.alibaba.dubbo.config.annotation.Reference;
import com.offcn.pojo.TbSeller;
import com.offcn.sellergoods.service.SellerService;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.ArrayList;
import java.util.List;

public class UserDetailServiceImpl implements UserDetailsService {
    @Reference
    private SellerService sellerService;

    @Override
    public UserDetails loadUserByUsername(String sellerId) throws UsernameNotFoundException {
        //1.加载用户权限集合
        List<GrantedAuthority> list = new ArrayList<GrantedAuthority>();
        list.add(new SimpleGrantedAuthority("ROLE_SELLER"));
        //2.匹配用户名和密码
        //通过sellerId查询商家对象
        TbSeller tbSeller = sellerService.findOne(sellerId);
        if (null != tbSeller) {
            //判断审核状态为审核通过
            if (tbSeller.getStatus().equals("1")) {
                return new User(sellerId, tbSeller.getPassword(), list);
            } else {
                return null;
            }
        } else {
            return null;
        }
    }
}