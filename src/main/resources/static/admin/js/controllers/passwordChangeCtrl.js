function PasswordChangeCtrl($scope, $rootScope, $location ,$route){
    $scope.passwordChange = function (){
        if($("#emailForgotten") && ($("#emailForgotten").val().trim() == "" || ($("#emailForgotten").val().trim() != "" && !isEmail($("#emailForgotten").val())))){
            showAlertBootStrapMsg("warning","Please enter a valid email");
            return;
        }
        var email = $("#emailForgotten").val();
        requestPass($rootScope,$scope,$location, email)
    }
}
PasswordChangeCtrl.$inject = ["$scope", "$rootScope", "$location", "$route"];
function requestPass(rootScope ,scope , location ,email){
    var success = function(response) {
        showAlertBootStrapMsg("success", "A link to choose a new password has been sent to you.");

        location.path( "/home" );
        scope.$apply();
        location.replace();
    }
    var failure = function(status){
        showAlertBootStrapMsg("warning","Error in request password change ");
    };
    var dataToSend = "email=" + email;// + "&xtoken=" + rootScope.xtoken;
    callClient("generate-lost-password", dataToSend, success, failure);
}