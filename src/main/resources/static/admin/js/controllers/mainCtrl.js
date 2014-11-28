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
    }

    $rootScope.logout = function () {
//        callServer("account/logout", "token=" + $rootScope.xtoken + "", function (response) {}, function (response) {});
        callServer("account/logout", "", function (response) {}, function (response) {});
        // var success = function () {
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
//        callServer("account/customer-token", "", function (response) {$rootScope.xtoken = response;}, function (response) {});
        // }
    };

    $rootScope.isPageActive = function (route) {
        return route === $location.path();
    };

    $rootScope.loginGoToTransactions = function (scope, location, rootScope) {
        rootScope.transactions = null;
        location.path("/listTransactions");
        scope.$apply();
        location.replace();
    };

//    callServer("account/customer-token", "", function (response) {$rootScope.xtoken = response;}, function (response) {});
}

MainCtrl.$inject = ["ngI18nResourceBundle", "ngI18nConfig", "$scope", "$rootScope", "$location", "$route"];