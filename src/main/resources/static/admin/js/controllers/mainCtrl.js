﻿/*
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

	var language = getHTTPParameter("lang") ? getHTTPParameter("lang").toLowerCase() : "en";
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
				"RETURN_SUBMITTED": $rootScope.resourceBundle.return_return_submitted,
				"RETURN_TO_BE_RECEIVED": $rootScope.resourceBundle.return_return_to_be_received,
				"RETURN_RECEIVED": $rootScope.resourceBundle.return_return_received,
				"RETURN_REFUSED": $rootScope.resourceBundle.return_return_refused,
				"RETURN_ACCEPTED": $rootScope.resourceBundle.return_return_accepted
			};
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
						"RETURN_SUBMITTED": $rootScope.resourceBundle.return_return_submitted,
						"RETURN_TO_BE_RECEIVED": $rootScope.resourceBundle.return_return_to_be_received,
						"RETURN_RECEIVED": $rootScope.resourceBundle.return_return_received,
						"RETURN_REFUSED": $rootScope.resourceBundle.return_return_refused,
						"RETURN_ACCEPTED": $rootScope.resourceBundle.return_return_accepted
					};
				});
			});
		});
	});

	$rootScope.createPage = false;
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
				$rootScope.customers = null;
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