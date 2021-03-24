package com.offcn.seckill.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.offcn.entity.PageResult;
import com.offcn.mapper.TbSeckillGoodsMapper;
import com.offcn.mapper.TbSeckillOrderMapper;
import com.offcn.pojo.TbSeckillGoods;
import com.offcn.pojo.TbSeckillOrder;
import com.offcn.pojo.TbSeckillOrderExample;
import com.offcn.pojo.TbSeckillOrderExample.Criteria;
import com.offcn.seckill.service.SeckillOrderService;
import com.offcn.seckill.utils.RedisLock;
import com.offcn.utils.IdWorker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Date;
import java.util.List;

/**
 * 服务实现层
 *
 * @author Administrator
 */
@Service
public class SeckillOrderServiceImpl implements SeckillOrderService {

    @Autowired
    private TbSeckillOrderMapper seckillOrderMapper;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private IdWorker idWorker;
    @Autowired
    private TbSeckillGoodsMapper seckillGoodsMapper;
    @Autowired
    private RedisLock redisLock;

    /**
     * 查询全部
     */
    @Override
    public List<TbSeckillOrder> findAll() {
        return seckillOrderMapper.selectByExample(null);
    }

    /**
     * 按分页查询
     */
    @Override
    public PageResult findPage(int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        Page<TbSeckillOrder> page = (Page<TbSeckillOrder>) seckillOrderMapper.selectByExample(null);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 增加
     */
    @Override
    public void add(TbSeckillOrder seckillOrder) {
        seckillOrderMapper.insert(seckillOrder);
    }


    /**
     * 修改
     */
    @Override
    public void update(TbSeckillOrder seckillOrder) {
        seckillOrderMapper.updateByPrimaryKey(seckillOrder);
    }

    /**
     * 根据ID获取实体
     *
     * @param id
     * @return
     */
    @Override
    public TbSeckillOrder findOne(Long id) {
        return seckillOrderMapper.selectByPrimaryKey(id);
    }

    /**
     * 批量删除
     */
    @Override
    public void delete(Long[] ids) {
        for (Long id : ids) {
            seckillOrderMapper.deleteByPrimaryKey(id);
        }
    }


    @Override
    public PageResult findPage(TbSeckillOrder seckillOrder, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);

        TbSeckillOrderExample example = new TbSeckillOrderExample();
        Criteria criteria = example.createCriteria();

        if (seckillOrder != null) {
            if (seckillOrder.getUserId() != null && seckillOrder.getUserId().length() > 0) {
                criteria.andUserIdLike("%" + seckillOrder.getUserId() + "%");
            }
            if (seckillOrder.getSellerId() != null && seckillOrder.getSellerId().length() > 0) {
                criteria.andSellerIdLike("%" + seckillOrder.getSellerId() + "%");
            }
            if (seckillOrder.getStatus() != null && seckillOrder.getStatus().length() > 0) {
                criteria.andStatusLike("%" + seckillOrder.getStatus() + "%");
            }
            if (seckillOrder.getReceiverAddress() != null && seckillOrder.getReceiverAddress().length() > 0) {
                criteria.andReceiverAddressLike("%" + seckillOrder.getReceiverAddress() + "%");
            }
            if (seckillOrder.getReceiverMobile() != null && seckillOrder.getReceiverMobile().length() > 0) {
                criteria.andReceiverMobileLike("%" + seckillOrder.getReceiverMobile() + "%");
            }
            if (seckillOrder.getReceiver() != null && seckillOrder.getReceiver().length() > 0) {
                criteria.andReceiverLike("%" + seckillOrder.getReceiver() + "%");
            }
            if (seckillOrder.getTransactionId() != null && seckillOrder.getTransactionId().length() > 0) {
                criteria.andTransactionIdLike("%" + seckillOrder.getTransactionId() + "%");
            }
        }

        Page<TbSeckillOrder> page = (Page<TbSeckillOrder>) seckillOrderMapper.selectByExample(example);
        return new PageResult(page.getTotal(), page.getResult());
    }

    @Override
    public TbSeckillOrder searchOrderFromRedisByUserId(String userId) {
        return (TbSeckillOrder) redisTemplate.boundHashOps("seckillOrder").get(userId);
    }

    @Override
    public void deleteOrderFromRedis(String userId, Long orderId) {
        TbSeckillOrder seckillOrder = (TbSeckillOrder) redisTemplate.boundHashOps("seckillOrder").get(userId);
        if (null != seckillOrder && seckillOrder.getId().longValue() == orderId.longValue()) {
            redisTemplate.boundHashOps("seckillOrder").delete(userId);
            TbSeckillGoods seckillGoods = (TbSeckillGoods) redisTemplate.boundHashOps("seckillGoods").get(seckillOrder.getSeckillId());
            if (null != seckillGoods) {
                seckillGoods.setStockCount(seckillGoods.getStockCount() + 1);
                redisTemplate.boundHashOps("seckillGoods").put(seckillOrder.getSeckillId(), seckillGoods);
            }else {
                seckillGoods = seckillGoodsMapper.selectByPrimaryKey(seckillOrder.getSeckillId());
                seckillGoods.setStockCount(seckillGoods.getStockCount() + 1);
                seckillGoodsMapper.updateByPrimaryKey(seckillGoods);
                redisTemplate.boundHashOps("seckillGoods").put(seckillGoods.getId(),seckillGoods);
            }
        }
    }

    @Override
    public void submitOrder(Long seckillId, String userId) {
        String appId = "createOrderLock";
        //设置超时时间一秒钟
        long ex = 1 * 1000L;
        String value = String.valueOf(System.currentTimeMillis() + ex);

        boolean lock = redisLock.lock(appId, value);
        if (lock) {
            TbSeckillGoods seckillGoods = (TbSeckillGoods) redisTemplate.boundHashOps("seckillGoods").get(seckillId);
            if (seckillGoods == null) {
                //throw new RuntimeException("不存在");
                System.out.println("不存在");
                return;
            }
            if (seckillGoods.getStockCount() <= 0) {
                //throw new RuntimeException("卖完了");
                System.out.println("卖完了");
                return;
            }
            seckillGoods.setStockCount(seckillGoods.getStockCount() - 1);
            redisTemplate.boundHashOps("seckillGoods").put(seckillId, seckillGoods);
            TbSeckillOrder seckillOrder = new TbSeckillOrder();
            seckillOrder.setId(idWorker.nextId());//订单ID
            seckillOrder.setSeckillId(seckillId);//秒杀商品ID
            seckillOrder.setCreateTime(new Date());//创建时间
            seckillOrder.setMoney(seckillGoods.getCostPrice());//秒杀价格
            seckillOrder.setSellerId(seckillGoods.getSellerId());//商家ID
            seckillOrder.setUserId(userId);//用户ID
            seckillOrder.setStatus("0");//未支付
            redisTemplate.boundHashOps("seckillOrder").put(userId, seckillOrder);
            //判断商品库存如果库存为零将库存同步到数据库删除该商品在秒杀商品中的缓存
            if (seckillGoods.getStockCount() <= 0) {
                seckillGoodsMapper.updateByPrimaryKey(seckillGoods);
                redisTemplate.boundHashOps("seckillGoods").delete(seckillId);
            }
            //程序执行完毕后，解锁
            redisLock.unlock(appId, value);
        }
    }

    @Override
    public void saveOrderFromRedisToDb(String userId, long orderId, String transactionId) {
        TbSeckillOrder seckillOrder = (TbSeckillOrder) redisTemplate.boundHashOps("seckillOrder").get(userId);
        if (seckillOrder == null) {
            throw new RuntimeException("该订单不存在");
        }
        if (seckillOrder.getId().longValue() != orderId) {
            throw new RuntimeException("订单不相符");
        }
        seckillOrder.setPayTime(new Date());
        seckillOrder.setStatus("1");
        seckillOrder.setTransactionId(transactionId); //交易流水号
        seckillOrderMapper.insert(seckillOrder);
        redisTemplate.boundHashOps("seckillOrder").delete(userId);
    }
}
