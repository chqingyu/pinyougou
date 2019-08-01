app.controller('searchController',function($scope,$location,searchService){
	
	// 定义搜索对象的结构
	$scope.searchMap={'keywords':'','category':'','brand':'','spec':{},'price':'','pageNo':1,'pageSize':40,'sort':'','sortField':''};
	
	// 搜索
	$scope.search=function(){
		$scope.searchMap.pageNo=parseInt($scope.searchMap.pageNo);
		searchService.search($scope.searchMap).success(function(response){
			$scope.resultMap=response;
			buildPageLabel(); // 构建分页栏
			
		});
	}
	
	buildPageLabel=function(){
		// 构建分页栏
		$scope.pageLabel=[];
		var firstPage = 1; // 开始页码
		var lastPage=$scope.resultMap.totalPage;
		
		$scope.firstDot=true; // 前边有点
		$scope.lastDot=true; // 后边有点
		
		// 如果总页码>5的情况,小于5的话不用改变,因为lastPage=$scope.resultMap.totalPage就是小于5直接显示
		if($scope.resultMap.totalPage>5){ 
			if($scope.searchMap.pageNo<=3){
				lastPage=5;
				$scope.firstDot=false; 
			}else if($scope.searchMap.pageNo>=$scope.resultMap.totalPage-2){
				firstPage=$scope.resultMap.totalPage-4;
				$scope.lastDot=false;
			}else{
				firstPage=$scope.searchMap.pageNo-2;
				lastPage=$scope.searchMap.pageNo+2;
			}
		}else{
			$scope.firstDot=false; 
			$scope.lastDot=false;
		}
		for(var i=firstPage;i<=lastPage;i++){
			$scope.pageLabel.push(i);
		}
	}
	
	// 添加搜索项,改变searchMap
	$scope.addSearchItem=function(key,value){
		
		if(key=='category' || key == 'brand' || key== 'price'){ // 如果用户点击的是分类或品牌或价格
			$scope.searchMap[key]=value;
		}else{ // 用户点击的是规格
			
			$scope.searchMap.spec[key]=value;
		}
		$scope.search(); // 查询
	}
	
	// 撤销搜索项
	$scope.removeSearchItem=function(key){
		if(key=='category' || key=='brand'|| key == 'price'){
			$scope.searchMap[key]="";
		}else{
			delete $scope.searchMap.spec[key];
		}
		
		$scope.search(); // 查询
	}
	
	// 分页查询
	$scope.queryByPage=function(pageNo){
		if(pageNo<1 || pageNo>$scope.resultMap.totalPage){
			return;
		}
		$scope.searchMap.pageNo=pageNo;
		$scope.search();
	}
	
	// 判断当前页是否是第一页
	$scope.isTopPage=function(){
		if($scope.searchMap.pageNo==1){
			return true;
		}else{
			return false;
		}
	}
	
	// 判断当前页是否为最后一页
	$scope.isEndPage=function(){
		if($scope.searchMap.pageNo == $scope.resultMap.totalPage){
			return true;
		}else{
			return false;
		}
	}
	
	// 排序查询
	$scope.sortSearch=function(sortField,sort){
		$scope.searchMap.sortField=sortField;
		$scope.searchMap.sort=sort;
		$scope.search();
	}
	
	// 判断关键字是否是品牌
	$scope.keywordsIsBrand=function(){
		
		for(var i=0;i<$scope.resultMap.brandList.length;i++){
			
			if($scope.searchMap.keywords.indexOf($scope.resultMap.brandList[i].text) >= 0){
				return true;
			}
		}
		return false;
	}
	
	// 接收首页传递过来的关键字
	$scope.loadkeywords=function(){
		$scope.searchMap.keywords = $location.search()['keywords'];
		$scope.search(); // 查询
	}
});