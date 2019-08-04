package com.pinyougou.sellergoods.service;

import java.util.List;
import java.util.Map;

import com.pinyougou.pojo.TbBrand;

import entity.PageResult;

public interface BrandService {

	List<TbBrand> findAll();

	PageResult findPage(int pageNum, int pageSize);

	void add(TbBrand brand);

	void update(TbBrand brand);

	TbBrand findOne(Long id);

	void delete(Long[] ids);

	PageResult findPage(TbBrand brand, int pageNum, int pageSize);

	List<Map> selectOptionList();
	

}
