/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */

function PasswordChangeCtrl($scope, $rootScope, $location, $route){
    $scope.passwordChange = function (){
        if($("#emailForgotten") && ($("#emailForgotten").val().trim() == "" || ($("#emailForgotten").val().trim() != "" && !isEmail($("#emailForgotten").val())))){
            showAlertBootStrapMsg("warning", $rootScope.resourceBundle.error_invalid_email);
            return;
        }
        var email = $("#emailForgotten").val();
        requestPass($rootScope, $scope, $location, $route, email)
    }
}
PasswordChangeCtrl.$inject = ["$scope", "$rootScope", "$location", "$route"];

function requestPass(rootScope, scope, location, route, email){
    var success = function(response) {
        showAlertBootStrapMsg("success", rootScope.resourceBundle.message_change_password);

        if(indexPage == true)
			navigateToPage(scope, location, rootScope, route, "home");
		if(merchantPage == true || customerPage == true)
			navigateToPage(scope, location, rootScope, route, "login");
    }
    var failure = function(status){
        showAlertBootStrapMsg("warning", rootScope.resourceBundle.error_change_password);
    };
    var dataToSend = "email=" + email;// + "&xtoken=" + rootScope.xtoken;
    callClient("generate-lost-password", dataToSend, success, failure);
}