package com.offcn.sellergoods.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.offcn.entity.PageResult;
import com.offcn.group.Specification;
import com.offcn.mapper.TbSpecificationMapper;
import com.offcn.mapper.TbSpecificationOptionMapper;
import com.offcn.pojo.TbSpecification;
import com.offcn.pojo.TbSpecificationExample;
import com.offcn.pojo.TbSpecificationExample.Criteria;
import com.offcn.pojo.TbSpecificationOption;
import com.offcn.pojo.TbSpecificationOptionExample;
import com.offcn.sellergoods.service.SpecificationService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
public class SpecificationServiceImpl implements SpecificationService {

	@Autowired
	private TbSpecificationMapper specificationMapper;
	@Autowired
	private TbSpecificationOptionMapper specificationOptionMapper;

	/**
	 * 查询全部
	 */
	@Override
	public List<TbSpecification> findAll() {
		return specificationMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbSpecification> page=   (Page<TbSpecification>) specificationMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
//	@Override
//	public void add(TbSpecification specification) {
//		specificationMapper.insert(specification);
//	}


	@Override
	public void add(Specification specification) {
		specificationMapper.insert(specification.getSpecification());
		if (null != specification.getSpecificationOptionList() && specification.getSpecificationOptionList().size() > 0) {
			for (TbSpecificationOption specificationOption : specification.getSpecificationOptionList()) {
				specificationOption.setSpecId(specification.getSpecification().getId());
				specificationOptionMapper.insert(specificationOption);
			}
		}
	}

	/**
	 * 修改
	 */
//	@Override
//	public void update(TbSpecification specification){
//		specificationMapper.updateByPrimaryKey(specification);
//	}
    @Override
    public void update(Specification specification){
        specificationMapper.updateByPrimaryKey(specification.getSpecification());
        TbSpecificationOptionExample tbSpecificationOptionExample = new TbSpecificationOptionExample();
        TbSpecificationOptionExample.Criteria criteria = tbSpecificationOptionExample.createCriteria();
        criteria.andSpecIdEqualTo(specification.getSpecification().getId());
        specificationOptionMapper.deleteByExample(tbSpecificationOptionExample);
        if (null != specification.getSpecificationOptionList() && specification.getSpecificationOptionList().size() > 0) {
            for (TbSpecificationOption specificationOption : specification.getSpecificationOptionList()) {
                specificationOption.setSpecId(specification.getSpecification().getId());
                specificationOptionMapper.insert(specificationOption);
            }
        }
    }
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
//	@Override
//	public TbSpecification findOne(Long id){
//		return specificationMapper.selectByPrimaryKey(id);
//	}
    @Override
    public Specification findOne(Long id){
        Specification specification = new Specification();
        specification.setSpecification(specificationMapper.selectByPrimaryKey(id));
        TbSpecificationOptionExample tbSpecificationOptionExample = new TbSpecificationOptionExample();
        TbSpecificationOptionExample.Criteria criteria = tbSpecificationOptionExample.createCriteria();
        criteria.andSpecIdEqualTo(id);
        List<TbSpecificationOption> tbSpecificationOptions = specificationOptionMapper.selectByExample(tbSpecificationOptionExample);
        specification.setSpecificationOptionList(tbSpecificationOptions);
        return specification;
    }


    /**
	 * 批量删除
	 */
//	@Override
//	public void delete(Long[] ids) {
//		for(Long id:ids){
//			specificationMapper.deleteByPrimaryKey(id);
//		}
//	}
    @Override
    public void delete(Long[] ids) {
        for(Long id:ids){
            TbSpecificationOptionExample tbSpecificationOptionExample = new TbSpecificationOptionExample();
            TbSpecificationOptionExample.Criteria criteria = tbSpecificationOptionExample.createCriteria();
            criteria.andSpecIdEqualTo(id);
            specificationOptionMapper.deleteByExample(tbSpecificationOptionExample);
            specificationMapper.deleteByPrimaryKey(id);
        }
    }
	
	
		@Override
    public PageResult findPage(TbSpecification specification, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);

        TbSpecificationExample example=new TbSpecificationExample();
        Criteria criteria = example.createCriteria();

        if(specification!=null){
            if(specification.getSpecName()!=null && specification.getSpecName().length()>0){
                criteria.andSpecNameLike("%"+specification.getSpecName()+"%");
            }
        }

        Page<TbSpecification> page= (Page<TbSpecification>)specificationMapper.selectByExample(example);
        return new PageResult(page.getTotal(), page.getResult());
    }

    @Override
    public List<Map> selectOptions() {
        return specificationMapper.selectOptions();
    }
}
