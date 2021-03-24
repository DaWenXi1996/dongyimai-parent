app.controller('baseController',function ($scope) {
    //设置分页参数
    $scope.paginationConf = {
        'totalItems': 10 , //总记录数
        'currentPage': 1,  //当前页码
        'itemsPerPage': 10, //每页显示记录数
        'perPageOptions': [5,10,20,50], //每页显示记录数的设置
        onChange: function (){
            $scope.reloadList();
        }
    }

    $scope.reloadList = function(){
        // $scope.findPageBrand($scope.paginationConf.currentPage,$scope.paginationConf.itemsPerPage);
        $scope.search($scope.paginationConf.currentPage,$scope.paginationConf.itemsPerPage);
    }

    $scope.selectIds = [];
    $scope.updateSelection = function ($event,id) {
        if($event.target.checked){
            $scope.selectIds.push(id);
        }else {
            var index = $scope.selectIds.indexOf(id);
            $scope.selectIds.splice(index,1);
        }
    }

    $scope.jsonToString = function (jsonString, key) {
        var json = JSON.parse(jsonString);
        var string = "";
        for(var i=0; i< json.length;i++){
            if(i>0) {string += ",";}
            string += json[i][key];
        }
        return string;
    }
})