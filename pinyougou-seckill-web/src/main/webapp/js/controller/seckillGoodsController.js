app.controller('seckillGoodsController',function($scope,seckillGoodsService){
	
	// 查询秒杀商品集合
	$scope.findList=function(){
		
		seckillGoodsService.findList().success(function(response){
			
			$scope.list=response;
		});
	}
});