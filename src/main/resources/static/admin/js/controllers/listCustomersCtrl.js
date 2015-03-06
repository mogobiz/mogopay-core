function ListCustomersCtrl($scope, $location, $rootScope, $route){
	$scope.goToListTrasactions = function () {
        $location.path("/listTransactions");
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
	$rootScope.selectedStore = $rootScope.allStores[0];
	$scope.customersSelectedStore = $rootScope.selectedStore;
	$scope.listCustomersSearch =  function () {listCustomersSearch($scope, $location, $rootScope, $route)};
	$scope.gotToCustomerDetails =  function (index) {gotToCustomerDetails($scope, $location, $rootScope, $route, index)};
	$scope.customersChangeStore =  function () {customersChangeStore($scope, $location, $rootScope, $route)};
}

function listCustomersSearch(scope, location, rootScope, route){
	var success = function (response) {
		scope.$apply(function () {
			rootScope.customers = response.list;
		});
	};
	var dataToSend = "";

	if ($.trim($("#listCustomersEmail").val()) != ""){
		dataToSend += "email=" + $("#listCustomersEmail").val();
	}

	if ($.trim($("#listCustomersLastName").val()) != ""){
		if(dataToSend != "")
			dataToSend += "&";
		dataToSend += "lastName=" + $("#listCustomersLastName").val();
	}

	callStoreServer("backoffice/listCustomers", dataToSend, success, function (response) {}, rootScope.selectedStore, "GET");
}

function gotToCustomerDetails(scope, location, rootScope, route, index){
	rootScope.selectedCustomer = rootScope.customers[index];
	rootScope.selectedOrder = null;
	location.path("/details");
	location.replace();
}

function customersChangeStore(scope, location, rootScope, route){
	rootScope.selectedStore = scope.customersSelectedStore;
}