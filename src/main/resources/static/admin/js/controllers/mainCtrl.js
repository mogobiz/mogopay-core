"use strict";

function MainCtrl(ngI18nResourceBundle, ngI18nConfig, $scope, $rootScope, $location, $route) {
    webshim.polyfill('forms forms-ext');
    $scope.$on('$viewContentLoaded', function() {
        $('body').updatePolyfill();
    });
    $rootScope.location = $location.absUrl().split('#')[0];
    $rootScope.serverUrl = serverUrl;

	$rootScope.returnItemStatusOptions = [{
		value: "UNDEFINED",
		label: "Undefined"
	},{
		value: "NOT_AVAILABLE",
		label: "Not Available"
	},{
		value: "BACK_TO_STOCK",
		label: "Back To Stock"
	},{
		value: "DISCARDED",
		label: "Discarded"
	}];

	$rootScope.returnStatusValues = {
		"RETURN_SUBMITTED": "Return Submitted",
		"RETURN_TO_BE_RECEIVED": "Return To Be Received",
		"RETURN_RECEIVED": "Return Received",
		"RETURN_REFUSED": "Return Refused",
		"RETURN_ACCEPTED": "Return Accepted"
	};

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
        $rootScope.customers = null;
        $rootScope.createPage = null;
		if(indexPage == true)
			$location.path("/home");
		if(merchantPage == true || customerPage == true)
			$location.path("/login");
        $location.replace();
    };

    $rootScope.isPageActive = function (route) {
        return route === $location.path();
    };

    $rootScope.loginGoToTransactions = function () {
        $rootScope.transactions = null;
        $location.path("/listTransactions");
        $location.replace();
    };
	
	$rootScope.getAllStores = function () {
        var success = function (response) {
			$scope.$apply(function () {
				$rootScope.allStores = response;
				$rootScope.selectedStore = response[0];
				$scope.loginGoToTransactions($scope, $location, $rootScope);
			});
		};
		callServer("account/list-compagnies", "", success, function (response) {}, "GET");
    };
	$scope.urlHistory = [];
	$scope.$on("$routeChangeSuccess", function () {
		if ($location.$$absUrl.split('#')[1] !== $scope.urlHistory[$scope.urlHistory.length - 1] && $location.$$absUrl.split('#')[1] !== "/") {
			$scope.urlHistory.push($location.$$absUrl.split('#')[1]);
		}
	});
	$scope.navigateBack = function(){navigateBack($scope, $rootScope, $location, $route);};
	// window.onbeforeunload = function (e) {
		// try {
			// if (e) {
				// e.returnValue = "This site will be closed!";
				// e.cancelBubble = true;
				// if (e.stopPropagation)
					// e.stopPropagation();
				// if (e.preventDefault)
					// e.preventDefault();
			// }
		// } catch (err) {
			// return "This site will be closed!";
		// }
	// }
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

function navigateBack(scope, rootScope, location, route){
	if(scope.urlHistory.length == "1") {
		return;
	}
	scope.urlHistory.pop();
	location.path(scope.urlHistory[scope.urlHistory.length - 1]);
	if (scope.$root.$$phase != "$apply" && scope.$root.$$phase != "$digest") {
		scope.$apply();
	}
}