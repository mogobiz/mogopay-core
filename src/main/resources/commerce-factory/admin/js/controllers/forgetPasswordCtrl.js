/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */

function ForgetPasswordCtrl($scope, $location, $rootScope, $route){
	$scope.forgettenSelectedSeller = $rootScope.allSellers[0].id;
    $scope.sendNewPassword = function (){sendNewPassword($scope, $location, $rootScope, $route)};
}

function sendNewPassword(scope, location, rootScope, route){
	if($("#emailForgotten") && ($("#emailForgotten").val().trim() == "" || ($("#emailForgotten").val().trim() != "" && !isEmail($("#emailForgotten").val())))){
		showAlertBootStrapMsg("warning", rootScope.resourceBundle.error_invalid_email);
		return;
	}
    var success = function(response) {
        showAlertBootStrapMsg("success", rootScope.resourceBundle.message_new_password);
        if(indexPage == true)
			navigateToPage(scope, location, rootScope, route, "home");
		if(merchantPage == true || customerPage == true)
			navigateToPage(scope, location, rootScope, route, "login");
    }
    var error = function(status){
        showAlertBootStrapMsg("warning", rootScope.resourceBundle.error_change_password);
    };
    var data = {email: $("#emailForgotten").val()};
		if(!rootScope.isMerchant)
			data.merchantId = scope.forgettenSelectedSeller;
    callServer("account/send-new-password", data, success, error, "POST", "params", "pay", true, true, true);
}