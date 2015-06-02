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

    $rootScope.mogopayGoToProfile = false;
	if(localStorage.getItem("mogopayGoToProfile") == "true"){
		localStorage.removeItem("mogopayGoToProfile");
		$rootScope.mogopayGoToProfile = true;
	}
	if(getHTTPParameter("profile") == "true"){
		localStorage.setItem("mogopayGoToProfile", "true");
		window.location.href = $location.$$absUrl.split("?")[0];
	}
	if ($rootScope.userProfile == undefined || $rootScope.userProfile == null) {
        if(indexPage == true && $location.$$path != "/home"){
			navigateToPage($scope, $location, $rootScope, $route, "home");
		}
		if((merchantPage == true || customerPage == true) && $location.$$path != "/login"){
			navigateToPage($scope, $location, $rootScope, $route, "login");
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
			navigateToPage($scope, $location, $rootScope, $route, "home");
		if(merchantPage == true || customerPage == true)
			navigateToPage($scope, $location, $rootScope, $route, "login");
    };

    $rootScope.isPageActive = function (route) {
        return route === $location.path();
    };

	$rootScope.getAllStores = function () {
        var success = function (response) {
            $scope.$apply(function () {
				$rootScope.allStores = response;
				$rootScope.selectedStore = response[0];
				$rootScope.transactions = null;
				if($rootScope.mogopayGoToProfile){
					$rootScope.mogopayGoToProfile = false;
					navigateToPage($scope, $location, $rootScope, $route, "profile");
				}
				else
					navigateToPage($scope, $location, $rootScope, $route, "listTransactions");
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

	$scope.navigateToPage = function(page){navigateToPage($scope, $location, $rootScope, $route, page);};
	$scope.navigateBack = function(){navigateBack($scope, $location, $rootScope, $route);};
}

MainCtrl.$inject = ["ngI18nResourceBundle", "ngI18nConfig", "$scope", "$rootScope", "$location", "$route"];

function navigateToPage(scope, location, rootScope, route, page){
	if(location.path() == "/" + page){
		return;
	}
	try {window.history.pushState({}, "", window.location.href);}
	catch (e) {console.log(e);}
	location.path("/" + page);
	location.replace();
	if (scope.$root.$$phase != "$apply" && scope.$root.$$phase != "$digest") {
		scope.$apply();
	}
}

function navigateBack(scope, location, rootScope, route){
	if(scope.urlHistory.length == "1") {
		return;
	}
	try {window.history.pushState({}, "", window.location.href);}
	catch (e) {console.log(e);}
	scope.urlHistory.pop();
	location.path(scope.urlHistory[scope.urlHistory.length - 1]);
	location.replace();
	if (scope.$root.$$phase != "$apply" && scope.$root.$$phase != "$digest") {
		scope.$apply();
	}
}

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