package com.pinyougou.user.controller;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/login")
@RestController
public class LoginController {

	@RequestMapping("/name")
	public Map showName(){
		
		String name = SecurityContextHolder.getContext().getAuthentication().getName();
		
		Map map = new ConcurrentHashMap();
		map.put("loginName", name);
		return map;
	}
}
