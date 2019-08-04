var app=angular.module('pinyougou',[]);
// 定义过滤器
app.filter('trustHtml',['$sce',function($sce){
	
	// data是被过滤的内容
	return function(data){
		return $sce.trustAsHtml(data); // 返回的是过滤后的的内容(信任html)
	}
}]);