/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */

function HomeCtrl($scope, $location, $rootScope, $route) {
    $scope.customerLogin =  function () {
		var success = function (response) {
			$rootScope.isMerchant = false;
			xtoken = response.token;
			$scope.$apply();
			navigateToPage($scope, $location, $rootScope, $route, "login");
		}
        callServer("account/customer-token", "", success, emptyFunc, "GET", "params", "pay", true, true, true);
    }
    $scope.merchantLogin =  function () {
		var success = function (response) {
				$rootScope.isMerchant = true;
				xtoken = response.token;
				$scope.$apply();
				navigateToPage($scope, $location, $rootScope, $route, "login");
            }
        callServer("account/merchant-token", "", success, emptyFunc, "GET", "params", "pay", true, true, true);
    }
	
	var success = function (){};
	var error = function (){};
	if(indexPage == true){
		success = function (response) {
			$rootScope.isMerchant = response.isMerchant;
			$rootScope.userProfile = response;
			$rootScope.getAllStores();
		};
		error = function (response) {
			$("#homeContainer").show();
		};
	}
	callServer("account/profile-info", "", success, error, "GET", "params", "pay", false, false, false);
}