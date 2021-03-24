app.service('uploadService',function ($http){
    this.uploadFile = function (){
        //获取表单的对象
        var formData = new FormData();
        //获取的上传文件的表单控件的数据
        formData.append('file',file.files[0]);

        return $http({
            'method':'POST',
            'url':'../uploadFile.do',
            'data':formData,
            'headers':{'Content-Type':undefined},   //angularJs的get和post请求默认传参JSON，上传文件需要单独设置成undefined，默认文件流的参数格式
            'transformRequest': angular.identity   //使用angularJs对数据进行序列化
        });

    }
})