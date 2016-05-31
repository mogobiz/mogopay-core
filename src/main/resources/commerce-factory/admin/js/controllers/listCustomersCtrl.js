/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */

function ListCustomersCtrl($scope, $location, $rootScope, $route){
	if(!isConnectedUser($scope, $location, $rootScope, $route))
		return;
	$scope.goToListTrasactions = function () {
        navigateToPage($scope, $location, $rootScope, $route, "listTransactions");
    };
    $scope.goToProfile = function () {
        var success = function (response) {
            $rootScope.userProfile = response;
            $scope.$apply();
            navigateToPage($scope, $location, $rootScope, $route, "profile");
        };
        callServer("account/profile-info", "", success, emptyFunc, "GET", "params", "pay", true, false, true);
    };
	selectedStore = $rootScope.allStores[0];
	$scope.customersSelectedStore = selectedStore;
	$scope.listCustomersSearch =  function () {listCustomersSearch($scope, $location, $rootScope, $route)};
	$scope.gotToCustomerDetails =  function (index) {gotToCustomerDetails($scope, $location, $rootScope, $route, index)};
	$scope.customersChangeStore =  function () {customersChangeStore($scope, $location, $rootScope, $route)};
	$scope.listCustomersSortTable =  function (filed) {listCustomersSortTable($scope, $location, $rootScope, $route, filed)};
}

function listCustomersSearch(scope, location, rootScope, route){
	var success = function (response) {
		scope.$apply(function () {
			scope.listCustomersSortField = "";
			scope.listCustomersSortReverse = false;
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
	callServer("backoffice/listCustomers", dataToSend, success, emptyFunc, "GET", "params", "store", true, true, true);
}

function gotToCustomerDetails(scope, location, rootScope, route, index){
	rootScope.selectedCustomer = rootScope.customers[index];
	rootScope.selectedTransaction = null;
	navigateToPage(scope, location, rootScope, route, "details");
}

function customersChangeStore(scope, location, rootScope, route){
	selectedStore = scope.customersSelectedStore;
}

function listCustomersSortTable(scope, location, rootScope, route, field){
	scope.listCustomersSortField = field;
	if($("#listCustomersTableResult th[name='" + field + "']").hasClass("asc")){
		$("#listCustomersTableResult th[name='" + field + "']").removeClass("asc").addClass("desc");
		scope.listCustomersSortReverse = true;
	}
	else{
		$("#listCustomersTableResult th").removeClass("desc").removeClass("asc").addClass("both");
		$("#listCustomersTableResult th[name='" + field + "']").removeClass("both").addClass("asc");
		scope.listCustomersSortReverse = false;
	}
}