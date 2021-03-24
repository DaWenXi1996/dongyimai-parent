app.controller('itemSearchController', function ($scope,$location, itemSearchService) {

    $scope.search = function () {
        $scope.searchMap.pageNo = parseInt($scope.searchMap.pageNo);
        itemSearchService.search($scope.searchMap).success(
            function (response) {
                $scope.resultMap = response;
                buildPageLabel();

            })
    }

    $scope.searchMap = {
        'keywords': '',
        'category': '',
        'brand': '',
        'spec': {},
        'price': '',
        'pageNo': 1,
        'pageSize': 20,
        'sortField': '',
        'sortValue': ''
    };

    $scope.removeSearchItem = function (key) {
        if (key == 'category' || key == 'brand' || key == 'price') {
            $scope.searchMap[key] = '';
        } else {
            //JSON中移除对象
            delete $scope.searchMap.spec[key];
        }
        //执行查询
        $scope.searchMap.pageNo=1;
        $scope.search();
    }

    $scope.addSearchItem = function (key, value) {
        if (key == 'category' || key == 'brand' || key == 'price') {
            $scope.searchMap[key] = value;
        } else {
            $scope.searchMap.spec[key] = value;
        }
        $scope.searchMap.pageNo=1;
        //执行查询
        $scope.search();
    }

    buildPageLabel = function () {
        $scope.pageLabel = [];
        var maxPage = $scope.resultMap.totalPages;
        var firstPage = 1;
        var lastPage = maxPage;
        $scope.firstDot = true;
        $scope.lastDot = true;
        if (maxPage > 5) {     //总页码大于5页才能产生动态页码
            if ($scope.searchMap.pageNo <= 3) {
                lastPage = 5;
                $scope.firstDot = false;
            } else if ($scope.searchMap.pageNo >= lastPage - 2) {
                firstPage = maxPage - 4;
                $scope.lastDot = false;
            } else {
                firstPage = $scope.searchMap.pageNo - 2;
                lastPage = $scope.searchMap.pageNo + 2;
            }
        } else {
            $scope.firstDot = false;
            $scope.lastDot = false;
        }
        for (var i = firstPage; i <= lastPage; i++) {
            $scope.pageLabel.push(i);
        }
    }

    $scope.queryByPage = function (pageNo) {
        if (pageNo < 1 || pageNo > $scope.resultMap.totalPages) {
            return;
        }
        $scope.searchMap.pageNo = pageNo;
        $scope.search();
    }

    //判断当前页是否是第一页
    $scope.isTopPage = function () {
        if ($scope.searchMap.pageNo == 1) {
            return true;
        } else {
            return false;
        }
    }

    //判断当前页是否是最后一页
    $scope.resultMap = {'totalPages': 10};
    $scope.isLastPage = function () {
        if ($scope.searchMap.pageNo == $scope.resultMap.totalPages) {
            return true;
        } else {
            return false;
        }
    }

    //判断是否是当前页
    $scope.isPage = function (pageNo) {
        if ($scope.searchMap.pageNo == pageNo) {
            return true;
        } else {
            return false;
        }
    }

    $scope.sortSearch = function (sortField, sortValue) {
        $scope.searchMap.sortField = sortField;
        $scope.searchMap.sortValue = sortValue;
        //执行查询
        $scope.search();
    }

    $scope.keywordsIsBrand = function () {
        for (var i = 0; i < $scope.resultMap.brandList.length; i++) {
            if ($scope.searchMap.keywords.indexOf($scope.resultMap.brandList[i].text) > -1) {
                return true;
            }
        }
        return false;
    }

    $scope.loadKeywords = function () {
        $scope.searchMap.keywords = $location.search()['keywords'];
        //执行查询
        $scope.search();
    }
})