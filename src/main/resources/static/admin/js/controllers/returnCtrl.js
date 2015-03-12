function ReturnCtrl($scope, $location, $rootScope, $route) {
	$scope.goToListTrasactions = function () {
        $location.path("/listTransactions");
        $location.replace();
    };
	$scope.goToListCustomers = function () {
        $location.path("/listCustomers");
        $location.replace();
    };
    $scope.goToProfile = function () {
        var success = function (response) {
            $rootScope.userProfile = response;
            $location.path("/profile");
            $scope.$apply();
            $location.replace();
        };
        callServer("account/profile-info", "", success, function (response) {});
    };
	$scope.returnItemStatusChanged = function (index) {returnItemStatusChanged($scope, $location, $rootScope, $route, index);};
	$scope.returnItemStatusOptions = [{
		value: "NOT_AVAILABLE",
		label: "Not Available"
	},{
		value: "BACK_TO_STOCK",
		label: "Back To Stock"
	},{
		value: "DISCARDED",
		label: "Discarded"
	}];
	$scope.returnStatusOptions = [{
		value: "RETURN_SUBMITTED",
		label: "Return Submitted"
	},{
		value: "RETURN_TO_BE_RECEIVED",
		label: "Return To Be Received"
	},{
		value: "RETURN_RECEIVED",
		label: "Return Received"
	},{
		value: "RETURN_REFUSED",
		label: "Return Refused"
	},{
		value: "RETURN_ACCEPTED",
		label: "Return Accepted"
	}];
	$scope.returnStatusTab = [];
	$scope.returnItemStatusTab = [];
	for(var i = 0; i < $rootScope.returnDetails.returnedItems.length; i++){
		var item = $rootScope.returnDetails.returnedItems[i];
		$scope.returnItemStatusTab[$scope.returnItemStatusTab.length] = item.BOReturn.status;
		$scope.returnStatusTab[$scope.returnStatusTab.length] = item.status;
	}
	console.log($scope.returnItemStatusTab);
	console.log($scope.returnStatusTab);
}

function returnItemStatusChanged(scope, location, rootScope, route, index){
	alert(scope.returnStatusTab[index])
}