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
        callServer("account/profile-info", "", success, function (response) {});
    };
	$scope.historyDetails = null;
	$rootScope.returnDetails = null;
	$rootScope.logsDetails = null;
	$rootScope.itemsToBeReturned = [];
	if($rootScope.selectedCustomer != null){
		detailsGetCustomerHistory($scope, $location, $rootScope, $route);
	}
	if($rootScope.selectedTransaction != null){
		detailsGetOrderDetails($scope, $location, $rootScope, $route);
		if($rootScope.isMerchant)
			detailsGetOrderLogs($scope, $location, $rootScope, $route)
	}
	$scope.detailsSelectOrder = function(index){detailsSelectOrder($scope, $location, $rootScope, $route, index)};
	$scope.detailsRefundCheckAll = function () {detailsRefundCheckAll($scope, $location, $rootScope, $route);};
	$scope.detailsRefundCheckOne = function () {detailsRefundCheckOne($scope, $location, $rootScope, $route);};
	$scope.detailsReturnSelectedItems = function () {detailsReturnSelectedItems($scope, $location, $rootScope, $route);};
	$scope.detailsSelectReturn = function (index) {detailsSelectReturn($scope, $location, $rootScope, $route, index);};
	$scope.refreshCardPopover = function () {refreshCardPopover();};
	$scope.refreshProductsPopover = function () {refreshProductsPopover();};
	$scope.refreshReturnPopover = function () {refreshReturnPopover();};
	$scope.refreshBoRetunPopover = function () {refreshBoRetunPopover();};
}

function detailsGetCustomerHistory(scope, location, rootScope, route){
	var success = function (response) {
		scope.$apply(function () {
			scope.historyDetails = response.list;
		});
	};
	callStoreServer("backoffice/listOrders", "email=" + rootScope.selectedCustomer.email, success, function (response) {}, rootScope.selectedStore, "GET");
}

function detailsSelectOrder(scope, location, rootScope, route, index){
	rootScope.returnDetails = null;
	rootScope.logsDetails = null;
	rootScope.selectedTransaction = scope.historyDetails[index];
	detailsGetOrderDetails(scope, location, rootScope, route);
	if(rootScope.isMerchant)
		detailsGetOrderLogs(scope, location, rootScope, route)
}

function detailsGetOrderDetails(scope, location, rootScope, route){
	var success = function (response) {
		scope.$apply(function () {
			scope.cartDetails = response;
			for(var  i = 0; i < scope.cartDetails.cartItems.length; i++){
				var item = scope.cartDetails.cartItems[i];
				item.sumReturnedItems = 0;
				for(var j = 0; j < item.bOReturnedItems.length; j++){
					item.sumReturnedItems += item.bOReturnedItems[j].quantity;
				}
			}
		});
	};
	callStoreServer("backoffice/cartDetails/" + rootScope.selectedTransaction.uuid, "", success, function (response) {}, rootScope.selectedStore, "GET");
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
	callServer("backoffice/transactions/" + rootScope.selectedTransaction.uuid + "/logs", "", success, function (response) {});
}

function detailsRefundCheckAll(scope, location, rootScope, route){
	$("input[name='detailsRefundOne']:not([disabled])").prop("checked", $("input[name='detailsRefundAll']").is(":checked"));
}

function detailsRefundCheckOne(scope, location, rootScope, route){
	var allchecked = true;
	var checkBoxes = $("input[name='detailsRefundOne']:not([disabled])");
	for(var i = 0; i < checkBoxes.length; i++){
		if(!$(checkBoxes[i]).is(":checked")){
			allchecked = false;
			break;
		}
	}
	$("input[name='detailsRefundAll']").prop("checked", allchecked);
}

function detailsSelectReturn(scope, location, rootScope, route, index){
	rootScope.itemsToBeReturned = [];
	rootScope.returnDetails = {
		name: scope.cartDetails.cartItems[index].bOProducts[0].product.name + " / (" + scope.cartDetails.cartItems[index].sku.sku + ")",
		returnedItems: scope.cartDetails.cartItems[index].bOReturnedItems,
		cartItem: scope.cartDetails.cartItems[index]
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
			rootScope.itemsToBeReturned[rootScope.itemsToBeReturned.length] = scope.cartDetails.cartItems[$(checkBoxes[i]).attr("index")];
		}
	}
	navigateToPage(scope, location, rootScope, route, "return");
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