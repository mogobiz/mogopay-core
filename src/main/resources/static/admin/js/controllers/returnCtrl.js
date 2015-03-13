function ReturnCtrl($scope, $location, $rootScope, $route) {
	$scope.goToListTrasactions = function () {
        $location.path("/listTransactions");
        $location.replace();
    };
	$scope.goToListCustomers = function () {
        $location.path("/listCustomers");
        $location.replace();
    };
	$scope.goToDetails = function () {
        $location.path("/details");
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
// Merchant Functions
	$scope.returnItemStatusTab = [];
	if($rootScope.isMerchant){
		for(var i = 0; i < $rootScope.returnDetails.returnedItems.length; i++){
			var item = $rootScope.returnDetails.returnedItems[i];
			$scope.returnItemStatusTab[$scope.returnItemStatusTab.length] = item.status;
		}
	}
	$scope.accpetReturn = function (index) {accpetReturn($scope, $location, $rootScope, $route, index);};
	$scope.refuseReturn = function (index) {refuseReturn($scope, $location, $rootScope, $route, index);};
	$scope.recieveReturn = function (index) {recieveReturn($scope, $location, $rootScope, $route, index);};

// Customer Functions
	$scope.submitReturnItems = function () {submitReturnItems($scope, $location, $rootScope, $route);};
}

// Merchant Functions
function accpetReturn(scope, location, rootScope, route, index){
	if(validateRetunForm(scope, location, rootScope, route, index)){
		//TODO
	}
}

function refuseReturn(scope, location, rootScope, route, index){
	if(validateRetunForm(scope, location, rootScope, route, index)){
		//TODO
	}
}

function recieveReturn(scope, location, rootScope, route, index){
	if(validateRetunForm(scope, location, rootScope, route, index)){
		//TODO
	}
}

function validateRetunItemsForms(scope, location, rootScope, route, index){
	var item = returnDetails.returnedItems[index];
	if(item.BOReturn[0].status == 'RETURN_SUBMITTED' || item.BOReturn[0].status == 'RETURN_RECEIVED'){
		if($("#returnMotivation-" + i).val() == ""){
			$("#returnMotivation-" + i).focus();
			showAlertBootStrapMsg("warning", "Motivation is required!");
			return false;
		}
	}
	if(item.BOReturn[0].status == 'RETURN_RECEIVED'){
		if($("#returnRefunded-" + i).val() == ""){
			$("#returnRefunded-" + i).focus();
			showAlertBootStrapMsg("warning", "Refunded is required!");
			return false;
		}
		if(!$("#returnRefunded-" + i)[0].checkValidity()){
			$("#returnRefunded-" + i).focus();
			showAlertBootStrapMsg("warning", "Invalid refunded!");
			return false;
		}
		if($("#returnTotalRefunded-" + i).val() == ""){
			$("#returnTotalRefunded-" + i).focus();
			showAlertBootStrapMsg("warning", "Refunded is required!");
			return false;
		}
		if(!$("#returnTotalRefunded-" + i)[0].checkValidity()){
			$("#returnTotalRefunded-" + i).focus();
			showAlertBootStrapMsg("warning", "Invalid refunded!");
			return false;
		}
	}
	return true;
}

// Customer Functions
function submitReturnItems(scope, location, rootScope, route){
	if(validateRetunItemsForms(scope, location, rootScope, route)){
		var data = [];
		for(var i = 0; i < rootScope.itemsToBeReturned.length; i++){
			data[data.length] = {
				id: rootScope.itemsToBeReturned[i].bOProducts[0].product.uuid,
				quantity: $("#toBeReturnedQuantity-" + i).val(),
				motivation: $("#toBeReturnedMotivation-" + i).val()
			}
		}
	}
	// TODO
}

function validateRetunItemsForms(scope, location, rootScope, route){
	for(var i = 0; i < rootScope.itemsToBeReturned.length; i++){
		if($("#toBeReturnedQuantity-" + i).val() == ""){
			$("#toBeReturnedQuantity-" + i).focus();
			showAlertBootStrapMsg("warning", "Quantity is required!");
			return false;
		}
		if(!$("#toBeReturnedQuantity-" + i)[0].checkValidity()){
			$("#toBeReturnedQuantity-" + i).focus();
			showAlertBootStrapMsg("warning", "Quantity is required!");
			return false;
		}
		if(parseInt($("#toBeReturnedQuantity-" + i).val()) > rootScope.itemsToBeReturned[i].quantity){
			$("#toBeReturnedQuantity-" + i).focus();
			showAlertBootStrapMsg("warning", "Invalid quantity!");
			return false;
		}
		if($("#toBeReturnedMotivation-" + i).val() == ""){
			$("#toBeReturnedMotivation-" + i).focus();
			showAlertBootStrapMsg("warning", "Motivation is required!");
			return false;
		}
	}
	return true;
}