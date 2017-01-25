/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */

"use strict";

function MainCtrl(ngI18nResourceBundle, ngI18nConfig, $scope, $rootScope, $location, $route) {
	webshim.polyfill('forms forms-ext');
	$scope.$on('$viewContentLoaded', function() {
		$('body').updatePolyfill();
	});
	$rootScope.location = $location.absUrl().split('#')[0];
	$rootScope.serverUrl = serverUrl;

	$rootScope.mogopayGoToProfile = false;
	if(localStorage.getItem("mogopayGoToProfile") == "true"){
		localStorage.removeItem("mogopayGoToProfile");
		$rootScope.mogopayGoToProfile = true;
	}
	var language = "en";
	if(localStorage.getItem("mogopayDisplayLang") && localStorage.getItem("mogopayDisplayLang") != ""){
		language = localStorage.getItem("mogopayDisplayLang");
		localStorage.removeItem("mogopayDisplayLang");
	}
	if(getHTTPParameter("profile") == "true" || getHTTPParameter("lang") != ""){
		if(getHTTPParameter("profile") == "true")
			localStorage.setItem("mogopayGoToProfile", "true");
		if(getHTTPParameter("lang") != "")
			localStorage.setItem("mogopayDisplayLang", getHTTPParameter("lang").toLowerCase());
		window.location.href = $location.$$absUrl.split("?")[0];
	}

	language = $.inArray(language, ngI18nConfig.supportedLocales) ? language : "en";
	$rootScope.i18n = {language: language};
	$rootScope.$watch("i18n.language", function (language) {
		ngI18nResourceBundle.get({locale: language}).success(function (resourceBundle) {
			$rootScope.resourceBundle = resourceBundle;
			$rootScope.returnItemStatusOptions = [{
				value: "UNDEFINED",
				label: $rootScope.resourceBundle.return_undefined
			},{
				value: "NOT_AVAILABLE",
				label: $rootScope.resourceBundle.return_not_available
			},{
				value: "BACK_TO_STOCK",
				label: $rootScope.resourceBundle.return_back_to_stock
			},{
				value: "DISCARDED",
				label: $rootScope.resourceBundle.return_discarded
			}];

			$rootScope.returnStatusValues = {
				"RETURN_SUBMITTED": $rootScope.resourceBundle.return_submitted,
				"RETURN_TO_BE_RECEIVED": $rootScope.resourceBundle.return_to_be_received,
				"RETURN_RECEIVED": $rootScope.resourceBundle.return_received,
				"RETURN_REFUSED": $rootScope.resourceBundle.return_refused,
				"RETURN_ACCEPTED": $rootScope.resourceBundle.return_accepted
			};

			/*
			 val              = Value("REFUNDED")
			 val       = Value("REFUNDED_FAILED")
			 val        = Value("SHIPMENT_ERROR")

			 */

			$rootScope.transactionStatusValues = [{
				value: "",
				label: $rootScope.resourceBundle.option_all
			},{
				value: "INITIATED",
				label: $rootScope.resourceBundle.transactionStatus_initiated,
			},{
				value: "COMPLETED",
				label: $rootScope.resourceBundle.transactionStatus_completed,
			},{
				value: "FAILED",
				label: $rootScope.resourceBundle.transactionStatus_failed,
			},{
				value: "PAYMENT_AUTHORIZED",
				label: $rootScope.resourceBundle.transactionStatus_payment_authorized,
			},{
				value: "PAYMENT_REFUSED",
				label: $rootScope.resourceBundle.transactionStatus_payment_refused,
			},{
				value: "PAYMENT_FAILED",
				label: $rootScope.resourceBundle.transactionStatus_payment_failed,
			},{
				value: "REFUNDED",
				label: $rootScope.resourceBundle.transactionStatus_refunded,
			},{
				value: "REFUNDED_FAILED",
				label: $rootScope.resourceBundle.transactionStatus_refunded_failed,
			},{
				value: "SHIPMENT_ERROR",
				label: $rootScope.resourceBundle.transactionStatus_shipment_error,
			}];

			$rootScope.deliveryStatusValues = [{
				value: "",
				label: $rootScope.resourceBundle.option_all,
			},{
				value: "NOT_STARTED",
				label: $rootScope.resourceBundle.deliveryStatus_not_started,
			},{
				value: "IN_PROGRESS",
				label: $rootScope.resourceBundle.deliveryStatus_in_progress,
            },{
                value: "DELIVERED",
                label: $rootScope.resourceBundle.deliveryStatus_delivered,
            },{
                value: "ERROR",
                label: $rootScope.resourceBundle.deliveryStatus_errord,
			},{
				value: "RETURNED",
				label: $rootScope.resourceBundle.deliveryStatus_returned,
			},{
				value: "CANCELED",
				label: $rootScope.resourceBundle.deliveryStatus_canceled
			}];
		}).error(function (resourceBundle) {
			$rootScope.i18n = {language: "en"};
			$rootScope.$watch("i18n.language", function (language) {
				ngI18nResourceBundle.get({locale: language}).success(function (resourceBundle) {
					$rootScope.resourceBundle = resourceBundle;
					$rootScope.returnItemStatusOptions = [{
						value: "UNDEFINED",
						label: $rootScope.resourceBundle.return_undefined
					},{
						value: "NOT_AVAILABLE",
						label: $rootScope.resourceBundle.return_not_available
					},{
						value: "BACK_TO_STOCK",
						label: $rootScope.resourceBundle.return_back_to_stock
					},{
						value: "DISCARDED",
						label: $rootScope.resourceBundle.return_discarded
					}];

					$rootScope.returnStatusValues = {
						"RETURN_SUBMITTED": $rootScope.resourceBundle.return_submitted,
						"RETURN_TO_BE_RECEIVED": $rootScope.resourceBundle.return_to_be_received,
						"RETURN_RECEIVED": $rootScope.resourceBundle.return_received,
						"RETURN_REFUSED": $rootScope.resourceBundle.return_refused,
						"RETURN_ACCEPTED": $rootScope.resourceBundle.return_accepted
					};
				});
			});
		});
	});

	$rootScope.createPage = false;
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
		var success = function(response) {
			xtoken = null;
			$rootScope.isMerchant = null;
			$rootScope.userProfile = null;
			$rootScope.transactions = null;
			$rootScope.customers = null;
			if(indexPage == true)
				navigateToPage($scope, $location, $rootScope, $route, "home");
			if(merchantPage == true || customerPage == true)
				navigateToPage($scope, $location, $rootScope, $route, "login");
		}
		callServer("account/logout", "", success, emptyFunc, "GET", "params", "pay", true, true, true);
	};

	$rootScope.isPageActive = function (route) {
		return route === $location.path();
	};

	$rootScope.getAllStores = function () {
		var success = function (response) {
			$scope.$apply(function () {
				$rootScope.allStores = response;
				selectedStore = response[0];
				$rootScope.transactions = null;
				$rootScope.customers = null;
				if($rootScope.mogopayGoToProfile){
					$rootScope.mogopayGoToProfile = false;
					navigateToPage($scope, $location, $rootScope, $route, "profile");
				}
				else
					navigateToPage($scope, $location, $rootScope, $route, "listTransactions");
			});
		};
		callServer("account/list-compagnies", "", success, emptyFunc, "GET", "params", "pay", false, false, false);
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
	callServer("account/confirm-signup", dataToSend, success, error, "GET", "params", "pay", true, false, true);
}

function validationGetUserProfile(scope, location, rootScope, route){
	var success = function(response){
		if(response.isMerchant)
			window.location.href = deployUrl + "merchant.html";
		else
			window.location.href = deployUrl + "customer.html";
	}
	var error = function(response){}
	callServer("account/profile-info", "", success, error, "GET", "params", "pay", true, true, true);
}