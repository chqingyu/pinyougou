package com.pinyougou.cart.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.pinyougou.cart.service.CartService;
import com.pinyougou.pojogroup.Cart;

import entity.Result;


@RequestMapping("/cart")
@RestController
public class CartController {

	@Reference(timeout=6000)
	private CartService cartService;
	
	@Autowired
	private HttpServletRequest request;
	
	@Autowired
	private HttpServletResponse response;
	
	// 从cookie中提取购物车
	@RequestMapping("/findCartList")
	public List<Cart> findCartList(){
		
		// 当前登录的帐号
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		System.out.println("当前登录帐号: "+username);
		String cartListString = util.CookieUtil.getCookieValue(request, "cartList", "UTF-8");
		if(StringUtils.isBlank(cartListString)){
			
			cartListString = "[]";
		}
		List<Cart> cartList_cookie = JSON.parseArray(cartListString, Cart.class);
		
		
		if("anonymousUser".equals(username)){ // 如果未登录
			System.out.println("从cookie中提取购物车");
			
			return cartList_cookie;
		}else{ // 如果登录
			
			
			List<Cart> cartList_redis = cartService.findCartListFromRedis(username);
			
			// 合并购物车
			if(cartList_cookie.size()>0){
				
				List<Cart> mergeCartList = cartService.mergeCartList(cartList_cookie, cartList_redis);
				cartService.saveCartListToRedis(username, mergeCartList);
				
				// 清除cookie购物车
				util.CookieUtil.deleteCookie(request, response, "cartList");
				
				System.out.println("执行了合并购物车的逻辑");
				return mergeCartList;
			}
			
			return cartList_redis;
			
		}
		
		
	}
	
	@CrossOrigin(origins="http://localhost:9105",allowCredentials="true")
	@RequestMapping("/addGoodsToCartList")
	public Result addGoodsToCartList(Long itemId,Integer num){
		
		// response.setHeader("Access-Control-Allow-Origin", "http://localhost:9105"); // 可以访问的域
		// response.setHeader("Access-Control-Allow-Credentials", "true"); // 如果操作localhost:9105的cookie,必须加上这句话
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		System.out.println("当前登录帐号: " + username);
		
		try {
			
			// 登录了就是redis购物车,没登录就是cookie购物车
			List<Cart> cartList = findCartList();
			
			// 调用服务方法
			cartList = cartService.addGoodsToCartList(cartList, itemId, num);
			
			
			if("anonymousUser".equals(username)){ // 没有登录
				// 将新的购物车存入cookie
				String cartListString = JSON.toJSONString(cartList);
				util.CookieUtil.setCookie(request, response, "cartList", cartListString,3600*24, "UTF-8");
				System.out.println("向cookie存储购物车");
			}else{
				cartService.saveCartListToRedis(username, cartList);
			}
		
			
			
			return new Result(true,"存入购物车成功");
		} catch (Exception e) {
			e.printStackTrace();
			
			return new Result(false,"存入购物车失败");
		}
		
	}
}
