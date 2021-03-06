package com.pinyougou.pay.service.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Value;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.wxpay.sdk.WXPay;
import com.github.wxpay.sdk.WXPayUtil;
import com.pinyougou.pay.service.WeixinPayService;

import util.HttpClient;

@Service
public class WeixinPayServiceImpl implements WeixinPayService {

	@Value("${appid}")
	private String appid;

	@Value("${partner}")
	private String partner;

	@Value("${partnerkey}")
	private String partnerkey;

	@Override
	public Map createNative(String out_trade_no, String total_fee) {
		// 参数封装
		Map param = new ConcurrentHashMap();
		param.put("appid", appid); // 公众帐号id
		param.put("mch_id", partner);
		param.put("nonce_str", WXPayUtil.generateNonceStr()); // 随机字符串
		param.put("body", "pinyougou");
		param.put("out_trade_no", out_trade_no); // 订单号
		param.put("total_fee", total_fee); // 金额(分)
		param.put("spbill_create_ip", "127.0.0.1");
		param.put("notify_url", "http://www.itcast.cn");
		param.put("trade_type", "NATIVE");

		try {
			String xmlParam = WXPayUtil.generateSignedXml(param, partnerkey);
			System.out.println("请求的参数: " + xmlParam);
			// 发送请求
			HttpClient httpClient = new HttpClient("https://api.mch.weixin.qq.com/pay/unifiedorder");
			httpClient.setHttps(true);
			httpClient.setXmlParam(xmlParam);
			httpClient.post();
			// 获取结果
			String xmlResult = httpClient.getContent();
			Map<String, String> mapResult = WXPayUtil.xmlToMap(xmlResult);
			System.out.println("微信返回的结果: " + mapResult);
			Map map = new ConcurrentHashMap<>();
			map.put("code_url", mapResult.get("code_url")); // 生成支付二维码的连接地址
			map.put("out_trade_no", out_trade_no);
			map.put("total_fee", total_fee);
			return map;
		} catch (Exception e) {
			e.printStackTrace();
			return new HashMap();
		}

	}

	@Override
	public Map queryPayStatus(String out_trade_no) {

		// 封装参数
		Map param = new ConcurrentHashMap<>();
		param.put("appid", appid);
		param.put("mch_id", partner);
		param.put("out_trade_no", out_trade_no);
		param.put("nonce_str", WXPayUtil.generateNonceStr());
		try {
			String xmlParam = WXPayUtil.generateSignedXml(param, partnerkey);

			// 发送请求
			HttpClient httpClient = new HttpClient("https//api.mch.weixin.qq.com/pay/orderquery");
			httpClient.setHttps(true);
			httpClient.setXmlParam(xmlParam);
			httpClient.post();

			// 获取结果
			String xmlResult = httpClient.getContent();
			Map<String, String> map = WXPayUtil.xmlToMap(xmlResult);
			System.out.println("调用查询api返回结果: " + map);
			return map;
		} catch (Exception e) {
			e.printStackTrace();
			return new HashMap<>();
		}

	}

	@Override
	public Map closePay(String out_trade_no) {
		// 封装参数
		Map param = new ConcurrentHashMap<>();
		param.put("appid", appid);
		param.put("mch_id", partner);
		param.put("out_trade_no", out_trade_no);
		param.put("nonce_str", WXPayUtil.generateNonceStr());
		try {
			String xmlParam = WXPayUtil.generateSignedXml(param, partnerkey);

			// 发送请求
			HttpClient httpClient = new HttpClient("https//api.mch.weixin.qq.com/pay/closeorder");
			httpClient.setHttps(true);
			httpClient.setXmlParam(xmlParam);
			httpClient.post();

			// 获取结果
			String xmlResult = httpClient.getContent();
			Map<String, String> map = WXPayUtil.xmlToMap(xmlResult);
			System.out.println("调用查询api返回结果: " + map);
			return map;
		} catch (Exception e) {
			e.printStackTrace();
			return new HashMap<>();
		}
	}

}
