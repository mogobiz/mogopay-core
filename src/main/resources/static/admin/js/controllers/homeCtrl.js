function HomeCtrl($scope, $location, $rootScope, $route) {
    $scope.customerLogin =  function () {
        callServer("account/customer-token", "", function (response) {
                $rootScope.xtoken = response;
                    $rootScope.isMerchant = false;
                    $rootScope.xtoken = response;
                    $location.path("/login");
                    $scope.$apply();
                    $location.replace();
            },
            function (response) {});
    }
    $scope.merchantLogin =  function () {
        callServer("account/merchant-token", "",
            function (response) {
                $rootScope.xtoken = response;
                    $rootScope.isMerchant = true;
                    $rootScope.xtoken = response;
                    $location.path("/login");
                    $scope.$apply();
                    $location.replace();
            },
            function (response) {});
    }
	
	$("#mainContainer").hide();
	var success = function (){};
	var failure = function (){};
	if(indexPage == true){
		success = function (response) {
		$("#mainContainer").show();
			$rootScope.isMerchant = response.isMerchant;
			$rootScope.userProfile = response;
			$rootScope.createPage = false;
			$scope.getAllStores($scope, $rootScope);
			$scope.loginGoToTransactions($scope, $location, $rootScope);
		};

		failure = function (response) {
			$("#mainContainer").show();
		};
	}
	callServer("account/profile-info", "", success, failure);
}