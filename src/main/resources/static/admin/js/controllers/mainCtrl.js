"use strict";

function MainCtrl(ngI18nResourceBundle, ngI18nConfig, $scope, $rootScope, $location, $route) {
    webshim.polyfill('forms forms-ext');
    $scope.$on('$viewContentLoaded', function() {
        $('body').updatePolyfill();
    });

    $rootScope.location = $location.absUrl().split('#')[0];

    $rootScope.serverUrl = serverUrl;

    if (($rootScope.userProfile == undefined || $rootScope.userProfile == null)) {
        if(indexPage == true && $location.$$path != "/home"){
			$location.path("/home");
			$location.replace();
		}
		if((merchantPage == true || customerPage == true) && $location.$$path != "/login"){
			$location.path("/login");
			$location.replace();
		}
		if(validationPage == true){
			$scope.validationInProgress = true;
			$scope.validationSuccess = false;
			$scope.validationError = false;

			$scope.token = "";
			if(getHTTPParameter("token")){
				$scope.token = getHTTPParameter("token");
			}
			validationConfirmSignUp($scope, $location, $rootScope, $route);
		}
    }

    $rootScope.logout = function () {
        callServer("account/logout", "", function (response) {}, function (response) {});
        $rootScope.xtoken = null;
        $rootScope.isMerchant = null;
        $rootScope.userProfile = null;
        $rootScope.transactions = null;
        $rootScope.transactionLogs = null;
        $rootScope.createPage = null;
		if(indexPage == true)
			$location.path("/home");
		if(merchantPage == true || customerPage == true)
			$location.path("/login");
        $scope.$apply();
        $location.replace();
    };

    $rootScope.isPageActive = function (route) {
        return route === $location.path();
    };

    $rootScope.loginGoToTransactions = function () {
        $rootScope.transactions = null;
        $location.path("/listTransactions");
        $scope.$apply();
        $location.replace();
    };
	
	$rootScope.getAllStores = function () {
        var success = function (response) {
			$scope.$apply(function () {
				$rootScope.allStores = response;
				$rootScope.selectedStore = response[0];
			});
		};
		callServer("account/list-compagnies", "", success, function (response) {}, "GET");
    };
}

MainCtrl.$inject = ["ngI18nResourceBundle", "ngI18nConfig", "$scope", "$rootScope", "$location", "$route"];

function validationConfirmSignUp(scope, location, rootScope, route){
	var dataToSend = "token=" + scope.token;
	var success = function(response){
		scope.validationInProgress = false;
		scope.validationSuccess = true;
		scope.validationError = false;
		scope.$apply();
		validationGetUserProfile(scope, location, rootScope, route);
	}
	var error = function(response){
		scope.validationInProgress = false;
		scope.validationSuccess = false;
		scope.validationError = true;
		scope.$apply();
	}
	callServer("account/confirm-signup", dataToSend, success, error);
}

function validationGetUserProfile(scope, location, rootScope, route){
	var success = function(response){
		if(response.isMerchant)
			window.location.href = deployUrl + "merchant.html";
		else
			window.location.href = deployUrl + "customer.html";
	}
	var error = function(response){}
	callServer("account/profile-info", "", success, error);
}