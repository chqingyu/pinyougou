package com.pinyougou.order.service.impl;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbOrderItemMapper;
import com.pinyougou.mapper.TbOrderMapper;
import com.pinyougou.mapper.TbPayLogMapper;
import com.pinyougou.order.service.OrderService;
import com.pinyougou.pojo.TbOrder;
import com.pinyougou.pojo.TbOrderExample;
import com.pinyougou.pojo.TbOrderExample.Criteria;
import com.pinyougou.pojo.TbOrderItem;
import com.pinyougou.pojo.TbPayLog;
import com.pinyougou.pojogroup.Cart;

import entity.PageResult;
import util.IdWorker;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Transactional
@Service
public class OrderServiceImpl implements OrderService {

	@Autowired
	private TbOrderMapper orderMapper;
	
	@Autowired
	private RedisTemplate redisTemplate;
	
	@Autowired
	private IdWorker idWorker;
	
	
	@Autowired
	private TbPayLogMapper payLogMapper;
	
	
	@Autowired
	private TbOrderItemMapper orderItemMapper;
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
		
		// 从redis中提取购物车列表
		List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps("cartList").get(order.getUserId());
		
		List<String> orderIdList = new CopyOnWriteArrayList<>();
		BigDecimal toto_money = new BigDecimal("0");
		// 循环购物车列表添加订单
		for(Cart cart:cartList){
			TbOrder tbOrder = new TbOrder();
			long orderId = idWorker.nextId(); // 获取id
			tbOrder.setOrderId(orderId);
			tbOrder.setPaymentType(order.getPaymentType()); // 支付类型
			tbOrder.setCreateTime(new Date());
			tbOrder.setUpdateTime(new Date());
			tbOrder.setUserId(order.getUserId());
			tbOrder.setReceiverAreaName(order.getReceiverAreaName()); // 收货人地址
			tbOrder.setReceiverMobile(order.getReceiverMobile());
			tbOrder.setReceiver(order.getReceiver());
			tbOrder.setSourceType(order.getSourceType());
			tbOrder.setSellerId(cart.getSellerId());
			tbOrder.setStatus("1");
			
			BigDecimal money = new BigDecimal("0");
			
			for(TbOrderItem orderItem:cart.getOrderItemList()){
				orderItem.setId(idWorker.nextId()); // 主键
				orderItem.setOrderId(orderId); // 订单编号
				orderItem.setSellerId(cart.getSellerId()); // 商家id
				orderItemMapper.insert(orderItem);
				money = money.add(orderItem.getTotalFee());
			}
			
			tbOrder.setPayment(money); // 合计金额
			orderMapper.insert(tbOrder);
			orderIdList.add(String.valueOf(orderId));
			toto_money.add(money);
		}
		
		// 添加支付日志
		if("1".equals(order.getPaymentType())){
			TbPayLog payLog = new TbPayLog();
			payLog.setOutTradeNo(String.valueOf(idWorker.nextId())); // 系统的支付订单号
			payLog.setCreateTime(new Date());
			payLog.setUserId(order.getUserId()); // 用户id
			payLog.setOrderList(orderIdList.toString().replace("[", "").replace("]","")); // 订单id
			String string = toto_money.multiply(new BigDecimal("100")).toString();
			payLog.setTotalFee(new Long(string)); // 金额(分)
			payLog.setTradeState("0"); // 交易状态
			payLog.setPayType("1");
			
			payLogMapper.insert(payLog);
			
			redisTemplate.boundHashOps("payLog").put(order.getUserId(), payLog);
		}
		
		// 清除redis中的购物车
		redisTemplate.boundHashOps("cartList").delete(order.getUserId());
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
	public TbOrder findOne(Long id){
		return orderMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			orderMapper.deleteByPrimaryKey(id);
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
			}
			if(order.getPostFee()!=null && order.getPostFee().length()>0){
				criteria.andPostFeeLike("%"+order.getPostFee()+"%");
			}
			if(order.getStatus()!=null && order.getStatus().length()>0){
				criteria.andStatusLike("%"+order.getStatus()+"%");
			}
			if(order.getShippingName()!=null && order.getShippingName().length()>0){
				criteria.andShippingNameLike("%"+order.getShippingName()+"%");
			}
			if(order.getShippingCode()!=null && order.getShippingCode().length()>0){
				criteria.andShippingCodeLike("%"+order.getShippingCode()+"%");
			}
			if(order.getUserId()!=null && order.getUserId().length()>0){
				criteria.andUserIdLike("%"+order.getUserId()+"%");
			}
			if(order.getBuyerMessage()!=null && order.getBuyerMessage().length()>0){
				criteria.andBuyerMessageLike("%"+order.getBuyerMessage()+"%");
			}
			if(order.getBuyerNick()!=null && order.getBuyerNick().length()>0){
				criteria.andBuyerNickLike("%"+order.getBuyerNick()+"%");
			}
			if(order.getBuyerRate()!=null && order.getBuyerRate().length()>0){
				criteria.andBuyerRateLike("%"+order.getBuyerRate()+"%");
			}
			if(order.getReceiverAreaName()!=null && order.getReceiverAreaName().length()>0){
				criteria.andReceiverAreaNameLike("%"+order.getReceiverAreaName()+"%");
			}
			if(order.getReceiverMobile()!=null && order.getReceiverMobile().length()>0){
				criteria.andReceiverMobileLike("%"+order.getReceiverMobile()+"%");
			}
			if(order.getReceiverZipCode()!=null && order.getReceiverZipCode().length()>0){
				criteria.andReceiverZipCodeLike("%"+order.getReceiverZipCode()+"%");
			}
			if(order.getReceiver()!=null && order.getReceiver().length()>0){
				criteria.andReceiverLike("%"+order.getReceiver()+"%");
			}
			if(order.getInvoiceType()!=null && order.getInvoiceType().length()>0){
				criteria.andInvoiceTypeLike("%"+order.getInvoiceType()+"%");
			}
			if(order.getSourceType()!=null && order.getSourceType().length()>0){
				criteria.andSourceTypeLike("%"+order.getSourceType()+"%");
			}
			if(order.getSellerId()!=null && order.getSellerId().length()>0){
				criteria.andSellerIdLike("%"+order.getSellerId()+"%");
			}
	
		}
		
		Page<TbOrder> page= (Page<TbOrder>)orderMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}

		@Override
		public TbPayLog searchPayLogFromRedis(String userId) {
			
			
			return (TbPayLog) redisTemplate.boundHashOps("payLog").get(userId);
		}

		@Override
		public void updateOrderStatus(String out_trade_no, String transaction_id) {
			
			// 修改支付日志的状态及相关字段
			TbPayLog payLog = payLogMapper.selectByPrimaryKey(out_trade_no);
			payLog.setPayTime(new Date());
			payLog.setTradeState("1");
			payLog.setTransactionId(transaction_id);
			
			payLogMapper.updateByPrimaryKey(payLog);
			
			
			
			// 修改订单表的状态
			String orderList = payLog.getOrderList();
			String[] orderIds = orderList.split(",");
			for (String orderId : orderIds) {
				TbOrder order = orderMapper.selectByPrimaryKey(new Long(orderId));
				order.setStatus("2"); // 已支付状态
				order.setPaymentTime(new Date());
				orderMapper.updateByPrimaryKey(order);
			}
			
			// 清除缓存中的payLog
			redisTemplate.boundHashOps("payLog").delete(payLog.getUserId());
		}
	
}
