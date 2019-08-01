 //控制层 
app.controller('itemCatController' ,function($scope,$controller   ,itemCatService,typeTemplateService){	
	
	$controller('baseController',{$scope:$scope});//继承
	
    //读取列表数据绑定到表单中  
	$scope.findAll=function(){
		itemCatService.findAll().success(
			function(response){
				$scope.list=response;
			}			
		);
	}    
	
	//分页
	$scope.findPage=function(page,rows){			
		itemCatService.findPage(page,rows).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}
	
	//查询实体 
	$scope.findOne=function(id){				
		itemCatService.findOne(id).success(
			function(response){
				$scope.entity= response;
				$scope.findByParentId($scope.parenIdF);
			}
		);				
	}
	
	//保存 
	$scope.save=function(){				
		var serviceObject;//服务层对象  				
		if($scope.entity.id!=null){//如果有ID
			serviceObject=itemCatService.update( $scope.entity ); //修改  
		}else{
			$scope.entity.parentId=$scope.parentIdF;
			serviceObject=itemCatService.add( $scope.entity  );//增加 
		}				
		serviceObject.success(
			function(response){
				if(response.success){
					findByParentId($scope.parentIdF);
				}else{
					alert(response.message);
				}
			}		
		);				
	}
	
	 
	//批量删除 
	$scope.dele=function(){			
		//获取选中的复选框			
		itemCatService.dele( $scope.selectIds ).success(
			function(response){
				if(response.success){
					$scope.reloadList();//刷新列表
				}						
			}		
		);				
	}
	
	$scope.searchEntity={};//定义搜索对象 
	
	//搜索
	$scope.search=function(page,rows){			
		itemCatService.search(page,rows,$scope.searchEntity).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}
	
	
	$scope.parentIdF=0
	// 根据上级分类id查询列表
	$scope.findByParentId=function(parentId){
		
		$scope.parentIdF=parentId;
		itemCatService.findByParentId(parentId).success(function(response){
			// 商品分类的集合
			$scope.list=response
		});
	}
    
	
	// 设置一个分类级别,用于控制面包屑的显示
	$scope.grade=1;
	$scope.setGrade=function(value){
		$scope.grade=value;
	}
	
	// p_entity是你所点的那个分类对象
	$scope.selectList=function(p_entity){
		
		if($scope.grade==1){
			
			$scope.entity_1=null;
			$scope.entity_2=null;
		}
		if($scope.grade==2){
			$scope.entity_1=p_entity;
			$scope.entity_2=null;
		}
		
		if($scope.grade==3){
			$scope.entity_2=p_entity;
		}
		
		// 根据你所点的这个分类对象的id,查询下级分类列表
		$scope.findByParentId(p_entity.id);
	}
	
	// select2控件数据
	$scope.selectListData={data:[]};
	$scope.findListData=function(){
		typeTemplateService.findListData().success(function(response){
			
			$scope.selectListData={data:response};
		});
	}
});	
