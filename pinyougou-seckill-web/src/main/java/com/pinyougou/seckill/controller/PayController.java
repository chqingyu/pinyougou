package com.pinyougou.seckill.controller;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pay.service.WeixinPayService;
import com.pinyougou.pojo.TbSeckillOrder;
import com.pinyougou.seckill.service.SeckillOrderService;

import entity.Result;

@RequestMapping("/pay")
@RestController
public class PayController {

	@Reference
	private WeixinPayService weixinPayService;
	
	@Reference
	private SeckillOrderService seckillOrderService;
	
	@RequestMapping("/createNative")
	public Map createNative(){
		
		// 获取当前的用户
		String userId = SecurityContextHolder.getContext().getAuthentication().getName();
		
		// 从缓存中提取秒杀订单
		TbSeckillOrder seckillOrder = seckillOrderService.searchOrderFromRedisByUserId(userId);
		
		// 调用微信支付接口
		if(seckillOrder != null){
			
			return weixinPayService.createNative(seckillOrder.getId()+"", seckillOrder.getMoney().multiply(new BigDecimal("100")).longValue()+"");
		}else{
			return new HashMap<>();
		}
		
	}
	
	@RequestMapping("/queryPayStatus")
	public Result queryPayStatus(String out_trade_no){
		
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		Result result = null;
		int x = 0;
		while(true){
			
			// 调用查询
			Map<String,String> map = weixinPayService.queryPayStatus(out_trade_no);
			
			if(map == null){
				result = new Result(false,"支付发生错误");
				break;
			}
			
			if(map.get("trade_state").equals("success")){ // 支付成功
				
				result = new Result(true,"支付成功");
				seckillOrderService.saveOrderFromRedisToDB(username, Long.parseLong(out_trade_no), map.get("transaction_id"));
				break;
			}
			
			try {
				// 停3s
				TimeUnit.SECONDS.sleep(3);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			x++;
			if(x >= 100){
				
				result = new Result(false,"二维码超时");
				
				// 关闭支付,关闭支付的时候可能用户已经支付了
				Map<String,String> payResult = weixinPayService.closePay(out_trade_no);
				if(payResult!=null && "FAIL".equals(payResult.get("return_code"))){
					if("ORDERPAID".equals(payResult.get("err_code"))){
						result = new Result(true,"支付成功");
						seckillOrderService.saveOrderFromRedisToDB(username, Long.parseLong(out_trade_no), map.get("transaction_id"));
					}
				}
				
				// 订单超时
				if(result.isSuccess()==false){
					
					// 删除缓存中生成的订单
					seckillOrderService.deleteOrderFromRedis(username, Long.parseLong(out_trade_no));
				}
				
			
				
				break;
			}
		}
		
		return result;
	}
}
