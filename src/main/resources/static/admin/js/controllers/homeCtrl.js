function HomeCtrl($scope, $location, $rootScope, $route) {
    $scope.customerLogin =  function () {
        callServer("account/customer-token", "", function (response) {
                $rootScope.xtoken = response;
                var success = function (response) {
                    $rootScope.isMerchant = false;
                    $rootScope.xtoken = response;
                    $location.path("/login");
                    $scope.$apply();
                    $location.replace();
                };
                callServer("account/customer-token", "", success, function (response) {});
            },
            function (response) {});

    }
    $scope.merchantLogin =  function () {
        callServer("account/customer-token", "",
            function (response) {
                $rootScope.xtoken = response;
                var success = function (response) {
                    $rootScope.isMerchant = true;
                    $rootScope.xtoken = response;
                    $location.path("/login");
                    $scope.$apply();
                    $location.replace();
                };
                callServer("account/merchant-token", "token=" + $rootScope.xtoken.token, success, function (response) {});
            },
            function (response) {});
    }
}