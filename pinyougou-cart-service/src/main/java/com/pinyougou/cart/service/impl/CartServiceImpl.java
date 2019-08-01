package com.pinyougou.cart.service.impl;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.cart.service.CartService;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbOrderItem;
import com.pinyougou.pojogroup.Cart;

@Service
public class CartServiceImpl implements CartService {

	@Autowired
	private TbItemMapper itemMapper;
	
	@Autowired
	private RedisTemplate redisTemplate;
	
	@Override
	public List<Cart> addGoodsToCartList(List<Cart> cartList, Long itemId, Integer num) {
		
		// 根据SKU的id查询商品sku对象
		TbItem item = itemMapper.selectByPrimaryKey(itemId);
		if(item == null){
			throw new RuntimeException("商品不存在");
		}
		if(!"1".equals(item.getStatus())){
			throw new RuntimeException("商品可能已下架");
		}
		
		// 根据sku对象得到商家id
		String sellerId = item.getSellerId();
		
		// 根据商家id在购物车列表中查询购物车对象
		Cart cart = searchCartBySellerId(cartList,sellerId);
		
		if(cart == null){ // 如果购物车列表中不存在该商家的购物车
			// 创建一个新的购物对象
			cart = new Cart();
			cart.setSellerId(sellerId); // 设置商家id
			cart.setSellerName(item.getSeller()); // 设置商家名称
			List<TbOrderItem> orderItemList = new CopyOnWriteArrayList<>();
			TbOrderItem orderItem = createOrderItem(item,num);
			orderItemList.add(orderItem);
			cart.setOrderItemList(orderItemList);
			
			
			// 将新的购物车对象添加到购物车集合
			cartList.add(cart);
			
		}else{ // 如果购物车集合中存在该商家的购物车
			
			
			// 判断该商品是否在购物车明细集合中已存在
			TbOrderItem orderItem = searchOrderItemByItemId(cart.getOrderItemList(), itemId);
			
			if(orderItem == null){ // 如果不存在,创建新的购物车明细对象,并添加到购物车明细集合
				
				orderItem = createOrderItem(item, num);
				
				cart.getOrderItemList().add(orderItem);
			}else{ // 如果存在,在原有的数量上相加,更改商品明细的价格
				
				orderItem.setNum(orderItem.getNum()+num); // 更改数量
				orderItem.setTotalFee(orderItem.getPrice().multiply(new BigDecimal(String.valueOf(orderItem.getNum())))); // 更改金额
				// 当明细的数量数量小于等于0,移除该明细
				if(orderItem.getNum() <= 0){
					cart.getOrderItemList().remove(orderItem);
				}
				if(cart.getOrderItemList().size() == 0){
					cartList.remove(cart);
				}
			}
					
						
		}
		
		
		return cartList;
	}
	
	/**
	 * 根据SKU的id在购物车明细集合中查找购物车明细
	 * @param orderItemList
	 * @param itemId
	 * @return
	 */
	public TbOrderItem searchOrderItemByItemId(List<TbOrderItem> orderItemList,Long itemId){
		
		for (TbOrderItem orderItem : orderItemList) {
			if(orderItem.getItemId().longValue() == itemId.longValue()){
				return orderItem;
			}
		}
		return null;
	}
	
	/**
	 * 在cartList中查找商家的cart对象
	 * @param cartList
	 * @param sellerId
	 * @return
	 */
	private Cart searchCartBySellerId(List<Cart> cartList,String sellerId){
		for (Cart cart : cartList) {
			if(cart.getSellerId().equals(sellerId)){
				
				return cart;
			}
		}
		return null;
	}

	private TbOrderItem createOrderItem(TbItem item,Integer num){
		
		// 创建一个新的购物车明细对象

		TbOrderItem orderItem = new TbOrderItem();
		orderItem.setGoodsId(item.getGoodsId());
		orderItem.setItemId(item.getId());
		orderItem.setNum(num);
		orderItem.setPicPath(item.getImage());
		orderItem.setPrice(item.getPrice());
		orderItem.setSellerId(item.getSellerId());
		orderItem.setTitle(item.getTitle());
		orderItem.setTotalFee(item.getPrice().multiply(new BigDecimal(String.valueOf(num))));
		
		return orderItem;
	}

	@Override
	public List<Cart> findCartListFromRedis(String username) {
		
		System.out.println("从redis中提取购物车"+username);
		List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps("cartList").get(username);
		
		if(cartList == null){
			cartList = new CopyOnWriteArrayList<>();
		}
		return cartList;
	}

	@Override
	public void saveCartListToRedis(String username, List<Cart> cartList) {
		
		System.out.println("向redis中存入购物车"+username);
		redisTemplate.boundHashOps("cartList").put(username, cartList);
	}

	@Override
	public List<Cart> mergeCartList(List<Cart> cartList1, List<Cart> cartList2) {
		
		// cartList1.addAll(cartList2); 不能简单合并
		
		for(Cart cart:cartList2){
			
			for(TbOrderItem orderItem:cart.getOrderItemList()){
				cartList1 = addGoodsToCartList(cartList1, orderItem.getItemId(), orderItem.getNum());
			}
		}
		
		return cartList1;
	}
}
