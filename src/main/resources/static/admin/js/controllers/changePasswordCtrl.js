/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */

function ChangePasswordCtrl($scope, $location, $rootScope, $route){
	$scope.changePasswordSave = function (){changePasswordSave($scope, $location, $rootScope, $route)};
}

function changePasswordSave(scope, location, rootScope, route, email){
	if($("#changePasswordCurrent").val().trim() == "" || $("#changePasswordNew").val().trim() == "" || $("#changePasswordConfirm").val().trim() == ""){
		showAlertBootStrapMsg("warning", $rootScope.resourceBundle.error_required);
		return;
	}
	if($("#changePasswordNew").val() != $("#changePasswordConfirm").val()){
		showAlertBootStrapMsg("warning", $rootScope.resourceBundle.error_password_match);
		return;
	}

	var success = function(response) {
		showAlertBootStrapMsg("success", rootScope.resourceBundle.message_change_password);
		navigateToPage(scope, location, rootScope, route, "profile");
	}
	var error = function(status){
		showAlertBootStrapMsg("warning", rootScope.resourceBundle.error_change_password);
	};
	var dataToSend = "current_password=" + $("#changePasswordCurrent").val() + "&new_password=" + $("#changePasswordNew").val();
	callServer("account/update-password", dataToSend, success, error, "GET", "params", "pay", true, false, true);
}