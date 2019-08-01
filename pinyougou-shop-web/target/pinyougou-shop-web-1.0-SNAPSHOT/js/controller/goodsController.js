 //控制层 
app.controller('goodsController' ,function($scope,$controller,$location  ,goodsService,itemCatService,typeTemplateService,uploadService){	
	
	$controller('baseController',{$scope:$scope});//继承
	
    //读取列表数据绑定到表单中  
	$scope.findAll=function(){
		goodsService.findAll().success(
			function(response){
				$scope.list=response;
			}			
		);
	}    
	
	//分页
	$scope.findPage=function(page,rows){			
		goodsService.findPage(page,rows).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}
	
	//查询实体 ,用于修改前的回显
	$scope.findOne=function(){
		// 从地址栏中搜索是否有id
		var id = $location.search()['id'];
		if(id == null){
			return ;
		}
		goodsService.findOne(id).success(
			function(response){
				$scope.entity= response;
				
				editor.html($scope.entity.goodsDesc.introduction);
				$scope.entity.goodsDesc.itemImages = JSON.parse($scope.entity.goodsDesc.itemImages);
				$scope.entity.goodsDesc.customAttributeItems = JSON.parse($scope.entity.goodsDesc.customAttributeItems);
				$scope.entity.goodsDesc.specificationItems = JSON.parse($scope.entity.goodsDesc.specificationItems);
				
				for(var i = 0;i < $scope.entity.itemList.length;i++){
					$scope.entity.itemList[i].spec = JSON.parse($scope.entity.itemList[i].spec);
				}
			}
		);				
	}
	
	//保存 
	$scope.save=function(){	
		
		$scope.entity.goodsDesc.introduction=editor.html();
		
		var serviceObject;//服务层对象  				
		if($scope.entity.goods.id!=null){//如果有ID
			serviceObject=goodsService.update( $scope.entity ); //修改  
		}else{
			serviceObject=goodsService.add( $scope.entity  );//增加 
		}				
		serviceObject.success(
			function(response){
				if(response.success){
					alert("保存成功");
					location.href='goods.html';
				}else{
					alert(response.message);
				}
			}		
		);				
	}
	
	 
	//批量删除 
	$scope.dele=function(){			
		//获取选中的复选框			
		goodsService.dele( $scope.selectIds ).success(
			function(response){
				if(response.success){
					$scope.reloadList();//刷新列表
					$scope.selectIds=[];
				}						
			}		
		);				
	}
	
	$scope.searchEntity={};//定义搜索对象 
	
	//搜索
	$scope.search=function(page,rows){
		goodsService.search(page,rows,$scope.searchEntity).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}
	
	$scope.uploadFile=function(){
		uploadService.uploadFile().success(function(response){
			
			if(response.success){
				$scope.image_entity.url=response.message;
			}else{
				alert(response.message);
			}
		});
	}
	
	$scope.entity={goods:{},goodsDesc:{itemImages:[],specificationItems:[]}};
	
	// 将当前上传的图片实体存入图片列表
	$scope.add_image_entity=function(){
		$scope.entity.goodsDesc.itemImages.push($scope.image_entity);
	}
    
	// 从图片列表中移除图片
	$scope.remove_image_entity=function(index){
		
		$scope.entity.goodsDesc.itemImages.splice(index,1);
	}
	
	// 查询一级分类列表
	$scope.selectItemCatList=function(){
		
		itemCatService.findByParentId(0).success(function(response){
			$scope.itemCat1List=response;
		});
	}
	
	// 观察一级分类值的改变,触发这个函数($watch() ),读取二级分类
	$scope.$watch('entity.goods.category1Id',function(newValue,oldValue){
		
		itemCatService.findByParentId(newValue).success(function(response){
			$scope.itemCat2List=response;
		});
	});
	
	// 观察二级分类值的改变,触发这个函数($watch() ),读取三级分类
	$scope.$watch('entity.goods.category2Id',function(newValue,oldValue){
		
		itemCatService.findByParentId(newValue).success(function(response){
			$scope.itemCat3List=response;
		});
	});
	
	
	
	// ... 读取模版id
	$scope.$watch('entity.goods.category3Id',function(newValue,oldValue){
		
		itemCatService.findOne(newValue).success(function(response){
			$scope.entity.goods.typeTemplateId=response.typeId;
		});
		
		
	});
	
	// 观察entity.goods.typeTemplateId的值的改变,触发这个函数,读取这个类型模版的一条记录
	$scope.$watch('entity.goods.typeTemplateId',function(newValue,oldValue){
		
		if($location.search()['id']==null){
			typeTemplateService.findOne(newValue).success(function(response){
				
				$scope.typeTemplate=response;
				$scope.typeTemplate.brandIds=JSON.parse($scope.typeTemplate.brandIds);
				$scope.entity.goodsDesc.customAttributeItems=JSON.parse($scope.typeTemplate.customAttributeItems);
				
			});
		}
		
		
		// .... 读取规格
		typeTemplateService.findSpecList(newValue).success(function(response){
			// 返回一个List<map>,map的数据{"id":33,"text":"屏幕尺寸",options:[{"optionName":"移动4G",....},{}]}
			$scope.specList=response;
		});
	});
	
	
	// 更改goodsDesc.specificationItems的数据
	$scope.updateSpecAttribute=function($event,name,value){
		
		var object = $scope.searchObjectByKey($scope.entity.goodsDesc.specificationItems,'attributeName',name);
		
		if(object!=null){
			
			if($event.target.checked){
				object.attributeValue.push(value);
			}else{
				object.attributeValue.splice(object.attributeValue.indexOf(value),1); // 移除这个属性对应的一个值
				if(object.attributeValue.length==0){
					$scope.entity.goodsDesc.specificationItems.splice($scope.entity.goodsDesc.specificationItems.indexOf(object),1);
				}
			}
		}else{
			$scope.entity.goodsDesc.specificationItems.push({"attributeName":name,"attributeValue":[value]});
		}
	}
	
	$scope.creatItemList=function(){
		$scope.entity.itemList=[{spec:{},price:0,num:9999,status:'0',isDefault:'0'}];
		
		var items = $scope.entity.goodsDesc.specificationItems;
		// 遍历[{attributeName:'...',attributeValue:[...]},{attributeName:'...',attributeValue:[...]}]
		for(var i = 0;i < items.length;i++){
			$scope.entity.itemList=addColumn($scope.entity.itemList,items[i].attributeName,items[i].attributeValue);
		}
	}
	
	addColumn=function(list,columnName,columnValues){
		
		var newList=[];
		
		// list是指$scope.entity.itemList
		for(var i = 0;i < list.length;i++){
			var oldRow = list[i];
			for(var j = 0;j < columnValues.length;j++){
				var newRow = JSON.parse(JSON.stringify(oldRow)); //深克隆
				newRow.spec[columnName]=columnValues[j];
				newList.push(newRow);
			}
		}
		return newList;
	}
	
	$scope.status=['未审核','已审核','审核未通过','已关闭'];
	$scope.itemCatList=[]; // 商品分类列表
	
	// 查询商品分类列表
	$scope.findItemCatList=function(){
		itemCatService.findAll().success(function(response){
			
			for(var i = 0;i < response.length;i++){
				$scope.itemCatList[response[i].id]=response[i].name;
			}
		});
	}
	
	$scope.checkAttributeValue=function(specName,optionName){
		
		// items : [{attributeName:aaa,attributeValue:[...],attributeName:bbb,attributeValue:[]}]
		var items = $scope.entity.goodsDesc.specificationItems;
		// 当前这个attributeName是否在items中
		var object = $scope.searchObjectByKey(items,'attributeName',specName);
		
		if(object != null){
			if(object.attributeValue.indexOf(optionName) >= 0){
				return true;
			}else{
				return false;
			}
		}else{
			return false;
		}
		
	}
	
	
	
});	
