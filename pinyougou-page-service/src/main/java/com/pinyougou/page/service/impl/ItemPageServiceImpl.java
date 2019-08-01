package com.pinyougou.page.service.impl;

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import com.pinyougou.mapper.TbGoodsDescMapper;
import com.pinyougou.mapper.TbGoodsMapper;
import com.pinyougou.mapper.TbItemCatMapper;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.page.service.ItemPageService;
import com.pinyougou.pojo.TbGoods;
import com.pinyougou.pojo.TbGoodsDesc;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbItemExample;
import com.pinyougou.pojo.TbItemExample.Criteria;

import freemarker.template.Configuration;
import freemarker.template.Template;

@Service
public class ItemPageServiceImpl implements ItemPageService{

	@Autowired
	private FreeMarkerConfigurer freeMarkerConfigurer;
	
	@Value("${pagedir}")
	private String pagedir;
	
	@Autowired
	private TbGoodsMapper goodsMapper;
	
	@Autowired
	private TbGoodsDescMapper goodsDescMapper;
	
	@Autowired
	private TbItemCatMapper tbItemCatMapper;
	
	@Autowired
	private TbItemMapper tbItemMapper;
	
	@Override
	public boolean genItemHtml(Long goodsId) {
		
		Configuration configuration = freeMarkerConfigurer.getConfiguration();
		try {
			Template template = configuration.getTemplate("item.ftl");
			
			// 创建数据模型
			Map dataModel = new HashMap<>();
			
			// 查询一个SPU
			TbGoods goods = goodsMapper.selectByPrimaryKey(goodsId);
			dataModel.put("goods", goods);
			// 查询一个SPU的描述
			TbGoodsDesc goodsDesc = goodsDescMapper.selectByPrimaryKey(goodsId);
			dataModel.put("goodsDesc", goodsDesc);
			
			// 查询这个SPU的三个分类名称
			String itemCat1 = tbItemCatMapper.selectByPrimaryKey(goods.getCategory1Id()).getName();
			String itemCat2 = tbItemCatMapper.selectByPrimaryKey(goods.getCategory2Id()).getName();
			String itemCat3 = tbItemCatMapper.selectByPrimaryKey(goods.getCategory3Id()).getName();
			dataModel.put("itemCat1", itemCat1);
			dataModel.put("itemCat2", itemCat2);
			dataModel.put("itemCat3", itemCat3);
			
			// 查询这个SPU的SKU集合
			TbItemExample example = new TbItemExample();
			Criteria criteria = example.createCriteria();
			criteria.andGoodsIdEqualTo(goodsId);
			criteria.andStatusEqualTo("1"); // 以上架的SKU
			List<TbItem> itemList = tbItemMapper.selectByExample(example);
			example.setOrderByClause("is_default desc"); // 按是否默认字段进行排序,目的是返回的结果第一条就是默认sku
			dataModel.put("itemList", itemList);
			
			Writer out = new FileWriter(pagedir+goodsId+".html");
			template.process(dataModel, out);
			out.close();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} 
		
	}

	@Override
	public boolean deleteItemHtml(Long[] goodsIds) {
		try {
			for (Long goodsId : goodsIds) {
				
				new File(pagedir+goodsId+".html").delete();
			}
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

}
