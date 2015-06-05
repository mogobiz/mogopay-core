function LoginCtrl($scope, $location, $rootScope ,$route) {
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
            showAlertBootStrapMsg("warning", "Please fill all required fields.");
            return;
        }
        var success = function (response) {
			var infoSuccess = function(infoResponse){
				$rootScope.userProfile = infoResponse;
				$rootScope.getAllStores();
			}
			callServer("account/profile-info", "", infoSuccess, function(){});
        };

        var error = function (response) {
            showAlertBootStrapMsg("warning", "Invaild username or password.");
        };

        var dataToSend = "";
        dataToSend += "&email=" + $("#user_email").val();
        dataToSend += "&password=" + $("#user_password").val();
        dataToSend += "&is_customer=" + !$rootScope.isMerchant;
		if(!$rootScope.isMerchant)
			dataToSend += "&merchant_id=" + $scope.loginSelectedSeller;

        postOnServer("account/login", dataToSend, success, error);
    };

    $scope.requestPasswordChange =  function () {
        navigateToPage($scope, $location, $rootScope, $route, "passwordChange");
    }

	if(merchantPage == true)
		$rootScope.isMerchant = true;
	if(customerPage == true)
		$rootScope.isMerchant = false;
	$("#mainContainer").hide();

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
		var success = function (){};
		var failure = function (){};
		if(indexPage == true){
			success = function (response) {
			$("#mainContainer").show();
				$rootScope.userProfile = response;
				$rootScope.getAllStores();
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
							$rootScope.xtoken = response;
						},
							function (response) {
						});
					}, function (response) {});
				}
				else{
					$rootScope.userProfile = response;
					$rootScope.getAllStores();
				}
			};

			failure = function (response) {
				$("#mainContainer").show();
				callServer("account/merchant-token", "",
					function (response) {
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
							$rootScope.xtoken = response;
						},
							function (response) {
						});
					}, function (response) {});
				}
				else{
					$rootScope.userProfile = response;
					$rootScope.getAllStores();
				}
			};
			failure = function (response) {
				$("#mainContainer").show();
				callServer("account/customer-token", "",
					function (response) {
						$rootScope.xtoken = response;
					},
					function (response) {
				});
			};
		}
		callServer("account/profile-info", "", success, failure);
	}
	callServer("account/list-merchants", "", listSuccess, function (response) {}, "GET");
}