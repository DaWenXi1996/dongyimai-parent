app.controller('itemPageController',function ($scope,$http){
    //加减购买数量
    $scope.add = function (num){
        $scope.num += num;
        //格式校验
        if($scope.num<1){
            $scope.num=1;
        }
    }

    //初始化规格对象的数据结构
    $scope.specificationItems = {};
    $scope.selectSpecification = function (name,value){
        $scope.specificationItems[name] = value;
        $scope.searchSku();
    }

    //判断是否是当前选中规格选项
    $scope.isSpecification = function (name,value){
        if($scope.specificationItems[name]==value){
            return true;
        }else{
            return false;
        }
    }

    //加载SKU数据
    $scope.loadSku = function (){
        $scope.sku = skuList[0];
        $scope.specificationItems = JSON.parse(JSON.stringify($scope.sku.spec));    //深克隆
    }

    //选中规格，将该规格的SKU信息显示
    $scope.searchSku = function (){
        //遍历skuList
        for(var i=0;i<skuList.length;i++){
            //比较选中的规格和skuList中的SKU对象的规格是否一致
            if(matchObject($scope.specificationItems,skuList[i].spec)){
                $scope.sku = skuList[i];
                return;
            }
        }
        //如果规格都不符合，则设置一个默认的SKU对象
        $scope.sku={'id':0,'title':'--','price':'0','spec':{}};
    }

    matchObject = function (map1,map2){
        for(var key in map1){
            if(map1[key]!=map2[key]){
                return false;
            }
        }
        for(var key in map2){
            if(map2[key]!=map1[key]){
                return false;
            }
        }
        return true;
    }

    //加入购物车
    $scope.addItemCart = function (){
        $http.get('http://localhost:9108/cart/addGoodsToCartList.do?itemId='+$scope.sku.id+'&num='+$scope.num,{'withCredentials':true}).success(
            function (response){
                if(response.success){
                    location.href="http://localhost:9108/cart.html";
                }else{
                    alert(response.message);
                }
            })
    }
})