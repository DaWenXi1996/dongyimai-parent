 //控制层 
app.controller('goodsController' ,function($scope,$controller,$location,goodsService,itemCatService,typeTemplateService,uploadService){
	
	$controller('baseController',{$scope:$scope});//继承
	
    //读取列表数据绑定到表单中  
	$scope.findAll=function(){
		goodsService.findAll().success(
			function(response){
				$scope.list=response;
			}			
		);
	}    
	
	//分页
	$scope.findPage=function(page,rows){			
		goodsService.findPage(page,rows).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}
	
	//查询实体 
	$scope.findOne=function(id){
        var id = $location.search()['id'];
        if (id == null) {
            return;
        }
        goodsService.findOne(id).success(
            function (response) {
                $scope.entity = response;
                //富文本编辑器
                editor.html($scope.entity.goodsDesc.introduction);
                //JSON结构的字符串的字段需要做JSON类型转换
                $scope.entity.goodsDesc.itemImages = JSON.parse($scope.entity.goodsDesc.itemImages);
                $scope.entity.goodsDesc.customAttributeItems = JSON.parse($scope.entity.goodsDesc.customAttributeItems);
                $scope.entity.goodsDesc.specificationItems = JSON.parse($scope.entity.goodsDesc.specificationItems);

                //SKU中的规格信息 做JSON类型转换
                for(var i=0;i<$scope.entity.itemList.length;i++){
                    $scope.entity.itemList[i].spec =  JSON.parse($scope.entity.itemList[i].spec);
                }
            }
        );
    }

    $scope.checkAttributeValue = function (specName, optionValue) {
        var item = $scope.entity.goodsDesc.specificationItems;
        var object = $scope.searchObjectByKey(item, 'attributeName', specName);
        if(object==null){
            return false;
        }else{
            //判断规格选项在集合中是否存在
            if(object.attributeValue.indexOf(optionValue)>-1){
                return true;
            }else{
                return false;
            }
        }
    }

    $scope.entity={'goods':{},'goodsDesc':{'specificationItems': [],'itemImages': []}}
	//保存 
	$scope.save=function(){
        $scope.entity.goodsDesc.introduction = editor.html();
		var serviceObject;//服务层对象  				
		if($scope.entity.goods.id!=null){//如果有ID
			serviceObject=goodsService.update( $scope.entity ); //修改  
		}else{
			serviceObject=goodsService.add( $scope.entity  );//增加 
		}				
		serviceObject.success(
			function(response){
				if(response.success){
                    //清空表单
                    $scope.entity={'goods':{},'goodsDesc':{'specificationItems': [],'itemImages': []}}
                    //清空富文本编辑器
                    editor.html('');
                    location.href = "goods.html";
				}else{
					alert(response.message);
				}
			}		
		);				
	}

	// $scope.add = function (){
	// 	//获取富文本编辑器的内容
	// 	$scope.entity.goodsDesc.introduction = editor.html();
	// 	goodsService.add($scope.entity).success(
	// 		function (response){
	// 		if(response.success){
	// 			//清空表单
    //             $scope.entity={'goods':{},'goodsDesc':{'specificationItems': [],'itemImages': []}}
	// 			//清空富文本编辑器
	// 			editor.html('');
	// 		}else{
	// 			alert(response.message);
	// 		}
	// 	})
	// }

	
	 
	//批量删除 
	$scope.dele=function(){			
		//获取选中的复选框			
		goodsService.dele( $scope.selectIds ).success(
			function(response){
				if(response.success){
					$scope.reloadList();//刷新列表
					$scope.selectIds=[];
				}						
			}		
		);				
	}
	
	$scope.searchEntity={};//定义搜索对象 
	
	//搜索
	$scope.search=function(page,rows){			
		goodsService.search(page,rows,$scope.searchEntity).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}

    $scope.selectItemCat1List=function () {
		itemCatService.findByParentId(0).success(
            function(response){
                $scope.itemCat1List=response;
            }
		);
    }

    $scope.$watch('entity.goods.category1Id',function (newValue,oldValue) {
		if(newValue){
            itemCatService.findByParentId(newValue).success(
                function(response){
                    $scope.itemCat2List=response;
                }
            );
		}
    })

    $scope.$watch('entity.goods.category2Id',function (newValue,oldValue) {
        if(newValue){
            itemCatService.findByParentId(newValue).success(
                function(response){
                    $scope.itemCat3List=response;
                }
            );
        }
    })

    $scope.$watch('entity.goods.category3Id',function (newValue,oldValue) {
        if(newValue){
            itemCatService.findOne(newValue).success(
                function(response){
                    $scope.entity.goods.typeTemplateId = response.typeId;
                }
            );
        }
    })

    $scope.$watch('entity.goods.typeTemplateId',function (newValue,oldValue) {
        if(newValue){
            typeTemplateService.findOne(newValue).success(
                function(response){
                    $scope.brandList = JSON.parse(response.brandIds);
                    // $scope.entity.goodsDesc.customAttributeItems = JSON.parse(response.customAttributeItems);
                    if ($location.search()["id"] == null) {
                        $scope.entity.goodsDesc.customAttributeItems = JSON.parse(response.customAttributeItems);
                    }
                }
            );
            typeTemplateService.findSpecList(newValue).success(
                function(response){
                    $scope.specList = response;
                }
            );
        }
    })

	$scope.updateSpecAttribute=function ($event,text,optionName) {
        var object = $scope.searchObjectByKey($scope.entity.goodsDesc.specificationItems, 'attributeName', text);
        if(object==null){
            $scope.entity.goodsDesc.specificationItems.push({"attributeName":text,"attributeValue":[optionName]})
		}else if ($event.target.checked){
        	object.attributeValue.push(optionName);
		}else{
            object.attributeValue.splice(object.attributeValue.indexOf(optionName), 1);
            if(object.attributeValue.length==0){
                $scope.entity.goodsDesc.specificationItems.splice($scope.entity.goodsDesc.specificationItems.indexOf(object), 1);
			}
		}
    }

    //初始化表单图片对象的数据结构
    $scope.item_image_entity = {};
    $scope.uploadFile = function () {
        uploadService.uploadFile().success(
            function (response) {
                if (response.success) {
                    $scope.item_image_entity.url = response.message;// 获取文件路径并回显
                } else {
                    alert(response.message);
                }
            }).error(function () {
            alert("上传图片发生异常");
        })
    }

    //增加行
    $scope.addTableRow = function () {
        $scope.entity.goodsDesc.itemImages.push($scope.item_image_entity);
    }

    //删除行
    $scope.deleteTableRow = function (index) {
        $scope.entity.goodsDesc.itemImages.splice(index, 1);
    }

    //创建SKU列表
    $scope.createItemList = function () {
        //1.初始化SKU信息的数据结构
        $scope.entity.itemList = [{'price': 0, 'num': 9999, 'status': '0', 'isDefault': '0', 'spec': {}}];
        //2.遍历规格集合，构建SKU的行和列
        var items = $scope.entity.goodsDesc.specificationItems;
        for (var i = 0; i < items.length; i++) {//循环添加列
            $scope.entity.itemList = addColumn($scope.entity.itemList, items[i].attributeName, items[i].attributeValue);
        }
    }

    /**
     * 添加列
     * @param list     SKU的集合
     * @param columnName    列名   规格名称
     * @param columnValues  列值   规格选项集合
     */
    addColumn = function (list, columnName, columnValues) {
        var newList = [];    //初始化一个集合  用来存储新增列之后的 行 集合
        for (var i = 0; i < list.length; i++) {
            var oldRow = list[i];      //取得原来的一行数据  等待增加列
            //遍历规格选项集合
            for (var j = 0; j < columnValues.length; j++) {//根据长度增加行
                var newRow = JSON.parse(JSON.stringify(oldRow)); //深克隆
                newRow.spec[columnName] = columnValues[j];
                newList.push(newRow);
            }
        }
        return newList;
    }

    $scope.status = ['未审核', '审核通过', '驳回', '关闭'];

    $scope.categoryList = [];

    $scope.findItemCatList = function () {
        itemCatService.findAll().success(function (response) {
            for (var i = 0; i < response.length; i++) {
                $scope.categoryList[response[i].id] = response[i].name;
            }
        })
    }
});