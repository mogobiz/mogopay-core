function ListTransactionsCtrl($scope, $location, $rootScope, $route) {
	if(!isConnectedUser($scope, $location, $rootScope, $route))
		return;
	$rootScope.selectedStore = $rootScope.allStores[0];
	$scope.transactionsSelectedStore = $rootScope.selectedStore;
	$scope.goToProfile = function () {
		var success = function (response) {
			$rootScope.userProfile = response;
			$scope.$apply();
			navigateToPage($scope, $location, $rootScope, $route, "profile");
		};
		callServer("account/profile-info", "", success, function (response) {});
	};
	$scope.goToListCustomers = function () {
		navigateToPage($scope, $location, $rootScope, $route, "listCustomers");
	};
	$scope.refreshCardPopover = function () {refreshCardPopover();};
	$scope.refreshReturnStatusPopover = function () {refreshReturnStatusPopover();};
	$scope.listTransactionsSearch =  function () {listTransactionsSearch($scope, $location, $rootScope, $route)};
	$scope.gotToOrderDetails = function (index) {gotToOrderDetails($scope, $location, $rootScope, $route, index);};
	$scope.transactionsChangeStore = function () {transactionsChangeStore($scope, $location, $rootScope, $route);};
}

function listTransactionsSearch (scope, location, rootScope, route) {
	var success = function (response) {
		rootScope.transactions = response.list;
		listTransactionsGetCartItems(scope, location, rootScope, route, rootScope.transactions[0].uuid, 0);
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

	if ($.trim($("#listTransactionsStatus").val()) != ""){
		if(dataToSend != "")
			dataToSend += "&";
		dataToSend += "transactionStatus=" + $("#listTransactionsStatus").val();
	}

	if ($.trim($("#listTransactionsDelivery").val()) != ""){
		if(dataToSend != "")
			dataToSend += "&";
		dataToSend += "deliveryStatus=" + $("#listTransactionsDelivery").val();
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

	callStoreServer("backoffice/listOrders", dataToSend, success, function (response) {}, rootScope.selectedStore, "GET");
}

function listTransactionsGetCartItems(scope, location, rootScope, route, transactionUUID, index){
	var success = function(response){
		var items = response.cartItems;
		var listRetunedStatus = []
		for(var  i = 0; i < items.length; i++){
			var item = items[i];
			for(var j = 0; j < item.bOReturnedItems.length; j++){
				if(item.bOReturnedItems[j].boReturns.length > 0){
					listRetunedStatus[listRetunedStatus.length] = {value: item.bOProducts[0].product.name + ": " + rootScope.returnStatusValues[item.bOReturnedItems[j].boReturns[0].status]};
				}
			}
		}
		if(listRetunedStatus.length == 0)
			listRetunedStatus = [{value: "None"}];
		rootScope.transactions[index].listRetunedStatus = listRetunedStatus;
		if(index == rootScope.transactions.length - 1){
			scope.$apply();
		}
		else{
			index++;
			listTransactionsGetCartItems(scope, location, rootScope, route, rootScope.transactions[index].uuid, index)
		}
	}
	callStoreServer("backoffice/cartDetails/" + transactionUUID, "", success, function (response) {}, rootScope.selectedStore, "GET");
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
	rootScope.selectedStore = scope.transactionsSelectedStore;
}