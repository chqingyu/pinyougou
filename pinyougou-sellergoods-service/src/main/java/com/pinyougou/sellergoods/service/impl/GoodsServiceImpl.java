package com.pinyougou.sellergoods.service.impl;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbGoodsDescMapper;
import com.pinyougou.mapper.TbGoodsMapper;
import com.pinyougou.mapper.TbItemCatMapper;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.mapper.TbSellerMapper;
import com.pinyougou.pojo.TbGoods;
import com.pinyougou.pojo.TbGoodsDesc;
import com.pinyougou.pojo.TbGoodsExample;
import com.pinyougou.pojo.TbGoodsExample.Criteria;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbItemCat;
import com.pinyougou.pojo.TbItemExample;
import com.pinyougou.pojo.TbSeller;
import com.pinyougou.pojogroup.Goods;
import com.pinyougou.sellergoods.service.GoodsService;

import entity.PageResult;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Transactional
@Service
public class GoodsServiceImpl implements GoodsService {

	@Autowired
	private TbGoodsMapper goodsMapper;
	
	@Autowired
	private TbGoodsDescMapper goodsDescMapper;
	
	@Autowired
	private TbItemMapper itemMapper;
	
	@Autowired
	private TbItemCatMapper itemCatMapper;
	
	@Autowired
	private TbSellerMapper sellerMapper;
	
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
		Page<TbGoods> page=   (Page<TbGoods>) goodsMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}


	
	/**
	 * 增加
	 */
	@Override
	public void add(Goods goods) {
		
		goods.getGoods().setAuditStatus("0");
		goodsMapper.insert(goods.getGoods());		
		
		goods.getGoodsDesc().setGoodsId(goods.getGoods().getId());
		goodsDescMapper.insert(goods.getGoodsDesc());
		
		this.saveItemList(goods); // 保存sku
	}

	private void saveItemList(Goods goods){
		
		// 如果开启了规格功能
		if("1".equals(goods.getGoods().getIsEnableSpec())){
			
			for(TbItem item : goods.getItemList()){
				String title = goods.getGoods().getGoodsName();
				Map<String,Object> map = JSON.parseObject(item.getSpec());
				for(String key:map.keySet()){
					title += ""+map.get(key);
				}
				item.setTitle(title);
				this.setItemValues(item,goods);
				itemMapper.insert(item);
			}
		}else{
			TbItem item = new TbItem();
			item.setTitle(goods.getGoods().getGoodsName());
			item.setPrice(goods.getGoods().getPrice());
			item.setNum(9999);
			item.setStatus("1");
			item.setIsDefault("1");
			item.setSpec("{}");
			
			this.setItemValues(item,goods);
			itemMapper.insert(item);
		}
	}
	
	private void setItemValues(TbItem item,Goods goods){
		
		item.setCategoryid(goods.getGoods().getCategory3Id());
		item.setCreateTime(new Date());
		item.setUpdateTime(new Date());
		
		item.setGoodsId(goods.getGoods().getId());
		item.setSellerId(goods.getGoods().getSellerId());
		
		TbItemCat itemCat = itemCatMapper.selectByPrimaryKey(goods.getGoods().getCategory3Id());
		item.setCategory(itemCat.getName());
		
		TbSeller seller = sellerMapper.selectByPrimaryKey(goods.getGoods().getSellerId());
		item.setSeller(seller.getNickName());
		
		List<Map> imageList = JSON.parseArray(goods.getGoodsDesc().getItemImages(),Map.class);
		if(imageList.size()>0){
			item.setImage((String)imageList.get(0).get("url"));
		}
		
	}
	
	/**
	 * 修改
	 */
	@Override
	public void update(Goods goods){
		
		goodsMapper.updateByPrimaryKey(goods.getGoods());
		goodsDescMapper.updateByPrimaryKey(goods.getGoodsDesc());
		
		TbItemExample example = new TbItemExample();
		com.pinyougou.pojo.TbItemExample.Criteria criteria = example.createCriteria();
		
		// 先删除这个spu下的sku
		criteria.andGoodsIdEqualTo(goods.getGoods().getId());
		itemMapper.deleteByExample(example);
		
		saveItemList(goods);
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public Goods findOne(Long id){
		
		Goods goods = new Goods();
		
		// SPU表
		TbGoods tbGoods = goodsMapper.selectByPrimaryKey(id);
		goods.setGoods(tbGoods);
		
		// SPU描述表
		TbGoodsDesc goodsDesc = goodsDescMapper.selectByPrimaryKey(id);
		goods.setGoodsDesc(goodsDesc);
		
		TbItemExample example = new TbItemExample();
		com.pinyougou.pojo.TbItemExample.Criteria criteria = example.createCriteria();
		criteria.andGoodsIdEqualTo(id);
		
		// SPU的sku集合
		List<TbItem> itemList = itemMapper.selectByExample(example);
		goods.setItemList(itemList);
		
		return goods;
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			TbGoods goods = goodsMapper.selectByPrimaryKey(id);
			goods.setIsDelete("1");
			goodsMapper.updateByPrimaryKey(goods);
		}		
	}
	
	
		@Override
	public PageResult findPage(TbGoods goods, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbGoodsExample example=new TbGoodsExample();
		Criteria criteria = example.createCriteria();
		criteria.andIsDeleteIsNull(); // 指定条件为为逻辑删除的记录
		if(goods!=null){			
						if(goods.getSellerId()!=null && goods.getSellerId().length()>0){
//				criteria.andSellerIdLike("%"+goods.getSellerId()+"%");
							criteria.andSellerIdEqualTo(goods.getSellerId());
			}
			if(goods.getGoodsName()!=null && goods.getGoodsName().length()>0){
				criteria.andGoodsNameLike("%"+goods.getGoodsName()+"%");
			}
			if(goods.getAuditStatus()!=null && goods.getAuditStatus().length()>0){
				criteria.andAuditStatusLike("%"+goods.getAuditStatus()+"%");
			}
			if(goods.getIsMarketable()!=null && goods.getIsMarketable().length()>0){
				criteria.andIsMarketableLike("%"+goods.getIsMarketable()+"%");
			}
			if(goods.getCaption()!=null && goods.getCaption().length()>0){
				criteria.andCaptionLike("%"+goods.getCaption()+"%");
			}
			if(goods.getSmallPic()!=null && goods.getSmallPic().length()>0){
				criteria.andSmallPicLike("%"+goods.getSmallPic()+"%");
			}
			if(goods.getIsEnableSpec()!=null && goods.getIsEnableSpec().length()>0){
				criteria.andIsEnableSpecLike("%"+goods.getIsEnableSpec()+"%");
			}
			if(goods.getIsDelete()!=null && goods.getIsDelete().length()>0){
				criteria.andIsDeleteLike("%"+goods.getIsDelete()+"%");
			}
	
		}
		
		Page<TbGoods> page= (Page<TbGoods>)goodsMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}

		@Override
		public void updateStatus(Long[] ids, String status) {
			
			for (Long id : ids) {
				TbGoods goods = goodsMapper.selectByPrimaryKey(id);
				goods.setAuditStatus(status);
				goodsMapper.updateByPrimaryKey(goods);
			}
		}
		
		
		/**
		 * 根据SPU的id集合,查询sku
		 * @param goodsIds
		 * @param status
		 * @return
		 */
		public List<TbItem> findItemListByGoodsIdListAndStatus(Long[] goodsIds,String status){
			
			TbItemExample example = new TbItemExample();
			com.pinyougou.pojo.TbItemExample.Criteria criteria = example.createCriteria();
			criteria.andStatusEqualTo(status); // 已审核
			criteria.andGoodsIdIn(Arrays.asList(goodsIds)); // 指定条件:SPU的id集合
			List<TbItem> itemList = itemMapper.selectByExample(example);
			return itemList;
		}
	
}
