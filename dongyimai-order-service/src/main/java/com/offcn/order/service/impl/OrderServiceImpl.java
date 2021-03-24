package com.offcn.order.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.offcn.entity.PageResult;
import com.offcn.group.Cart;
import com.offcn.mapper.TbOrderItemMapper;
import com.offcn.mapper.TbOrderMapper;
import com.offcn.mapper.TbPayLogMapper;
import com.offcn.order.service.OrderService;
import com.offcn.pojo.TbOrder;
import com.offcn.pojo.TbOrderExample;
import com.offcn.pojo.TbOrderExample.Criteria;
import com.offcn.pojo.TbOrderItem;
import com.offcn.pojo.TbPayLog;
import com.offcn.utils.IdWorker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
public class OrderServiceImpl implements OrderService {

	@Autowired
	private TbOrderMapper orderMapper;
	@Autowired
	private RedisTemplate redisTemplate;
	@Autowired
	private IdWorker idWorker;
	@Autowired
	private TbOrderItemMapper orderItemMapper;
	@Autowired
	private TbPayLogMapper payLogMapper;
	
	/**
	 * 查询全部
	 */
	@Override
	public List<TbOrder> findAll() {
		return orderMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbOrder> page=   (Page<TbOrder>) orderMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(TbOrder order) {
		List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps("cartList").get(order.getUserId());
		if (!CollectionUtils.isEmpty(cartList)) {
			double total_fee = 0.00;
			ArrayList<String> orderList = new ArrayList<>();
			for (Cart cart : cartList) {
				TbOrder tbOrder = new TbOrder();
				long orderId = idWorker.nextId();                                       //订单ID
				System.out.println("orderId:" + orderId);
				orderList.add(orderId+"");
				tbOrder.setOrderId(orderId);
				tbOrder.setPaymentType(order.getPaymentType());                       //支付方式
				tbOrder.setStatus("1");                                              //状态：1、未付款，2、已付款，3、未发货，4、已发货，5、交易成功，6、交易关闭,7、待评价
				tbOrder.setCreateTime(new Date());                                   //创建时间
				tbOrder.setUpdateTime(new Date());                                   //更新时间
				tbOrder.setUserId(order.getUserId());                                //用户ID
				tbOrder.setReceiverAreaName(order.getReceiverAreaName());            //收货地址
				tbOrder.setReceiverMobile(order.getReceiverMobile());                //联系人电话
				tbOrder.setReceiver(order.getReceiver());                            //收货人
				tbOrder.setSourceType(order.getSourceType());                        //订单来源：1:app端，2：pc端，3：M端，4：微信端，5：手机qq端
				tbOrder.setSellerId(cart.getSellerId());                            //商家ID

				double money = 0.00;
				//3.遍历订单详情列表，保存订单详情对象
				for (TbOrderItem orderItem : cart.getOrderItemList()) {
					orderItem.setId(idWorker.nextId());                  //订单详情ID
					orderItem.setOrderId(orderId);                      //订单ID
					money += orderItem.getTotalFee().doubleValue();    //计算订单实付金额
					//执行保存订单详情
					orderItemMapper.insert(orderItem);
				}
				total_fee += money;
				tbOrder.setPayment(new BigDecimal(money));              //实付金额
				//执行保存订单
				orderMapper.insert(tbOrder);
			}

			if (order.getPaymentType().equals("1")){
				TbPayLog tbPayLog = new TbPayLog();
				tbPayLog.setOutTradeNo(idWorker.nextId()+"");
				tbPayLog.setCreateTime(new Date());
				BigDecimal bigMoney = new BigDecimal(total_fee);
				BigDecimal cs = new BigDecimal(100L);
				BigDecimal totalFee_big = bigMoney.multiply(cs);
				tbPayLog.setTotalFee(totalFee_big.longValue());
				tbPayLog.setUserId(order.getUserId());
				String orderListStr = orderList.toString().replace("[", "").replace("]", "").replace(" ", "");
				tbPayLog.setOrderList(orderListStr);
				tbPayLog.setPayType(order.getPaymentType());
				tbPayLog.setTradeState("0");
				payLogMapper.insert(tbPayLog);
				redisTemplate.boundHashOps("payLog").put(order.getUserId(), tbPayLog);
			}

			//4.清空当前登录人的缓存中的购物车列表
			redisTemplate.boundHashOps("cartList").delete(order.getUserId());
		}
	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(TbOrder order){
		orderMapper.updateByPrimaryKey(order);
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbOrder findOne(Long orderId){
		return orderMapper.selectByPrimaryKey(orderId);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] orderIds) {
		for(Long orderId:orderIds){
			orderMapper.deleteByPrimaryKey(orderId);
		}		
	}
	
	
		@Override
	public PageResult findPage(TbOrder order, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbOrderExample example=new TbOrderExample();
		Criteria criteria = example.createCriteria();
		
		if(order!=null){			
						if(order.getPaymentType()!=null && order.getPaymentType().length()>0){
				criteria.andPaymentTypeLike("%"+order.getPaymentType()+"%");
			}			if(order.getPostFee()!=null && order.getPostFee().length()>0){
				criteria.andPostFeeLike("%"+order.getPostFee()+"%");
			}			if(order.getStatus()!=null && order.getStatus().length()>0){
				criteria.andStatusLike("%"+order.getStatus()+"%");
			}			if(order.getShippingName()!=null && order.getShippingName().length()>0){
				criteria.andShippingNameLike("%"+order.getShippingName()+"%");
			}			if(order.getShippingCode()!=null && order.getShippingCode().length()>0){
				criteria.andShippingCodeLike("%"+order.getShippingCode()+"%");
			}			if(order.getUserId()!=null && order.getUserId().length()>0){
				criteria.andUserIdLike("%"+order.getUserId()+"%");
			}			if(order.getBuyerMessage()!=null && order.getBuyerMessage().length()>0){
				criteria.andBuyerMessageLike("%"+order.getBuyerMessage()+"%");
			}			if(order.getBuyerNick()!=null && order.getBuyerNick().length()>0){
				criteria.andBuyerNickLike("%"+order.getBuyerNick()+"%");
			}			if(order.getBuyerRate()!=null && order.getBuyerRate().length()>0){
				criteria.andBuyerRateLike("%"+order.getBuyerRate()+"%");
			}			if(order.getReceiverAreaName()!=null && order.getReceiverAreaName().length()>0){
				criteria.andReceiverAreaNameLike("%"+order.getReceiverAreaName()+"%");
			}			if(order.getReceiverMobile()!=null && order.getReceiverMobile().length()>0){
				criteria.andReceiverMobileLike("%"+order.getReceiverMobile()+"%");
			}			if(order.getReceiverZipCode()!=null && order.getReceiverZipCode().length()>0){
				criteria.andReceiverZipCodeLike("%"+order.getReceiverZipCode()+"%");
			}			if(order.getReceiver()!=null && order.getReceiver().length()>0){
				criteria.andReceiverLike("%"+order.getReceiver()+"%");
			}			if(order.getInvoiceType()!=null && order.getInvoiceType().length()>0){
				criteria.andInvoiceTypeLike("%"+order.getInvoiceType()+"%");
			}			if(order.getSourceType()!=null && order.getSourceType().length()>0){
				criteria.andSourceTypeLike("%"+order.getSourceType()+"%");
			}			if(order.getSellerId()!=null && order.getSellerId().length()>0){
				criteria.andSellerIdLike("%"+order.getSellerId()+"%");
			}	
		}
		
		Page<TbOrder> page= (Page<TbOrder>)orderMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}

	@Override
	public void updateOrderStatus(String out_trade_no, String transaction_id) {
		TbPayLog tbPayLog = payLogMapper.selectByPrimaryKey(out_trade_no);
		tbPayLog.setPayTime(new Date());            //支付时间
		tbPayLog.setTradeState("1");                //支付状态 已支付
		tbPayLog.setTransactionId(transaction_id);  //交易流水号
		payLogMapper.updateByPrimaryKey(tbPayLog);
		String orderList = tbPayLog.getOrderList();   //订单id集合  111,222,333
		String[] orderIds = orderList.split(",");
		for (String orderId : orderIds) {
			TbOrder tbOrder = orderMapper.selectByPrimaryKey(Long.parseLong(orderId));
			tbOrder.setPaymentTime(new Date());      //付款时间
			tbOrder.setStatus("2");                 //支付状态  已付款
			orderMapper.updateByPrimaryKey(tbOrder);
		}
		//3.清空缓存中的支付日志
		redisTemplate.boundHashOps("payLog").delete(tbPayLog.getUserId());
	}
}
