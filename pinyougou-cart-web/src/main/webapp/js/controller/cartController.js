app.controller('cartController',function($scope,cartService){
	
	// 查询购物车列表
	$scope.findCartList=function(){
		
		cartService.findCartList().success(function(response){
			
			$scope.cartList=response;
			$scope.totalValue = cartService.sum($scope.cartList);
		});
	}
	
	// 某一个购物车明细数量的增加
	$scope.addGoodsToCartList=function(itemId,num){
		
		cartService.addGoodsToCartList(itemId,num).success(function(response){
			
			if(response.success){ // 如果成功
				
				$scope.findCartList(); // 重新发送请求从数据,cookie中获取;
				
			}else{
				alert(response.message);
			}
		});
	}
	
	
	/*sum=function(cartList){
		
		var totalValue = {totalNum:0,totalMoney:0};

		
		for(var i=0;i<cartList.length;i++){
			
			var cart = $scope.cartList[i]
			for(var j=0;j<cart.orderItemList.length;j++){
				var orderItem = cart.orderItemList[j]; // 每一个购物车明细
				totalValue.totalNum += orderItem.num; // 累加数量
				totalValue.totalMoney += orderItem.totalFee; // 累加金额
			}
		}
		
		return totalValue;
	}*/
	
	
	// 获取当前用户的地址列表
	$scope.findAddressList=function(){
		
		cartService.findAddressList().success(function(response){
			
			$scope.addressList=response;
			
			for(var i=0;i<$scope.addressList.length;i++){
				
				
				if($scope.addressList[i].isDefault=='1'){
					$scope.address=$scope.addressList[i];
					break;
				}
			}
		});
	}
	
	// 选择地址
	$scope.selectAddress=function(address){
		$scope.address=address;
	}
	
	// 判断某地址对象是不是当前选择的地址
	$scope.isSelectedAddress=function(address){
		
		if(address == $scope.address){
			return true;
		}else{
			return false;
		}
	}
	
	$scope.order={paymentType:'1'};
	
	// 选择支付类型
	$scope.selectPayType=function(type){
		
		$scope.orde
	}
	
	// 保存订单
	$scope.submitOrder=function(){
		
		$scope.order.receiverAreaName=$scope.address.address; //地址
		$scope.order.receiverMobile=$scope.address.mobile;
		$scope.order.receiver=$scope.address.contact; //联系人
		cartService.submitOrder($scope.order).success(function(response){
//			alert(response.message);
			if(response.success){
				// 页面跳转
				if($scope.order.paymentType=='1'){ // 如果是微信,跳到支付页面
					location.href="pay.html";
				}else{ // 如果货到付款,跳转到提示页面
					
					location.href="paysuccess.html";
				}
			}else{
				alert(response.message); // 也可以跳转到提示页面
			}
		});
	}
});