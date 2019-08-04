app.controller('baseController',function($scope){
	
	$scope.reloadList=function(){
		
		// 切换页码,切换至选定页码数据
		$scope.search($scope.paginationConf.currentPage,$scope.paginationConf.itemsPerPage);
	}
	
	$scope.paginationConf={
			currentPage: 1,
			totalItems: 10,
			itemsPerPage: 10,
			perPageOptions: [10, 20, 30, 40, 50],
			onChange: function(){
				$scope.reloadList(); // 切换页码后刷新数据
			}
	};
	
	$scope.selectIds=[]; //选中的id集合
	
	$scope.updateSelection = function($event,id){
		
		if($event.target.checked){ // 如果被选中,增加到数组中
			$scope.selectIds.push(id);
		}else{
			var idx = $scope.selectIds.indexOf(id);
			$scope.selectIds.splice(idx,1); // 删除
		}
	}
	
	// 这个是将json字符串的Map(对象)数组,转换成josn对象,遍历数组,拼接某个key的值成一个字符串
	$scope.jsonToString=function(jsonString,key){
		
		var json=JSON.parse(jsonString);
		var value="";
		
		for(var i=0;i<json.length;i++){
			
			if(i>0){
				value+=",";
			}
			value +=json[i][key];
		}
		return value;
	}
	
	
	// 在List<Map>集合中根据某key的值查询对象
	$scope.searchObjectByKey=function(list,key,keyValue){
		
		for(var i=0;i<list.length;i++){
			if(list[i][key]==keyValue){
				return list[i];
			}
		}
		return null;
	}
});