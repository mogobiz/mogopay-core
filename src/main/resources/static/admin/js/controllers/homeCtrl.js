function HomeCtrl($scope, $location, $rootScope, $route) {
    $scope.customerLogin =  function () {
        callServer("account/customer-token", "", function (response) {
                $rootScope.xtoken = response;
                    $rootScope.isMerchant = false;
                    $rootScope.xtoken = response;
                    $scope.$apply();
                    navigateToPage($scope, $location, $rootScope, $route, "login");
            },
            function (response) {});
    }
    $scope.merchantLogin =  function () {
        callServer("account/merchant-token", "",
            function (response) {
                $rootScope.xtoken = response;
                    $rootScope.isMerchant = true;
                    $rootScope.xtoken = response;
                    $scope.$apply();
                    navigateToPage($scope, $location, $rootScope, $route, "login");
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
			$rootScope.getAllStores();
		};

		failure = function (response) {
			$("#mainContainer").show();
		};
	}
	callServer("account/profile-info", "", success, failure);
}