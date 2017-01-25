/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */

function DetailsCtrl($scope, $location, $rootScope, $route) {
	if(!isConnectedUser($scope, $location, $rootScope, $route))
		return;
    $scope.goToListTrasactions = function () {
        navigateToPage($scope, $location, $rootScope, $route, "listTransactions");
    };
	$scope.goToListCustomers = function () {
        navigateToPage($scope, $location, $rootScope, $route, "listCustomers");
    };
    $scope.goToProfile = function () {
        var success = function (response) {
            $rootScope.userProfile = response;
            $scope.$apply();
            navigateToPage($scope, $location, $rootScope, $route, "profile");
        };
        callServer("account/profile-info", "", success, emptyFunc, "GET", "params", "pay", true, false, true);
    };
	$scope.historyDetails = null;
	$scope.shippingDetails = null;
	$scope.shippingProviderDetails = "";
	$scope.shippingAddressDetails = "";
	$scope.customerAddress = "";
	$rootScope.returnDetails = null;
	$rootScope.logsDetails = null;
	$rootScope.itemsToBeReturned = [];
	$rootScope.tracknigInfo = [];
	$scope.detailsDisableReturn = true;
	if($rootScope.selectedCustomer != null){
		detailsGetCustomerHistory($scope, $location, $rootScope, $route);
		$scope.customerAddress = "";
		$scope.customerAddress += $scope.selectedCustomer.address.city != null ? $scope.selectedCustomer.address.city : "";
		$scope.customerAddress += $scope.selectedCustomer.address.zipCode != null ? ", " + $scope.selectedCustomer.address.zipCode : "";
		$scope.customerAddress += $scope.selectedCustomer.address.road != null ? ", " + $scope.selectedCustomer.address.road : "";
		$scope.customerAddress += $scope.selectedCustomer.address.road2 != null ? ", " + $scope.selectedCustomer.address.road2 : "";
	}
	if($rootScope.selectedTransaction != null){
		detailsGetOrderDetails($scope, $location, $rootScope, $route);
		$scope.shippingDetails = $rootScope.selectedTransaction.shippingData;
		if($scope.shippingDetails != null){
			$scope.shippingProviderDetails = $scope.shippingDetails.provider;
			if($scope.shippingDetails.service != $scope.shippingDetails.provider){
				$scope.shippingProviderDetails += ", " + $scope.shippingDetails.service;
			}
			if($scope.shippingDetails.rateType != $scope.shippingDetails.provider && $scope.shippingDetails.rateType != $scope.shippingDetails.service){
				$scope.shippingProviderDetails += ", " + $scope.shippingDetails.rateType;
			}
			$scope.shippingAddressDetails = "";
			$scope.shippingAddressDetails += $scope.shippingDetails.shippingAddress.city != null ? $scope.shippingDetails.shippingAddress.city : "";
			$scope.shippingAddressDetails += $scope.shippingDetails.shippingAddress.zipCode != null ? ", " + $scope.shippingDetails.shippingAddress.zipCode : "";
			$scope.shippingAddressDetails += $scope.shippingDetails.shippingAddress.road != null ? ", " + $scope.shippingDetails.shippingAddress.road : "";
			$scope.shippingAddressDetails += $scope.shippingDetails.shippingAddress.road2 != null ? ", " + $scope.shippingDetails.shippingAddress.road2 : "";
		}
	}
	$scope.detailsSelectOrder = function(index){detailsSelectOrder($scope, $location, $rootScope, $route, index)};
	$scope.detailsRefundCheckAll = function () {detailsRefundCheckAll($scope, $location, $rootScope, $route);};
	$scope.detailsRefundCheckOne = function () {detailsRefundCheckOne($scope, $location, $rootScope, $route);};
	$scope.detailsReturnSelectedItems = function () {detailsReturnSelectedItems($scope, $location, $rootScope, $route);};
	$scope.detailsSelectReturn = function (index) {detailsSelectReturn($scope, $location, $rootScope, $route, index);};
	$scope.detailsGoToTrackingPage = function () {detailsGoToTrackingPage($scope, $location, $rootScope, $route);};
	$scope.refreshCardPopover = function () {refreshCardPopover();};
	$scope.refreshProductsPopover = function () {refreshProductsPopover();};
	$scope.refreshReturnPopover = function () {refreshReturnPopover();};
	$scope.refreshBoRetunPopover = function () {refreshBoRetunPopover();};
	$scope.toUTCDate = function(dateValue){return toUTCDate(dateValue);};
}

function toUTCDate(dateValue){
	var date = new Date(dateValue);
	var newDate = new Date(date.getUTCFullYear(), date.getUTCMonth(), date.getUTCDate(),  date.getUTCHours(), date.getUTCMinutes(), date.getUTCSeconds(), date.getUTCMilliseconds());
	return newDate.getTime();
}

function detailsGetCustomerHistory(scope, location, rootScope, route){
	var success = function (response) {
		scope.$apply(function () {
			scope.historyDetails = response.list;
		});
	};
	callServer("backoffice/listOrders", "email=" + rootScope.selectedCustomer.email, success, emptyFunc, "GET", "params", "store", true, true, true);
}

function detailsSelectOrder(scope, location, rootScope, route, index){
	$("input[name='detailsRefundAll']").prop("checked", false);
	scope.detailsDisableReturn = true;
	rootScope.returnDetails = null;
	rootScope.logsDetails = null;
	rootScope.selectedTransaction = scope.historyDetails[index];
	detailsGetOrderDetails(scope, location, rootScope, route);
	scope.shippingDetails = rootScope.selectedTransaction.shippingData;
	if(scope.shippingDetails != null){
		scope.shippingProviderDetails = scope.shippingDetails.provider;
		if(scope.shippingDetails.service != scope.shippingDetails.provider){
			scope.shippingProviderDetails += ", " + scope.shippingDetails.service;
		}
		if(scope.shippingDetails.rateType != scope.shippingDetails.provider && scope.shippingDetails.rateType != scope.shippingDetails.service){
			scope.shippingProviderDetails += ", " + scope.shippingDetails.rateType;
		}
		scope.shippingAddressDetails = "";
		scope.shippingAddressDetails += scope.shippingDetails.shippingAddress.city != null ? scope.shippingDetails.shippingAddress.city : "";
		scope.shippingAddressDetails += scope.shippingDetails.shippingAddress.zipCode != null ? ", " + scope.shippingDetails.shippingAddress.zipCode : "";
		scope.shippingAddressDetails += scope.shippingDetails.shippingAddress.road != null ? ", " + scope.shippingDetails.shippingAddress.road : "";
		scope.shippingAddressDetails += scope.shippingDetails.shippingAddress.road2 != null ? ", " + scope.shippingDetails.shippingAddress.road2 : "";
	}
	$("html,body").animate({
		scrollTop: $(".detailsOrderBlock").offset().top
	}, 500);
}

function detailsGetOrderDetails(scope, location, rootScope, route){
	var success = function (response) {
		if(rootScope.isMerchant)
			detailsGetOrderLogs(scope, location, rootScope, route);
		scope.$apply(function () {
			scope.cartDetails = response;
			scope.mogopayShopCart = extractMogopayShopCart(response);
			for(var  i = 0; i < scope.mogopayShopCart.cartItems.length; i++){
				var item = scope.mogopayShopCart.cartItems[i];
				item.sumReturnedItems = 0;
				for(var j = 0; j < item.bOReturnedItems.length; j++){
					item.sumReturnedItems += item.bOReturnedItems[j].quantity;
				}
			}
		});
	};
	var error = function (response) {
		scope.cartDetails = {};
		scope.mogopayShopCart = {};
		showAlertBootStrapMsg("warning", rootScope.resourceBundle.error_details_not_found);
		if(rootScope.isMerchant)
			detailsGetOrderLogs(scope, location, rootScope, route);
	};
	callServer("backoffice/cartDetails/" + rootScope.selectedTransaction.uuid, "", success, error, "GET", "params", "store", true, true, true);
}

function detailsGetOrderLogs(scope, location, rootScope, route){
	var success = function (response) {
		for (var i = 0; i < response.length; i++){
			var log = response[i].log.replace(new RegExp("=", "g"), " = ");
			if(log.indexOf("&") >= 0)
				response[i].log = log.split("&");
			else
				response[i].log = [log];
		}
		scope.$apply(function () {
			rootScope.logsDetails = response;
		});
	};
	callServer("backoffice/transactions/" + rootScope.selectedTransaction.uuid + "/logs", "", success, emptyFunc, "GET", "params", "pay", false, false, false);
}

function detailsRefundCheckAll(scope, location, rootScope, route){
	scope.detailsDisableReturn = !($("input[name='detailsRefundAll']").is(":checked") && $("input[name='detailsRefundOne']:not([disabled])").length > 0);
	$("input[name='detailsRefundOne']:not([disabled])").prop("checked", $("input[name='detailsRefundAll']").is(":checked"));
}

function detailsRefundCheckOne(scope, location, rootScope, route){
	var allchecked = true;
	scope.detailsDisableReturn = true;
	var checkBoxes = $("input[name='detailsRefundOne']:not([disabled])");
	for(var i = 0; i < checkBoxes.length; i++){
		if(!$(checkBoxes[i]).is(":checked")){
			allchecked = false;
		}
		else{
			scope.detailsDisableReturn = false;
		}
	}
	$("input[name='detailsRefundAll']").prop("checked", allchecked);
}

function detailsSelectReturn(scope, location, rootScope, route, index){
	rootScope.itemsToBeReturned = [];
	rootScope.returnDetails = {
		name: scope.mogopayShopCart.cartItems[index].principal.product.name + " / (" + scope.mogopayShopCart.cartItems[index].sku.sku + ")",
		returnedItems: scope.mogopayShopCart.cartItems[index].bOReturnedItems,
		cartItem: scope.mogopayShopCart.cartItems[index]
	}
	if(rootScope.isMerchant){
		navigateToPage(scope, location, rootScope, route, "return");
	}
}

function detailsReturnSelectedItems(scope, location, rootScope, route){
	rootScope.itemsToBeReturned = [];
	var checkBoxes = $("input[name='detailsRefundOne']:not([disabled])");
	if(checkBoxes.length == 0)
		return;
	for(var i = 0; i < checkBoxes.length; i++){
		if($(checkBoxes[i]).is(":checked")){
			rootScope.itemsToBeReturned[rootScope.itemsToBeReturned.length] = scope.mogopayShopCart.cartItems[$(checkBoxes[i]).attr("index")];
		}
	}
	navigateToPage(scope, location, rootScope, route, "return");
}

function detailsGoToTrackingPage(scope, location, rootScope, route){
	rootScope.tracknigInfo = scope.shippingDetails.trackingHistory;
	navigateToPage(scope, location, rootScope, route, "trackingInfo");
}

function refreshCardPopover() {
	$("[rel=popoverCard]").popover({
		html : true,
		placement:"bottom",
		content: function () {
			$(".popover").removeClass("in").remove();
			var parent =  $(this).parent();
			var element = $(".dialog", parent);
			return element.html();
		}
	});
	$( window ).resize(function () {
		$("[rel=popoverCard]").popover("hide");
	});
}

function refreshProductsPopover() {
	$("[rel=popoverProduts]").popover({
		html : true,
		placement:"bottom",
		content: function () {
			$(".popover").removeClass("in").remove();
			var parent =  $(this).parent();
			var element = $(".dialog", parent);
			return element.html();
		}
	});
	$( window ).resize(function () {
		$("[rel=popoverProduts]").popover("hide");
	});
}

function refreshReturnPopover() {
	$("[rel=popoverReturn]").popover({
		html : true,
		placement:"bottom",
		content: function () {
			$(".popover").removeClass("in").remove();
			var parent =  $(this).parent();
			var element = $(".dialog", parent);
			return element.html();
		}
	});
	$( window ).resize(function () {
		$("[rel=popoverReturn]").popover("hide");
	});
}

function refreshBoRetunPopover() {
	$("[rel=popoverBoRetun]").popover({
		html : true,
		placement:"bottom",
		content: function () {
			$(".popover").removeClass("in").remove();
			var parent =  $(this).parent();
			var element = $(".dialog", parent);
			return element.html();
		}
	});
	$( window ).resize(function () {
		$("[rel=popoverBoRetun]").popover("hide");
	});
}