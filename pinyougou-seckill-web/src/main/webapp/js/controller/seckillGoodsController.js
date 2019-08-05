app.controller('seckillGoodsController',function($scope,$location,$interval,seckillGoodsService){
	
	// 查询秒杀商品集合
	$scope.findList=function(){
		
		seckillGoodsService.findList().success(function(response){
			
			$scope.list=response;
		});
	}
	
	// 查询秒杀商品
	$scope.findOne=function(){
		
		// 接受参数
		var id = $location.search()['id'];
		seckillGoodsService.findOne(id).success(function(response){
			
			$scope.entity=response;
			
			// 倒计时开始
			// 获取从结束时间到当前日期的秒数
			allsecond=Math.floor((new Date($scope.entity.endTime).getTime()-new Date().getTime())/1000);
			
			var time = $interval(function(){
				
				$scope.timeString=convertTimeString(allsecond);
				allsecond=allsecond-1;
				if($scope.second<=0){
					$interval.cancel(time)
				}
			},1000);
			
		});
	}
	
	
	// 转换秒为 天小时分钟秒格式 
	convertTimeString=function(allsecond){
		
		var days = Math.floor(allsecond/(60*60*24)); //天数
		
		var hours = Math.floor((allsecond-days*60*60*24)/(60*60));
		
		var minutes = Math.floor((allsecond-days*60*60*24-hours*60*60)/60);
		var seconds = allsecond-days*60*60*24-hours*60*60-minutes*60;
		
		var timeString = "";
		if(days>0){
			timeString += days+"天";
		}
		return timeString+hours+":"+minutes+":"+seconds;
	}

	// 提交订单
	$scope.submitOrder=function(){
		seckillGoodsService.submitOrder($scope.entity.id).success(function(response){
			
			if(response.success){
				alert("抢购成功,请在5分钟之内完成支付");
				location.href="pay.html"; // 跳转到支付页面
			}else{
				alert(response.message);
			}
			
		});
	}

});