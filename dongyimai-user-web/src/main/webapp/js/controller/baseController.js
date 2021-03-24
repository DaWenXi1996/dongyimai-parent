app.controller('baseController', function ($scope) {
    //设置分页参数
    $scope.paginationConf = {
        'totalItems': 10, //总记录数
        'currentPage': 1,  //当前页码
        'itemsPerPage': 10, //每页显示记录数
        'perPageOptions': [10, 20, 30, 40, 50], //每页显示记录数的设置
        onChange: function () {
            //执行分页查询
            //$scope.findPage($scope.paginationConf.currentPage,$scope.paginationConf.itemsPerPage);
            $scope.reloadList();
        }
    }

    $scope.reloadList = function () {
        // $scope.findPage($scope.paginationConf.currentPage, $scope.paginationConf.itemsPerPage);
        $scope.search($scope.paginationConf.currentPage, $scope.paginationConf.itemsPerPage);
    }

    $scope.selectIds = [];//初始化ID数组的数据结构

    //选中、反选
    //$event 复选框的事件对象  id 操作品牌id
    $scope.updateSelection = function ($event, id) {
        //判断复选框是选中还是反选
        if ($event.target.checked) {
            $scope.selectIds.push(id);   //选中则添加元素
        } else {
            //反选则移除元素
            var index = $scope.selectIds.indexOf(id);
            $scope.selectIds.splice(index, 1);    //参数一：移除元素的索引位置  参数二：移除个数
        }
    }

    //转换字符串
    $scope.jsonToString = function (jsonString, key) {
        //1.将JSON结构的字符串转换成JSON对象
        var json = JSON.parse(jsonString);
        var value = "";
        //2.遍历JSON对象（集合）
        for (var i = 0; i < json.length; i++) {
            if (i > 0) {
                value += "，";
            }
            //3.取得key值，并拼接字符串
            value += json[i][key];
        }
        //4.返回拼接好的字符串
        return value;
    }

    //根据key值在集合中判断对象是否存在
    $scope.searchObjectByKey = function (list,key,keyValue){
        for(var i=0;i<list.length;i++){
            if(list[i][key]==keyValue){
                return list[i];
            }
        }
        return null;
    }


})