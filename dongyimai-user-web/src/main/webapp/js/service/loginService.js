app.service('loginService',function ($http){
    this.getLoginName = function (){
        return $http.get('../getLoginName.do');
    }
})