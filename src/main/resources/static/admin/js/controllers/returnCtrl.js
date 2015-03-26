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
	$scope.refundedValue = [];
	$scope.totalRefundedValue = [];
	$scope.returnItemStatusTab = [];
	if($rootScope.isMerchant){
		for(var i = 0; i < $rootScope.returnDetails.returnedItems.length; i++){
			var item = $rootScope.returnDetails.returnedItems[i];
			$scope.returnItemStatusTab[$scope.returnItemStatusTab.length] = item.status;
			$scope.refundedValue[$scope.refundedValue.length] = item.refunded;
			$scope.totalRefundedValue[$scope.totalRefundedValue.length] = item.totalRefunded;
		}
	}
	$scope.acceptReturn = function (index) {acceptReturn($scope, $location, $rootScope, $route, index);};
	$scope.refuseReturn = function (index) {refuseReturn($scope, $location, $rootScope, $route, index);};
	$scope.receiveReturn = function (index) {receiveReturn($scope, $location, $rootScope, $route, index);};
	$scope.checkTotalRefundedValidity = function (index) {checkTotalRefundedValidity($scope, $location, $rootScope, $route, index);};

// Customer Functions
	$scope.submitReturnItems = function () {submitReturnItems($scope, $location, $rootScope, $route);};
}

// Merchant Functions
function acceptReturn(scope, location, rootScope, route, index){
	if(validateRetunForm(scope, location, rootScope, route, index)){
		if(rootScope.returnDetails.returnedItems[index].boReturns[0].status == "RETURN_SUBMITTED")
			updateReturnItem(scope, location, rootScope, route, index, "RETURN_TO_BE_RECEIVED");
		else
			updateReturnItem(scope, location, rootScope, route, index, "RETURN_ACCEPTED");
	}
}

function refuseReturn(scope, location, rootScope, route, index){
	if(validateRetunForm(scope, location, rootScope, route, index)){
		updateReturnItem(scope, location, rootScope, route, index, "RETURN_REFUSED");
	}
}

function receiveReturn(scope, location, rootScope, route, index){
	if(validateRetunForm(scope, location, rootScope, route, index)){
		updateReturnItem(scope, location, rootScope, route, index, "RETURN_RECEIVED");
	}
}

function updateReturnItem(scope, location, rootScope, route, index, returnStatus){
	var data = {
		status: scope.returnItemStatusTab[index],
		refunded: parseInt(parseFloat($("#returnRefunded-" + index).val())),
		totalRefunded: parseInt(parseFloat($("#returnTotalRefunded-" + index).val())),
		returnStatus: returnStatus,
		motivation: $("#returnMotivation-" + index).val()
	}
	callStoreServerJson(
		"backoffice/cartDetails/" + rootScope.selectedTransaction.uuid + "/" + rootScope.returnDetails.cartItem.uuid + "/" + rootScope.returnDetails.returnedItems[index].uuid,
		data,
		function () {scope.goToDetails();scope.$apply();},
		function () {},
		rootScope.selectedStore,
		"PUT"
	);
}

function validateRetunForm(scope, location, rootScope, route, index){
	var item = rootScope.returnDetails.returnedItems[index];
	if($("#returnMotivation-" + index).val() == ""){
		$("#returnMotivation-" + index).focus();
		showAlertBootStrapMsg("warning", "Motivation is required!");
		return false;
	}
	if(item.boReturns[0].status == 'RETURN_RECEIVED'){
		if($("#returnRefunded-" + index).val() == ""){
			$("#returnRefunded-" + index).focus();
			showAlertBootStrapMsg("warning", "Refunded is required!");
			return false;
		}
		if(!$("#returnRefunded-" + index)[0].checkValidity()){
			$("#returnRefunded-" + index).focus();
			showAlertBootStrapMsg("warning", "Invalid refunded!");
			return false;
		}
		if($("#returnTotalRefunded-" + index).val() == ""){
			$("#returnTotalRefunded-" + index).focus();
			showAlertBootStrapMsg("warning", "Refunded is required!");
			return false;
		}
		if(!$("#returnTotalRefunded-" + index)[0].checkValidity()){
			$("#returnTotalRefunded-" + index).focus();
			showAlertBootStrapMsg("warning", "Invalid refunded!");
			return false;
		}
	}
	return true;
}

function checkTotalRefundedValidity(scope, location, rootScope, route, index){
	var refunded = 0, totalRefunded = 0;
	try{refunded = parseFloat($("#returnRefunded-" + index).val());} catch(e){};
	try{totalRefunded = parseFloat($("#returnTotalRefunded-" + index).val());} catch(e){};
	var quantity = parseFloat($("#returnQuantity-" + index).val());

	if(totalRefunded < (quantity * refunded)){
		$("#returnTotalRefunded-" + index).val(quantity * refunded);
	}
}

// Customer Functions
function submitReturnItems(scope, location, rootScope, route){
	if(validateRetunItemsForms(scope, location, rootScope, route)){
		for(var i = 0; i < rootScope.itemsToBeReturned.length; i++){
			var data = {
				quantity: parseInt(parseFloat($("#toBeReturnedQuantity-" + i).val())),
				motivation: $("#toBeReturnedMotivation-" + i).val()
			}
			callStoreServerJson(
				"backoffice/cartDetails/" + rootScope.selectedTransaction.uuid + "/" + rootScope.itemsToBeReturned[i].uuid,
				data,
				function () {scope.goToDetails();scope.$apply();},
				function () {},
				rootScope.selectedStore,
				"POST"
			);
		}
	}
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