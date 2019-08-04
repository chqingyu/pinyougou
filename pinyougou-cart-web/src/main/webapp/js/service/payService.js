app.service('payService',function(){
	
	this.createNative=function(){
		
		return $http.get('pay/createNative.do');
	}
	
	this.queryPayStatus=function(){
		
		return $http.get('pay/queryPayStatus.do?out_trade_no='+out_trade_no);
	}
});