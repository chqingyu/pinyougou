app.controller('contentController',function($scope,contentService){
	
	// 根据分类id查询分类
	$scope.contentList=[];
	$scope.findByCategoryId=function(categoryId){
		contentService.findByCategoryId(categoryId).success(function(response){
			$scope.contentList[categoryId]=response;
		});
	}
	
	// 搜索
	$scope.search=function(){
		location.href="http://localhost:9104/search.html#?keywords="+$scope.keywords;
	}
});