function ListTransactionsCtrl($scope, $location, $rootScope, $route) {
	$rootScope.selectedStore = $rootScope.allStores[0];
	$scope.transactionsSelectedStore = $rootScope.selectedStore;
	$scope.goToProfile = function () {
		var success = function (response) {
			$rootScope.userProfile = response;
			$location.path("/profile");
			$scope.$apply();
			$location.replace();
		};
		callServer("account/profile-info", "", success, function (response) {});
	};
	$scope.goToListCustomers = function () {
		$location.path("/listCustomers");
		$location.replace();
	};
	$scope.refreshCardPopover = function () {refreshCardPopover();};
	$scope.listTransactionsSearch =  function () {listTransactionsSearch($scope, $location, $rootScope, $route)};
	$scope.gotToOrderDetails = function (index) {gotToOrderDetails($scope, $location, $rootScope, $route, index);};
	$scope.transactionsChangeStore = function () {transactionsChangeStore($scope, $location, $rootScope, $route);};
}

function listTransactionsSearch (scope, location, rootScope, route) {
	var success = function (response) {
		scope.$apply(function () {
			rootScope.transactions = response.list;
		});
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

function zerosAutoComplete(number, length){
	var str = "" + number
	while (str.length < length) {
		str = "0" + str
	}
	return str
}

function gotToOrderDetails(scope, location, rootScope, route, index){
	rootScope.selectedOrder = rootScope.transactions[index];
	rootScope.selectedCustomer = (rootScope.transactions[index].customer != "undefined") ? rootScope.transactions[index].customer : null;
	location.path("/details");
	location.replace();
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

function transactionsChangeStore(scope, location, rootScope, route){
	rootScope.selectedStore = scope.transactionsSelectedStore;
}