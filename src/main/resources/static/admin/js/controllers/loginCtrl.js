function LoginCtrl($scope, $location, $rootScope ,$route) {
    $scope.signup = function () {
        $rootScope.createPage = true;
        $rootScope.userProfile = null;
        $location.path("/signup");
        $location.replace();
    };

    $scope.goToProfile = function () {
        $rootScope.createPage = true;
        $rootScope.userProfile = null;
        $location.path("/profile");
        $location.replace();
    };

    $scope.logInBykey = function (e) {
        if(e.keyCode==13) {
            $scope.Login();
        }
    };

    $scope.Login =  function () {
        if($("#user_email").val() == "" || $("#user_password").val() == "") {
            showAlertBootStrapMsg("warning", "Please fill all required fields.");
            return;
        }

        var success = function (response) {
            $rootScope.createPage = false;
			$scope.getAllStores($scope, $rootScope);
            $scope.loginGoToTransactions($scope, $location, $rootScope);
        };

        var error = function (response) {
            $rootScope.createPage = false;
            showAlertBootStrapMsg("warning", "Invaild username or password.");
        };

        var dataToSend = "";
        dataToSend += "&email=" + $("#user_email").val();
        dataToSend += "&password=" + $("#user_password").val();
        if ($("#merchant_id").val() != undefined && $("#merchant_id").val() != "")
            dataToSend += "&merchant_id=" + $("#merchant_id").val();
        dataToSend += "&is_customer=" + !$rootScope.isMerchant;

        postOnServer("account/login", dataToSend, success, error);
    };

    $scope.requestPasswordChange =  function () {
        $location.path("/passwordChange");
        $location.replace();
    }
	
	$("#mainContainer").hide();
	var success = function (){};
	var failure = function (){};
	if(indexPage == true){
		success = function (response) {
		$("#mainContainer").show();
			$rootScope.isMerchant = response.isMerchant;
			$rootScope.userProfile = response;
			$rootScope.createPage = false;
			$scope.getAllStores($scope, $rootScope);
			$scope.loginGoToTransactions($scope, $location, $rootScope);
		};

		failure = function (response) {
			$("#mainContainer").show();
		};
	}
	if(merchantPage == true){
		success = function (response) {
			$("#mainContainer").show();
			if(!response.isMerchant){
				callServer("account/logout", "", function (response) {
					callServer("account/merchant-token", "",
					function (response) {
						$rootScope.isMerchant = true;
						$rootScope.xtoken = response;
					},
						function (response) {
					});
				}, function (response) {});
			}
			else{
				$rootScope.isMerchant = response.isMerchant;
				$rootScope.userProfile = response;
				$rootScope.createPage = false;
				$scope.getAllStores($scope, $rootScope);
				$scope.loginGoToTransactions($scope, $location, $rootScope);
			}
		};

		failure = function (response) {
			$("#mainContainer").show();
			callServer("account/merchant-token", "",
				function (response) {
					$rootScope.isMerchant = true;
					$rootScope.xtoken = response;
				},
				function (response) {
			});
		};
	}
	if(customerPage == true){
		success = function (response) {
			$("#mainContainer").show();
			if(response.isMerchant){
				callServer("account/logout", "", function (response) {
					callServer("account/customer-token", "",
					function (response) {
						$rootScope.isMerchant = false;
						$rootScope.xtoken = response;
					},
						function (response) {
					});
				}, function (response) {});
			}
			else{
				$rootScope.isMerchant = response.isMerchant;
				$rootScope.userProfile = response;
				$rootScope.createPage = false;
				$scope.getAllStores($scope, $rootScope);
				$scope.loginGoToTransactions($scope, $location, $rootScope);
			}
		};
		failure = function (response) {
			$("#mainContainer").show();
			callServer("account/customer-token", "",
				function (response) {
					$rootScope.isMerchant = false;
					$rootScope.xtoken = response;
				},
				function (response) {
			});
		};
	}
	callServer("account/profile-info", "", success, failure);
}
