app.service('cartService', function ($http) {
    this.findCartList = function () {
        return $http.get('../cart/findCartList.do');
    }

    this.addGoodsToCartList = function (itemId, num) {
        return $http.get('../cart/addGoodsToCartList.do?itemId=' + itemId + '&num=' + num);
    }

    this.sum = function (cartList) {
        var totalValue = {'totalNum': 0, 'totalMoney': 0.00};
        for (var i = 0; i < cartList.length; i++) {
            var orderItemList = cartList[i].orderItemList;
            for (var j = 0; j < orderItemList.length; j++) {
                totalValue.totalNum += orderItemList[j].num;    //总数量
                totalValue.totalMoney += orderItemList[j].totalFee;   //总金额
            }
        }
        return totalValue;
    }

    //提交订单
    this.submitOrder = function (entity){
        return $http.post('../order/add.do',entity);
    }
})