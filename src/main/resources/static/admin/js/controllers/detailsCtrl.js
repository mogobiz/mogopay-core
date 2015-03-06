function DetailsCtrl($scope, $location, $rootScope, $route) {
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
	$rootScope.detailsHistory = null;
	if($rootScope.selectedCustomer != null){
		detailsGetCustomerHistory($scope, $location, $rootScope, $route);
	}
	if($rootScope.selectedOrder != null){
		detailsGetOrderDetails($scope, $location, $rootScope, $route);
	}
	$scope.detailsSelectOrder = function(index){detailsSelectOrder($scope, $location, $rootScope, $route, index)};
	$scope.refreshCardPopover = function () {refreshCardPopover();};
	$scope.refreshProductsPopover = function () {refreshProductsPopover();};
}

function detailsGetCustomerHistory(scope, location, rootScope, route){
	var success = function (response) {
		scope.$apply(function () {
			rootScope.detailsHistory = response.list;
		});
	};
	callStoreServer("backoffice/listOrders", "email=" + rootScope.selectedCustomer.email, success, function (response) {}, rootScope.selectedStore, "GET");
}

function detailsSelectOrder(scope, location, rootScope, route, index){
	rootScope.selectedOrder = rootScope.detailsHistory[index];
	detailsGetOrderDetails(scope, location, rootScope, route);
}

function detailsGetOrderDetails(scope, location, rootScope, route){
	var success = function (response) {
		scope.$apply(function () {
			scope.cartDetails = response;
		});
	};
	callStoreServer("backoffice/cartDetails/" + rootScope.selectedOrder.uuid, "", success, function (response) {}, rootScope.selectedStore, "GET");
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