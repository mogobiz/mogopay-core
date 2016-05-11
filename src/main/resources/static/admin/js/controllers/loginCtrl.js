/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */

function LoginCtrl($scope, $location, $rootScope, $route) {
    $scope.signup = function () {
        $rootScope.createPage = true;
        $rootScope.userProfile = null;
        navigateToPage($scope, $location, $rootScope, $route, "signup");
    };

    $scope.logInBykey = function (e) {
        if(e.keyCode==13) {
            $scope.Login();
        }
    };

    $scope.Login =  function () {
        if($("#user_email").val() == "" || $("#user_password").val() == "") {
            showAlertBootStrapMsg("warning", $rootScope.resourceBundle.error_required);
            return;
        }
        var success = function (response) {
			var infoSuccess = function(infoResponse){
				$rootScope.userProfile = infoResponse;
				$rootScope.getAllStores();
			}
			callServer("account/profile-info", "", infoSuccess, emptyFunc, "GET", "params", "pay", false, true, true);
        };

        var error = function (response) {
            showAlertBootStrapMsg("warning", $rootScope.resourceBundle.error_invalid_credentials);
        };

        var dataToSend = "";
        dataToSend += "&email=" + $("#user_email").val();
        dataToSend += "&password=" + $("#user_password").val();
        dataToSend += "&is_customer=" + !$rootScope.isMerchant;
		if(!$rootScope.isMerchant)
			dataToSend += "&merchant_id=" + $scope.loginSelectedSeller;

        callServer("account/login", dataToSend, success, error, "POST", "params", "pay", true, false, true);
    };

    $scope.requestNewPassword =  function () {
        navigateToPage($scope, $location, $rootScope, $route, "forgetPassword");
    }

	if(merchantPage == true)
		$rootScope.isMerchant = true;
	if(customerPage == true)
		$rootScope.isMerchant = false;

	if($rootScope.allSellers && $rootScope.allSellers.length > 0){
		$scope.loginSelectedSeller = $rootScope.allSellers[0].id;
		loginGetProfileInfo($scope, $location, $rootScope, $route);
		return;
	}
	var listSuccess = function(response){
		$scope.$apply(function () {
			var allSellers = [];
			for(var i = 0; i < response.length; i++){
				for(var key in response[i]){
					allSellers[allSellers.length] = {name: key, id: response[i][key]};
				}
			}
			$rootScope.allSellers = allSellers;
			$scope.loginSelectedSeller = $rootScope.allSellers[0].id;
		});
		loginGetProfileInfo($scope, $location, $rootScope, $route);
	}
	callServer("account/list-merchants", "", listSuccess, emptyFunc, "GET", "params", "pay", true, true, true);
}

function loginGetProfileInfo(scope, location, rootScope, route){
	var success = function (){};
	var error = function (){};
	if(indexPage == true){
		success = function (response) {
			rootScope.userProfile = response;
			rootScope.getAllStores();
		};

		error = function (response) {
			$("#loginContainer").show();
		};
	}
	if(merchantPage == true){
		success = function (response) {
			if(!response.isMerchant){
				var logoutSuccess = function (response) {
					$("#loginContainer").show();
					callServer("account/merchant-token", "", function (response) {xtoken = response.token;}, emptyFunc, "GET", "params", "pay", false, false, false);
				}
				callServer("account/logout", "", logoutSuccess, emptyFunc, "GET", "params", "pay", false, false, false);
			}
			else{
				rootScope.userProfile = response;
				rootScope.getAllStores();
			}
		};

		error = function (response) {
			$("#loginContainer").show();
			callServer("account/merchant-token", "", function (response){xtoken = response.token;}, emptyFunc, "GET", "params", "pay", false, false, false);
		};
	}
	if(customerPage == true){
		success = function (response) {
			if(response.isMerchant){
				var logoutSuccess = function (response) {
					$("#loginContainer").show();
					callServer("account/customer-token", "", function (response) {xtoken = response.token;}, emptyFunc, "GET", "params", "pay", false, false, false);
				}
				callServer("account/logout", "", logoutSuccess, emptyFunc, "GET", "params", "pay", false, false, false);
			}
			else{
				rootScope.userProfile = response;
				rootScope.getAllStores();
			}
		};
		error = function (response) {
			$("#loginContainer").show();
			callServer("account/customer-token", "", function (response) {xtoken = response.token;}, emptyFunc, "GET", "params", "pay", false, false, false);
		};
	}
	callServer("account/profile-info", "", success, error, "GET", "params", "pay", false, false, false);
}