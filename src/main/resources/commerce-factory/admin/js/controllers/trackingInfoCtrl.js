/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */

function TrackingInfoCtrl($scope, $location, $rootScope, $route) {
	$("html,body").animate({scrollTop: 0}, 0);
	if(!isConnectedUser($scope, $location, $rootScope, $route))
		return;
    $scope.goToListTrasactions = function () {
        navigateToPage($scope, $location, $rootScope, $route, "listTransactions");
    };
	$scope.goToListCustomers = function () {
        navigateToPage($scope, $location, $rootScope, $route, "listCustomers");
    };
    $scope.goToProfile = function () {
        var success = function (response) {
            $rootScope.userProfile = response;
            $scope.$apply();
            navigateToPage($scope, $location, $rootScope, $route, "profile");
        };
        callServer("account/profile-info", "", success, emptyFunc, "GET", "params", "pay", true, false, true);
    };
}