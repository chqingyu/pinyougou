app.service('brandService',function($http){
	
	// 查询所有品牌的一个集合
	this.findAll=function(){
		return $http.get('../brand/findAll.do');
	}
	
	
	// page:当前页码;rows: 当前页应该显示数据的条数
	this.findPage=function(page,rows){
		return $http.get('../brand/findPage.do?page='+page+'&rows='+rows);
	}
	
	// 根据id,查询一条数据
	this.findOne=function(id){
		return $http.get('../brand/findOne.do?id='+id);
	}
	
	this.add=function(entity){
		return $http.post('../brand/add.do',entity);
	}
	
	// 修改
	this.update=function(entity){
		return $http.post('../brand/update.do',entity);
	}
	
	// 删除
	this.dele=function(ids){
		return $http.get('../brand/delete.do?ids='+ids);
	}
	
	// 搜索
	this.search=function(page,rows,searchEntity){
		
		return $http.post('../brand/search.do?page='+page+'&rows='+rows,searchEntity);
	}
	
	// 所有品牌的下拉列表数据
	this.selectOptionList=function(){
		return $http.get('../brand/selectOptionList.do');
	}
});