package com.pinyougou.cart.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pay.service.WeixinPayService;

import util.IdWorker;

@RequestMapping("/pay")
@RestController
public class PayController {

	@Reference
	private WeixinPayService weixinPayService;
	
	@RequestMapping("/createNative")
	public Map createNative(){
		
		IdWorker idWorker = new IdWorker();
		
		return weixinPayService.createNative(String.valueOf(idWorker.nextId()), "1");
	}
}
