package com.offcn.seckill.service;

import com.offcn.entity.PageResult;
import com.offcn.pojo.TbSeckillOrder;

import java.util.List;

/**
 * 服务层接口
 * @author Administrator
 *
 */
public interface SeckillOrderService {

	/**
	 * 返回全部列表
	 * @return
	 */
	public List<TbSeckillOrder> findAll();
	
	
	/**
	 * 返回分页列表
	 * @return
	 */
	public PageResult findPage(int pageNum, int pageSize);


	/**
	 * 增加
	*/
	public void add(TbSeckillOrder seckill_order);


	/**
	 * 修改
	 */
	public void update(TbSeckillOrder seckill_order);


	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	public TbSeckillOrder findOne(Long id);


	/**
	 * 批量删除
	 * @param ids
	 */
	public void delete(Long[] ids);

	/**
	 * 分页
	 * @param pageNum 当前页 码
	 * @param pageSize 每页记录数
	 * @return
	 */
	public PageResult findPage(TbSeckillOrder seckill_order, int pageNum, int pageSize);

	/**
	 * 秒杀商品提交订单
	 * @param seckillId    商品ID
	 * @param userId		当前登录用户ID
	 */
	public void submitOrder(Long seckillId,String userId);

	/**
	 * 通过当前登录人在缓存中查询订单信息
	 * @param userId
	 * @return
	 */
	public TbSeckillOrder searchOrderFromRedisByUserId(String userId);

	/**
	 * 保存秒杀订单到数据库
	 * @param userId
	 * @param orderId
	 * @param transactionId
	 */
	public void saveOrderFromRedisToDb(String userId,long orderId,String transactionId);

	/**
	 * 秒杀超时则删除订单
	 * @param userId
	 * @param orderId
	 */
	public void deleteOrderFromRedis(String userId,Long orderId);
}
