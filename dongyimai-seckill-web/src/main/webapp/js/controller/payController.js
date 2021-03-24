app.controller('payController',function ($scope,$location,payService){

    $scope.createNative = function (){
        payService.createNative().success(
            function (response){
                $scope.outTradeNo = response.out_trade_no;   //订单号
                $scope.totalMoney = (response.total_amount/100).toFixed(2);    //分  需要分转元
                var qrious = new QRious({
                    'element':document.getElementById("erweima"),
                    'level':'H',
                    'size':250,
                    'value':response.qrCode
                });
                $scope.queryPayStatus($scope.outTradeNo);
        })
    }

    $scope.queryPayStatus = function (outTradeNo){
        payService.queryPayStatus(outTradeNo).success(
            function (response){
                if(response.success){
                    location.href="paysuccess.html#?money="+$scope.totalMoney;
                }else{
                    if(response.message=='二维码超时'){
                        //页面上显示二维码超时
                        document.getElementById("timeout").innerHTML = "二维码已过期，刷新页面重新获取二维码。";
                    }else{
                    location.href="payfail.html";
                    }
                }
        })
    }

    $scope.getMoney = function (){
        return $location.search()['money'];
    }
})