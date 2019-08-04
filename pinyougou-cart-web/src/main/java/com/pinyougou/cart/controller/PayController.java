package com.pinyougou.cart.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.order.service.OrderService;
import com.pinyougou.pay.service.WeixinPayService;
import com.pinyougou.pojo.TbPayLog;

import entity.Result;

@RequestMapping("/pay")
@RestController
public class PayController {

	@Reference
	private WeixinPayService weixinPayService;
	
	@Reference
	private OrderService orderService;
	
	@RequestMapping("/createNative")
	public Map createNative(){
		
		// 获取当前的用户
		String userId = SecurityContextHolder.getContext().getAuthentication().getName();
		
		// 从缓存中提取支付日志
		TbPayLog payLog = orderService.searchPayLogFromRedis(userId);
		
		// 调用微信支付接口
		if(payLog != null){
			
			return weixinPayService.createNative(payLog.getOutTradeNo(), payLog.getTotalFee()+"");
		}else{
			return new HashMap<>();
		}
		
	}
	
	@RequestMapping("/queryPayStatus")
	public Result queryPayStatus(String out_trade_no){
		
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
				orderService.updateOrderStatus(out_trade_no, map.get("transaction_id"));
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
				break;
			}
		}
		
		return result;
	}
}
