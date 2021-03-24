app.service('payService',function ($http){
    this.createNative = function (){
        return $http.get("../alipay/createNative.do");
    }

    this.queryPayStatus = function (outTradeNo){
        return $http.get("../alipay/queryPayStatus.do?outTradeNo="+outTradeNo);
    }
})