app.service('uploadService',function($http){
	
	this.uploadFile=function(){
		var formdata = new FormData();
		formdata.append('file',file.files[0]); // 'file':表单中是<input type="file">,file.file[0]:表单中是<input type="file",name="file">,中第一个name="file"
		
		return $http({
			
			url:'../upload.do',
			method:'post',
			data:formdata,
			headers:{'Content-Type':undefined},
			transformRequest:angular.indentity
		});
	}
});