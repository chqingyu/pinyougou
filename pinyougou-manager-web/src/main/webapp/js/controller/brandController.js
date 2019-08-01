app.controller('brandController',function($scope,$controller,brandService){
	
	$controller('baseController',{$scope:$scope}); // 控制器的继承
	
	$scope.findAll=function(){
		brandService.findAll().success(function(response){
			
			$scope.list=response; // 全部品牌的一个数组
		});
	}
	
	$scope.findPage=function(page,rows){
		
		// page:当前页码,rows:当前页显示的条数
		brandService.findPage(page,rows).success(function(response){
			
			// 后端:response={int total,List rows};
			$scope.list=response.rows;
			
			$scope.paginationConf.totalItems=response.total; //总记录数
		});
	}
	
	
	// 根据品牌的id,查询一条数据
	$scope.findOne=function(id){
		
		brandService.findOne(id).success(function(response){
			$scope.entity=response;
		});
	}
	
	
	$scope.save=function(){
		
		var serviceObject; // 用于接收服务层返回成功的对象引用
		
		// 新增和修改绑定的是同一个$scope.entity的引用
		if($scope.entity.id!=null){
			serviceObject = brandService.update($scope.entity);
		}else{
			serviceObject = brandService.add($scope.entity);
		}
		
		serviceObject.success(function(response){
			
			if(response.success){
				
				$scope.reloadList(); // 重新查询新的数据列表
			}else{
				alert(respose.message);
			}
		});
	}
	
	$scope.dele=function(){
		
		brandService.dele($scope.selectIds).success(function(response){
			
			$scope.reloadList(); // 重新查询新的数据列表
			
			$scope.selectIds=[];
		});
	}
	
	$scope.searchEntity={};
	$scope.search=function(page,rows){
		brandService.search(page,rows,$scope.searchEntity).success(function(response){
			
			$scope.list=response.rows;
			$scope.paginationConf.totalItems=response.total; // 更新总记录数
		});
	}
});