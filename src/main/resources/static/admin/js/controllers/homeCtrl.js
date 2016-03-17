/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */

function HomeCtrl($scope, $location, $rootScope, $route) {
    $scope.customerLogin =  function () {
		var success = function (response) {
			$rootScope.xtoken = response;
			$rootScope.isMerchant = false;
			$scope.$apply();
			navigateToPage($scope, $location, $rootScope, $route, "login");
		}
        callServer("account/customer-token", "", success, function (response) {}, "GET", true, true, true);
    }
    $scope.merchantLogin =  function () {
		var success = function (response) {
                $rootScope.xtoken = response;
				$rootScope.isMerchant = true;
				$scope.$apply();
				navigateToPage($scope, $location, $rootScope, $route, "login");
            }
        callServer("account/merchant-token", "", success, function (response) {}, "GET", true, true, true);
    }
	
	var success = function (){};
	var failure = function (){};
	if(indexPage == true){
		success = function (response) {
			$rootScope.isMerchant = response.isMerchant;
			$rootScope.userProfile = response;
			$rootScope.getAllStores();
		};
		failure = function (response) {
			$("#homeContainer").show();
		};
	}
	callServer("account/profile-info", "", success, failure, "GET", false, false, false);
}