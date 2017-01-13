/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */

function ListTransactionsCtrl($scope, $location, $rootScope, $route) {
	if(!isConnectedUser($scope, $location, $rootScope, $route))
		return;
	selectedStore = $rootScope.allStores[0];
	$scope.transactionsSelectedStore = selectedStore;
	$scope.listTransactionsStatus = "";
	$scope.listTransactionsDelivery = "";
	$scope.goToProfile = function () {
		var success = function (response) {
			$rootScope.userProfile = response;
			$scope.$apply();
			navigateToPage($scope, $location, $rootScope, $route, "profile");
		};
		callServer("account/profile-info", "", success, emptyFunc, "GET", "params", "pay", true, false, true);
	};
	$scope.goToListCustomers = function () {
		navigateToPage($scope, $location, $rootScope, $route, "listCustomers");
	};
	$scope.refreshCardPopover = function () {refreshCardPopover();};
	$scope.refreshReturnStatusPopover = function () {refreshReturnStatusPopover();};
	$scope.listTransactionsSearch =  function () {listTransactionsSearch($scope, $location, $rootScope, $route)};
	$scope.gotToOrderDetails = function (index) {gotToOrderDetails($scope, $location, $rootScope, $route, index);};
	$scope.transactionsChangeStore = function () {transactionsChangeStore($scope, $location, $rootScope, $route);};
	$scope.listTransactionsSortTable = function (field) {listTransactionsSortTable($scope, $location, $rootScope, $route, field);};
}

function listTransactionsSearch (scope, location, rootScope, route) {
	var success = function (response) {
		scope.listTransactionsSortField = "";
		scope.listTransactionsSortReverse = false;
		rootScope.transactions = response.list;
		if(rootScope.transactions.length > 0)
			listTransactionsGetCartItems(scope, location, rootScope, route, rootScope.transactions[0].uuid, 0);
		else{
			scope.$apply();
			$("body").removeClass("loading");
		}
	};
	var dataToSend = "";

	if ($.trim($("#listTransactionsEmail").val()) != ""){
		dataToSend += "email=" + $("#listTransactionsEmail").val();
	}

	if ($.trim($("#listTransactionsAmount").val()) != ""){
		if(dataToSend != "")
			dataToSend += "&";
		dataToSend += "price=" + $("#listTransactionsAmount").val();
	}

	if (scope.listTransactionsStatus != ""){
		if(dataToSend != "")
			dataToSend += "&";
		dataToSend += "transactionStatus=" + scope.listTransactionsStatus;
	}

	if (scope.listTransactionsDelivery != ""){
		if(dataToSend != "")
			dataToSend += "&";
		dataToSend += "deliveryStatus=" + scope.listTransactionsDelivery;
	}

	if ($("#listTransactionsStartDate").val() != ""){
		var startDate = new Date($("#listTransactionsStartDate").val().replace(/-/g, "/"));
		if ($("#listTransactionsStartTime").val() != ""){
			var time = $("#listTransactionsStartTime").val().split(":");
			startDate.setHours(time[0], time[1], 0);
		}
		
		var offset = new Date().getTimezoneOffset()
		offset = ((offset<0? '+':'-') + zerosAutoComplete(parseInt(Math.abs(offset/60)), 2) + ":" + zerosAutoComplete(Math.abs(offset%60), 2));

		startDate = startDate.toJSON();
		startDate = startDate.substring(0, startDate.lastIndexOf(".")) + offset;

		if(dataToSend != "")
			dataToSend += "&";
		dataToSend += "startDate=" + encodeURIComponent(startDate);
	}

	if ($("#listTransactionsEndDate").val() != ""){
		var endDate = new Date($("#listTransactionsEndDate").val().replace(/-/g, "/"));
		if ($("#listTransactionsEndTime").val() != ""){
			var time = $("#listTransactionsEndTime").val().split(":");
			endDate.setHours(time[0], time[1], 0);
		}
		var offset = new Date().getTimezoneOffset()
		offset = ((offset<0? '+':'-') + zerosAutoComplete(parseInt(Math.abs(offset/60)), 2) + ":" + zerosAutoComplete(Math.abs(offset%60), 2));

		endDate = endDate.toJSON();
		endDate = endDate.substring(0, endDate.lastIndexOf(".")) + offset;

		if(dataToSend != "")
			dataToSend += "&";
		dataToSend += "endDate=" + encodeURIComponent(endDate);
	}

	callServer("backoffice/listOrders", dataToSend, success, emptyFunc, "GET", "params", "store", true, false, true);
}

function listTransactionsGetCartItems(scope, location, rootScope, route, transactionUUID, index){
	var success = function(response){
		var mogopayShopCart = extractMogopayShopCart(response)
		var listRetunedStatus = []
		var listDeliveryStatus = []
		if (mogopayShopCart != null && mogopayShopCart.cartItems != null) {
			for(var  i = 0; i < mogopayShopCart.cartItems.length; i++){
				var item = mogopayShopCart.cartItems[i];
				if (item.bODelivery) {
					listDeliveryStatus[listDeliveryStatus.length] = item.bODelivery.status
				}
				for(var j = 0; j < item.bOReturnedItems.length; j++){
					if(item.bOReturnedItems[j].boReturns.length > 0){
						listRetunedStatus[listRetunedStatus.length] = {value: item.principal.product.name + ": " + rootScope.returnStatusValues[item.bOReturnedItems[j].boReturns[0].status]};
					}
				}
			}
		}
		if(listDeliveryStatus.length == 0)
			listDeliveryStatus = ["None"];
		if(listRetunedStatus.length == 0)
			listRetunedStatus = [{value: "None"}];
		rootScope.transactions[index].deliveryStatus = listDeliveryStatus;
		rootScope.transactions[index].listRetunedStatus = listRetunedStatus;
		if(index == rootScope.transactions.length - 1){
			scope.$apply();
			$("body").removeClass("loading");
		}
		else{
			index++;
			listTransactionsGetCartItems(scope, location, rootScope, route, rootScope.transactions[index].uuid, index)
		}
	}
	var error = function(response){
		rootScope.transactions[index].listRetunedStatus = [{value: "None"}];
		if(index == rootScope.transactions.length - 1){
			scope.$apply();
			$("body").removeClass("loading");
		}
		else{
			index++;
			listTransactionsGetCartItems(scope, location, rootScope, route, rootScope.transactions[index].uuid, index)
		}
	}
	callServer("backoffice/cartDetails/" + transactionUUID, "", success, error, "GET", "params", "store", false, false, false);
}

function extractMogopayShopCart(transaction) {
	if (transaction.shopCarts) {
		for (var i = 0; i < transaction.shopCarts.length; i++) {
			var shopCart = transaction.shopCarts[i]
			if (shopCart.shopId == "MOGOBIZ") return shopCart;
		}
	}
	return null;
}

function zerosAutoComplete(number, length){
	var str = "" + number
	while (str.length < length) {
		str = "0" + str
	}
	return str
}

function gotToOrderDetails(scope, location, rootScope, route, index){
	rootScope.selectedTransaction = rootScope.transactions[index];
	rootScope.selectedCustomer = (rootScope.transactions[index].customer != "undefined") ? rootScope.transactions[index].customer : null;
	navigateToPage(scope, location, rootScope, route, "details");
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

function refreshReturnStatusPopover() {
	$("[rel=popoverReturnStatus]").popover({
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
		$("[rel=popoverReturnStatus]").popover("hide");
	});
}

function transactionsChangeStore(scope, location, rootScope, route){
	selectedStore = scope.transactionsSelectedStore;
}

function listTransactionsSortTable(scope, location, rootScope, route, field){
	scope.listTransactionsSortField = field;
	if($("#listTransactionsTableResult th[name='" + field + "']").hasClass("asc")){
		$("#listTransactionsTableResult th[name='" + field + "']").removeClass("asc").addClass("desc");
		scope.listTransactionsSortReverse = true;
	}
	else{
		$("#listTransactionsTableResult th").removeClass("desc").removeClass("asc").addClass("both");
		$("#listTransactionsTableResult th[name='" + field + "']").removeClass("both").addClass("asc");
		scope.listTransactionsSortReverse = false;
	}
}