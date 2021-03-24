package com.offcn.pay.service;

import com.offcn.pojo.TbPayLog;

import java.util.Map;

public interface AliPayService {

    /**
     * 预下单生成二维码
     * @param out_trade_no    订单编号  注意：不允许重复
     * @param total_amount      支付金额 单位：（分）
     * @return
     */
    public Map<String,Object> createNative(String out_trade_no, String total_amount);


    /**
     * 查询交易状态
     * @param out_trade_no   订单编号
     * @return
     */
    public Map<String,Object> queryPayStatus(String out_trade_no);

    /**
     * 从redis中获取当前登陆人的支付日志
     * @param userId
     * @return
     */
    public TbPayLog searchPayLogFromRedis(String userId);
}
