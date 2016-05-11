/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */

function SignupCtrl($scope, $location, $rootScope, $route) {
	$scope.$on("$destroy", function (event, next, current) {
		$rootScope.createPage = false;
	});

	var signupMinBirthDate = new Date();
	var signupMaxBirthDate = new Date();
	signupMinBirthDate.setFullYear(new Date().getFullYear() - 100);
	signupMaxBirthDate.setFullYear(new Date().getFullYear() - 18);
	$scope.signupMinBirthDate = dateToDateValue(signupMinBirthDate);
	$scope.signupMaxBirthDate = dateToDateValue(signupMaxBirthDate);
	$scope.signupSelectedSeller = "";

	$scope.signupCivilityOptions = [{"value": "MR", "name": "Mr."}, {"value": "MRS", "name": "Mrs."}];
	$scope.signupCivilityModel = $scope.signupCivilityOptions[0];

	$scope.signupCountriesOptions = [];
	signupLoadCountries($scope, $location, $rootScope, $route);

	setTimeout(function () {
		$("#signupCity").autocomplete({
			source: function (request, response) {
				var valueCountry = ($scope.signupCountriesModel && $scope.signupCountriesModel != "") ? $scope.signupCountriesModel.code : "";
				var valueState = ($scope.signupStateModel && $scope.signupStateModel != "") ? $scope.signupStateModel.code : "";
				var valueRegion = ($scope.signupRegionModel && $scope.signupRegionModel != "") ? $scope.signupRegionModel.code : "";
				var valueCity = $("#signupCity").val();
				signupGetCitiesForAutoComplete(valueCountry, valueState, valueRegion, valueCity, response);
			},
			select: function (event, ui) {
				$(event.target).val(ui.item.value);
			},
			minLength: 3,
			delay: 0
		});
	}, 250);

	$scope.signupCheckPasswordConfrimation = function () {signupCheckPasswordConfrimation($scope, $location, $rootScope, $route);};
	$scope.signupCheckPhoneNumberForCountry = function () {signupCheckPhoneNumberForCountry($scope, $location, $rootScope, $route);};
	$scope.signupLoadStatesForCountry = function () {signupLoadStatesForCountry($scope, $location, $rootScope, $route);};
	$scope.signupLoadRegionsForState = function () {signupLoadRegionsForState($scope, $location, $rootScope, $route);};
	$scope.signupCreateProfile = function () {setTimeout(function(){signupCreateProfile($scope, $location, $rootScope, $route);}, 250);};
}

function signupGetCitiesForAutoComplete(country, state, region, city, response) {
	var success = function (data) {
		response($.map(data, function (item) {
			return {
				value: item.name
			}
		}));
	};
	var dataToSend = "country=" + country + "&parent_admin1_code=" + state + "&parent_admin2_code=" + region + "&name=" + city;
	callServer("country/cities", dataToSend, success, emptyFunc, "GET", "params", "pay", false, false, false);
}

function signupLoadCountries(scope, location, rootScope, route) {
	var success = function (response) {
		scope.signupCountriesOptions = response;
		scope.signupCountriesModel = "";
		scope.$apply();
	}
	callServer("country/countries-for-billing", "", success, emptyFunc, "GET", "params", "pay", true, true, true);
}

function signupCheckPasswordConfrimation(scope, location, rootScope, route){
	$("#signupConfirmPassword")[0].setCustomValidity($("#signupConfirmPassword").val() != $("#signupPassword").val() ? rootScope.resourceBundle.error_password_match : "");
	if($("#signupConfirmPassword").val() != $("#signupPassword").val()) {
		showAlertBootStrapMsg("warning", rootScope.resourceBundle.error_password_match);
	}
}

function signupLoadStatesForCountry(scope, location, rootScope, route) {
	if(!scope.signupCountriesModel || scope.signupCountriesModel == "") {
		scope.signupStateOptions = [];
		scope.signupStateModel = "";
		scope.signupRegionOptions = [];
		scope.signupRegionModel = "";
		return;
	}
	var success = function (response) {
		scope.signupStateOptions = response;
		scope.signupStateModel = "";
		scope.signupRegionOptions = [];
		scope.signupRegionModel = "";
		scope.$apply();
	};
	callServer("country/admins1/" + scope.signupCountriesModel.code, "", success, emptyFunc, "GET", "params", "pay", true, true, true);
}

function signupLoadRegionsForState(scope, location, rootScope, route) {
	if(!scope.signupStateModel || scope.signupStateModel == "") {
		scope.signupRegionOptions = [];
		scope.signupRegionModel = "";
		return;
	}
	var success = function (response) {
		scope.signupRegionOptions = response;
		scope.signupRegionModel = "";
		scope.$apply();
	};
	var dataToSend = "country=" + scope.signupCountriesModel.code + "&state=" + scope.signupStateModel.code;
	callServer("country/admins2/" + scope.signupStateModel.code, "", success, emptyFunc, "GET", "params", "pay", true, true, true);
}

function signupCheckPhoneNumberForCountry(scope, location, rootScope, route) {
	if(!scope.signupCountriesModel || scope.signupCountriesModel == "") {
		showAlertBootStrapMsg("warning", rootScope.resourceBundle.error_select_country);
		$("#signupPhoneNumber").val("");
		return;
	}
	var success = function (response) {
		if (response['isValid'] == true) {
			$("#signupPhoneNumber").val(response['nationalFormat']);
		} else {
			showAlertBootStrapMsg("warning", rootScope.resourceBundle.error_invalid_phone);
			$("#signupPhoneNumber").val("");
		}
	};
	var action = "country/" + scope.signupCountriesModel.code + "/check-phone-number/" + $("#signupPhoneNumber").val();
	callServer(action, "", success, emptyFunc, "GET", "params", "pay", false, false, false);
}

function signupCreateProfile(scope, location, rootScope, route) {
	if(!signupValidateForm(scope, location, rootScope, route))
		return;
	var dataToSend = signupGetFormData(scope, location, rootScope, route);
	var success = function (response) {
		showAlertBootStrapMsg("success", rootScope.resourceBundle.message_signup);

		if(response.token == ""){
			var success = function (response) {
				rootScope.getAllStores();
			};
			var data = "";
			data += "email=" + $("#signupEmail").val();
			data += "&password=" + $("#signupPassword").val();
			data += "&is_customer=" + !rootScope.isMerchant;
			callServer("account/login", data, success, emptyFunc, "POST", "params", "pay", false, true, true);
		}
		else{
			$("body").removeClass("loading");
			navigateToPage(scope, location, rootScope, route, "validation");
		}
	};
	var error = function (response) {
		if (response.status == 401) {
			showAlertBootStrapMsg("warning", rootScope.resourceBundle.error_account_exist);
		}
	};
	callServer("account/signup", dataToSend, success, error, "POST", "params", "pay", true, false, true);
}

function signupGetFormData(scope, location, rootScope, route) {
	var data = "";
	data += "is_merchant=" + rootScope.isMerchant;
	if(!rootScope.isMerchant)
		data += "&merchant_id=" + scope.signupSelectedSeller;

	data += "&email=" + $("#signupEmail").val();
	data += "&password=" + $("#signupPassword").val();
	data += "&password2=" + $("#signupConfirmPassword").val();

	if(rootScope.isMerchant){
		data += "&company=" + $("#signupCompanyName").val();
		data += "&website=" + $("#signupWebsite").val();
	}
	data += "&lphone=" + $("#signupPhoneNumber").val();
	data += "&civility=" + scope.signupCivilityModel.value;
	data += "&firstname=" + $("#signupFirstName").val();
	data += "&lastname=" + $("#signupLastName").val();
	data += "&birthday=" + $("#signupBirthDate").val();
	data += "&country=" + scope.signupCountriesModel.code;
	data += "&admin1=" + ((scope.signupStateModel != "") ? scope.signupStateModel.code : "");
	data += "&admin2=" + ((scope.signupRegionModel != "") ? scope.signupRegionModel.code : "");
	data += "&city=" + $("#signupCity").val();
	data += "&road=" + $("#signupRoad").val();
	data += "&zip_code=" + $("#signupPostalCode").val();
	if(rootScope.isMerchant)
		data += "&withShippingAddress=true";
	else
		data += "&withShippingAddress=" + $("#signupWithShippingAddress").is(":checked");
	data += "&validation_url=" + deployUrl + "validation.html";
	return data;
}

function signupValidateForm(scope, location, rootScope, route) {
	if($("#signupEmail").val() == "" || $("#signupPhoneNumber").val() == ""
		|| $("#signupFirstName").val() == "" || $("#signupLastName").val() == "" || $("#signupBirthDate").val() == ""
		|| !scope.signupCountriesModel || scope.signupCountriesModel == "" || $("#signupCity").val() == "" || $("#signupRoad").val() == ""
		|| $("#signupPostalCode").val() == "" || $("#signupPassword").val() == "" || $("#signupConfirmPassword").val() == "") {
		showAlertBootStrapMsg("warning", rootScope.resourceBundle.error_required);
		return false;
	}
	if (rootScope.isMerchant && $("#signupCompanyName").val() == "" || $("#signupWebsite").val() == "") {
		showAlertBootStrapMsg("warning", rootScope.resourceBundle.error_required);
		return false;
	}
	if(!$("#signupEmail")[0].checkValidity()) {
		$("#signupEmail").focus();
		showAlertBootStrapMsg("warning", rootScope.resourceBundle.error_invalid_email);
		return false;
	}
	if($("#signupPassword").val() != $("#signupConfirmPassword").val()) {
		$("#signupConfirmPassword").focus();
		showAlertBootStrapMsg("warning", rootScope.resourceBundle.error_password_match);
		return false;
	}
	if(rootScope.isMerchant && !$("#signupCompanyName")[0].checkValidity()) {
		$("#signupCompanyName").focus();
		showAlertBootStrapMsg("warning", rootScope.resourceBundle.error_invalid_company_name);
		return false;
	}
	if(rootScope.isMerchant && !$("#signupWebsite")[0].checkValidity()) {
		$("#signupWebsite").focus();
		showAlertBootStrapMsg("warning", rootScope.resourceBundle.error_invalid_website);
		return false;
	}
	if(!$("#signupBirthDate")[0].checkValidity()) {
		$("#signupBirthDate").focus();
		showAlertBootStrapMsg("warning", rootScope.resourceBundle.error_invalid_birthdate);
		return false;
	}
	return true;
}
