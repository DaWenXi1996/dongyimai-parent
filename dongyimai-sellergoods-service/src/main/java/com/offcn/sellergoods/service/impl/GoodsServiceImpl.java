package com.offcn.sellergoods.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.offcn.entity.PageResult;
import com.offcn.group.Goods;
import com.offcn.mapper.*;
import com.offcn.pojo.*;
import com.offcn.pojo.TbGoodsExample.Criteria;
import com.offcn.sellergoods.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 服务实现层
 *
 * @author Administrator
 */
@Service
public class GoodsServiceImpl implements GoodsService {

    @Autowired
    private TbGoodsMapper goodsMapper;
    @Autowired
    private TbGoodsDescMapper goodsDescMapper;
    @Autowired
    private TbItemCatMapper itemCatMapper;
    @Autowired
    private TbBrandMapper tbBrandMapper;
    @Autowired
    private TbSellerMapper sellerMapper;
    @Autowired
    private TbItemMapper itemMapper;

    /**
     * 查询全部
     */
    @Override
    public List<TbGoods> findAll() {
        return goodsMapper.selectByExample(null);
    }

    /**
     * 按分页查询
     */
    @Override
    public PageResult findPage(int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        Page<TbGoods> page = (Page<TbGoods>) goodsMapper.selectByExample(null);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 增加
     */
//	@Override
//	public void add(TbGoods goods) {
//		goodsMapper.insert(goods);
//	}
    @Override
    public void add(Goods goods) {
        goods.getGoods().setAuditStatus("0");  //审核状态 未审核
        goodsMapper.insert(goods.getGoods());
        goods.getGoodsDesc().setGoodsId(goods.getGoods().getId());
        goodsDescMapper.insert(goods.getGoodsDesc());
        this.saveItemList(goods);
    }

    //保存SKU信息
    private void saveItemList(Goods goods) {
        if ("1".equals(goods.getGoods().getIsEnableSpec())) {
            //5.保存SKU 商品详情信息
            if (!CollectionUtils.isEmpty(goods.getItemList())) {
                for (TbItem item : goods.getItemList()) {
                    String title = goods.getGoods().getGoodsName();
                    //设置SKU的名称  商品名+规格选项
                    Map<String, String> specMap = JSON.parseObject(item.getSpec(), Map.class);    //取得SKU的规格选项，并做JSON类型转换
                    for (String key : specMap.keySet()) {
                        title += specMap.get(key) + " ";
                    }
                    item.setTitle(title);                                       //SKU名称
                    this.setItemValue(goods, item);
                    //保存SKU信息
                    itemMapper.insert(item);

                }
            }
        } else {
            //不启用规格  SKU信息为默认值
            TbItem item = new TbItem();
            item.setTitle(goods.getGoods().getGoodsName());     //商品名称
            item.setPrice(goods.getGoods().getPrice());      //默认使用SPU的价格
            item.setNum(9999);
            item.setStatus("0");            //是否启用
            item.setIsDefault("1");         //是否默认
            item.setSpec("{}");             //没有选择规格，则放置空JSON结构
            this.setItemValue(goods, item);
            itemMapper.insert(item);

        }
    }

    private void setItemValue(Goods goods, TbItem item) {
        item.setCategoryid(goods.getGoods().getCategory3Id());     //商品分类 三级
        item.setCreateTime(new Date());                             //创建时间
        item.setUpdateTime(new Date());                             //更新时间
        item.setGoodsId(goods.getGoods().getId());                   //SPU ID
        item.setSellerId(goods.getGoods().getSellerId());           //商家ID
        //查询分类对象
        TbItemCat itemCat = itemCatMapper.selectByPrimaryKey(goods.getGoods().getCategory3Id());
        item.setCategory(itemCat.getName());                            //分类名称
        //查询品牌对象
        TbBrand tbBrand = tbBrandMapper.selectByPrimaryKey(goods.getGoods().getBrandId());
        item.setBrand(tbBrand.getName());                               //品牌名称
        //查询商家对象
        TbSeller tbSeller = sellerMapper.selectByPrimaryKey(goods.getGoods().getSellerId());
        item.setSeller(tbSeller.getNickName());                         //商家名称
        List<Map> imageList = JSON.parseArray(goods.getGoodsDesc().getItemImages(), Map.class);
        if (imageList.size() > 0) {
            item.setImage((String) imageList.get(0).get("url"));           //商品图片
        }

    }

    /**
     * 修改
     */
    @Override
    public void update(Goods goods) {
        goods.getGoods().setAuditStatus("0");
        goodsMapper.updateByPrimaryKey(goods.getGoods());
        goodsDescMapper.updateByPrimaryKey(goods.getGoodsDesc());
        TbItemExample tbItemExample = new TbItemExample();
        TbItemExample.Criteria criteria = tbItemExample.createCriteria();
        criteria.andGoodsIdEqualTo(goods.getGoods().getId());
        itemMapper.deleteByExample(tbItemExample);
        this.saveItemList(goods);
    }

    /**
     * 根据ID获取实体
     *
     * @param id
     * @return
     */
    @Override
    public Goods findOne(Long id) {
        TbGoods tbGoods = goodsMapper.selectByPrimaryKey(id);
        //2.根据ID查询商品扩展信息
        TbGoodsDesc tbGoodsDesc = goodsDescMapper.selectByPrimaryKey(id);
        //3.根据ID查询SKU列表
        TbItemExample tbItemExample = new TbItemExample();
        TbItemExample.Criteria criteria = tbItemExample.createCriteria();
        criteria.andGoodsIdEqualTo(id);
        List<TbItem> itemList = itemMapper.selectByExample(tbItemExample);

        //4.设置复合实体对象
        Goods goods = new Goods();
        goods.setGoods(tbGoods);
        goods.setGoodsDesc(tbGoodsDesc);
        goods.setItemList(itemList);
        return goods;
    }

    /**
     * 批量删除
     */
    @Override
    public void delete(Long[] ids) {
        for (Long id : ids) {
            TbGoods tbGoods = goodsMapper.selectByPrimaryKey(id);
            tbGoods.setIsDelete("1");
            goodsMapper.updateByPrimaryKey(tbGoods);
        }
        List<TbItem> itemList = this.findItemListByGoodsIdAndStatus(ids, "1");
        for (TbItem item : itemList) {
            item.setStatus("0");    //修改SKU状态为禁用
            itemMapper.updateByPrimaryKey(item);
        }
    }


    @Override
    public PageResult findPage(TbGoods goods, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);

        TbGoodsExample example = new TbGoodsExample();
        Criteria criteria = example.createCriteria();

        if (goods != null) {
            if (goods.getSellerId() != null && goods.getSellerId().length() > 0) {
//				criteria.andSellerIdLike("%"+goods.getSellerId()+"%");
                criteria.andSellerIdEqualTo(goods.getSellerId());
            }
            if (goods.getGoodsName() != null && goods.getGoodsName().length() > 0) {
                criteria.andGoodsNameLike("%" + goods.getGoodsName() + "%");
            }
            if (goods.getAuditStatus() != null && goods.getAuditStatus().length() > 0) {
                criteria.andAuditStatusLike("%" + goods.getAuditStatus() + "%");
            }
            if (goods.getIsMarketable() != null && goods.getIsMarketable().length() > 0) {
                criteria.andIsMarketableLike("%" + goods.getIsMarketable() + "%");
            }
            if (goods.getCaption() != null && goods.getCaption().length() > 0) {
                criteria.andCaptionLike("%" + goods.getCaption() + "%");
            }
            if (goods.getSmallPic() != null && goods.getSmallPic().length() > 0) {
                criteria.andSmallPicLike("%" + goods.getSmallPic() + "%");
            }
            if (goods.getIsEnableSpec() != null && goods.getIsEnableSpec().length() > 0) {
                criteria.andIsEnableSpecLike("%" + goods.getIsEnableSpec() + "%");
            }
//            if (goods.getIsDelete() != null && goods.getIsDelete().length() > 0) {
//                criteria.andIsDeleteLike("%" + goods.getIsDelete() + "%");
//            }
            criteria.andIsDeleteIsNull();
        }

        Page<TbGoods> page = (Page<TbGoods>) goodsMapper.selectByExample(example);
        return new PageResult(page.getTotal(), page.getResult());
    }

    @Override
    public void updateStatus(Long[] ids, String status) {
        for (Long id : ids) {
            TbGoods tbGoods = goodsMapper.selectByPrimaryKey(id);
            tbGoods.setAuditStatus(status);
            //1.修改SPU商品信息的状态
            goodsMapper.updateByPrimaryKey(tbGoods);
            //2.修改SKU的状态
            TbItemExample tbItemExample = new TbItemExample();
            TbItemExample.Criteria criteria = tbItemExample.createCriteria();
            criteria.andGoodsIdEqualTo(id);
            List<TbItem> itemList = itemMapper.selectByExample(tbItemExample);
            if(!CollectionUtils.isEmpty(itemList)){
                for(TbItem item:itemList){
                    item.setStatus(status);
                    itemMapper.updateByPrimaryKey(item);
                }
            }
        }
    }

    @Override
    public List<TbItem> findItemListByGoodsIdAndStatus(Long[] goodsIds, String status) {
        TbItemExample tbItemExample = new TbItemExample();
        TbItemExample.Criteria criteria = tbItemExample.createCriteria();
        criteria.andStatusEqualTo(status);
        criteria.andGoodsIdIn(Arrays.asList(goodsIds));
        return itemMapper.selectByExample(tbItemExample);
    }
}
