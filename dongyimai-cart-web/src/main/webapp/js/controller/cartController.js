app.controller('cartController', function ($scope, cartService,addressService) {
    $scope.findCartList = function () {
        cartService.findCartList().success(
            function (response) {
                $scope.cartList = response;
                $scope.totalValue = cartService.sum($scope.cartList);
            })
    }

    $scope.addGoodsToCartList = function (itemId, num) {
        cartService.addGoodsToCartList(itemId, num).success(
            function (response) {
                if (response.success) {
                    //刷新购物车列表
                    $scope.findCartList();
                } else {
                    alert(response.message);
                }
            })
    }

    $scope.findAddressListByUserId = function (){
        addressService.findAddressListByUserId().success(
            function (response){
                $scope.addressList = response;
                for(var i=0;i<$scope.addressList.length;i++){
                    if($scope.addressList[i].isDefault=='1'){
                        $scope.address = $scope.addressList[i];
                    }
                }

            })
    }

    //地址选择
    $scope.selectAddress = function (address){
        $scope.address = address;
    }
    //判断是否选中地址
    $scope.isSelect = function (address){
        if($scope.address == address){
            return true;
        }else{
            return false;
        }
    }

    //初始化订单对象的数据结构
    $scope.order = {'paymentType':'1'};   //支付类型，1、在线支付，2、货到付款
    $scope.selectPaymentType = function (type){
        $scope.order.paymentType = type;
    }

    $scope.submitOrder = function () {
        $scope.order.receiverAreaName = $scope.address.address;   //收货地址
        $scope.order.receiverMobile = $scope.address.mobile;        //联系电话
        $scope.order.receiver = $scope.address.contact;             //收货人
        cartService.submitOrder($scope.order).success(
            function (response) {
                if (response.success){
                    if ($scope.order.paymentType == '1') {
                        location.href = "pay.html";
                    } else {
                        location.href = "paysuccess.html";
                    }
                } else {
                    alert(response.message);
                }
        })
    }
})