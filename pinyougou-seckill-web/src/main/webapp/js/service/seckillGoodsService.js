app.service('seckillGoodsService',function($http){
	
	
	this.findList=function(){
		
		return $http.get('seckillGoods/findList.do');
	}
	
	// 根据id查询秒杀商品
	this.findOne=function(id){
		return $http.get('seckillGoods/findOneFromRedis.do?id='+id)
	}
	
	this.submitOrder=function(seckillId){
		return $http.get('seckillOrder/submitOrder.do?seckillId='+seckillId);
	}
});