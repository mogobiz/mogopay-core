function SignupCtrl($scope, $location, $rootScope, $route) {
	var minBirthDate = new Date();
	var maxBirthDate = new Date();
	minBirthDate.setFullYear(new Date().getFullYear() - 100);
	maxBirthDate.setFullYear(new Date().getFullYear() - 18);
	$scope.minBirthDate = dateToDateValue(minBirthDate);
	$scope.maxBirthDate = dateToDateValue(maxBirthDate);

	$scope.profileCivilityOptions = [{"value": "MR", "name": "Mr."}, {"value": "MRS", "name": "Mrs."}];
	$scope.profileCivilityModel = $scope.profileCivilityOptions[0];
	if($rootScope.userProfile && $rootScope.userProfile.account && $rootScope.userProfile.account.civility.name == "MRS") {
		$scope.profileCivilityModel = $scope.profileCivilityOptions[1];
	}

	$scope.profileCountriesOptions = [];
	profileLoadCountries($scope, $location, $rootScope, $route);

	setTimeout(function () {
		$("#profileCity").autocomplete({
			source: function (request, response) {
				var valueCountry = ($scope.profileCountriesModel && $scope.profileCountriesModel != "") ? $scope.profileCountriesModel.code : "";
				var valueState = ($scope.profileStateModel && $scope.profileStateModel != "") ? $scope.profileStateModel.code : "";
				var valueRegion = ($scope.profileRegionModel && $scope.profileRegionModel != "") ? $scope.profileRegionModel.code : "";
				var valueCity = $("#profileCity").val();
				profileGetCitiesForAutoComplete(valueCountry, valueState, valueRegion, valueCity, response);
			},
			select: function (event, ui) {
				$(event.target).val(ui.item.value);
			},
			minLength: 3,
			delay: 0
		});
	}, 1000);

	$scope.profileCheckPhoneNumberForCountry = function () {profileCheckPhoneNumberForCountry($scope, $location, $rootScope, $route);};
	$scope.profileCheckPasswordConfrimation = function () {
		$("#profileConfirmPassword")[0].setCustomValidity($("#profileConfirmPassword").val() != $("#profilePassword").val() ? "The two passwords must match !" : "");
		if($("#profileConfirmPassword").val() != $("#profilePassword").val()) {
			showAlertBootStrapMsg("warning", "The two passwords must match !");
		}
	};

	$scope.profileCheckPasswordRegExValidity = function () {profileCheckPasswordRegExValidity($scope, $location, $rootScope, $route);};

	$scope.profileLoadStatesForCountry = function () {profileLoadStatesForCountry($scope, $location, $rootScope, $route);};
	$scope.profileLoadRegionsForState = function () {profileLoadRegionsForState($scope, $location, $rootScope, $route);};

	$scope.createProfile = function () {
		setTimeout(function () { createProfile($scope, $location, $rootScope, $route); }, 250);
	};

	function profileCheckPhoneNumberForCountry(scope, location, rootScope, route) {
		if(!scope.profileCountriesModel || scope.profileCountriesModel == "") {
			showAlertBootStrapMsg("warning", "Please select your country first");
			$("#profilePhoneNumber").val("");
			return;
		}
		var success = function (response) {
			if (response['isValid'] == true) {
				$("#profilePhoneNumber").val(response['nationalFormat']);
			} else {
				showAlertBootStrapMsg("warning", "Invalid phone number!");
				$("#profilePhoneNumber").val("");
			}
		};
		callServer(
			"country/" + scope.profileCountriesModel.code + "/check-phone-number/" + $("#profilePhoneNumber").val(),
			"",
			success,
			function (response) {}
		);
	}

	function profileCheckPasswordRegExValidity(scope, location, rootScope, route) {
		var success = function (response) {
			$("#authPasswordRegex")[0].setCustomValidity(response =="false" ? "Invalid Regular Expression" : "");
			if(response == "false") {
				$("#authPasswordRegex").focus();
				showAlertBootStrapMsg("warning", "Invalid Regular Expression");
			}
		};
		var pattern = encodeURIComponent($("#authPasswordRegex").val());
		callServer("account/is-pattern-valid/" + pattern, "", success, function (response) {});
	}

	function profileGetCitiesForAutoComplete(country, state, region, city, response) {
		var success = function (data) {
			response($.map(data, function (item) {
				return {
					value: item.name
				}
			}));
		};
		var dataToSend = "country=" + country + "&parent_admin1_code=" + state + "&parent_admin2_code=" + region + "&name=" + city;
		callServer("country/cities", dataToSend, success, function (response) {});
	}

	function profileLoadCountries(scope, location, rootScope, route) {
		var success = function (response) {
			scope.profileCountriesOptions = response;
			scope.profileCountriesModel = "";
			if(rootScope.userProfile && rootScope.userProfile.account.address.country) {
				for(var i = 0; i < scope.profileCountriesOptions.length; i++) {
					if(scope.profileCountriesOptions[i].code == rootScope.userProfile.account.address.country) {
						scope.profileCountriesModel = scope.profileCountriesOptions[i];
						scope.$apply();
						profileLoadStatesForCountry(scope, location, rootScope, route);
						break;
					}
				}
			}
			scope.$apply();
		}
		callServer("country/countries-for-billing", "", success, function (response) {});
	}

	function profileLoadStatesForCountry(scope, location, rootScope, route) {
		if(!scope.profileCountriesModel || scope.profileCountriesModel == "") {
			scope.profileStateOptions = [];
			scope.profileStateModel = "";
			scope.profileRegionOptions = [];
			scope.profileRegionModel = "";
			return;
		}
		var success = function (response) {
			scope.profileStateOptions = response;
			scope.profileStateModel = "";
			scope.profileRegionOptions = [];
			scope.profileRegionModel = "";
			if(rootScope.userProfile && rootScope.userProfile.account.address.admin1) {
				for(var i = 0; i < scope.profileStateOptions.length; i++) {
					if(scope.profileStateOptions[i].code == rootScope.userProfile.account.address.admin1) {
						scope.profileStateModel = scope.profileStateOptions[i];
						scope.$apply();
						profileLoadRegionsForState(scope, location, rootScope, route);
						break;
					}
				}
			}
			scope.$apply();
		};
		callServer("country/admins1/" + scope.profileCountriesModel.code, "", success, function (response) {});
	}

	function profileLoadRegionsForState(scope, location, rootScope, route) {
		if(!scope.profileStateModel || scope.profileStateModel == "") {
			scope.profileRegionOptions = [];
			scope.profileRegionModel = "";
			return;
		}
		var success = function (response) {
			scope.profileRegionOptions = response;
			scope.profileRegionModel = "";
			if(rootScope.userProfile && rootScope.userProfile.account.address.admin2) {
				for(var i = 0; i < scope.profileRegionOptions.length; i++) {
					if(scope.profileRegionOptions[i].code == rootScope.userProfile.account.address.admin2) {
						scope.profileRegionModel = scope.profileRegionOptions[i];
						break;
					}
				}
			}
			scope.$apply();
		};
		var dataToSend = "country=" + scope.profileCountriesModel.code + "&state=" + scope.profileStateModel.code;
		callServer("country/admins2/" + scope.profileStateModel.code, "", success, function (response) {});
	}

	function createProfile(scope, location, rootScope, route) {
		if(!validateProfileForm(scope, location, rootScope, route))
			return;
		var dataToSend = getProfileFormData(scope, location, rootScope, route);
		var success = function (response) {
			showAlertBootStrapMsg("success", "Sign up successful!");

			if(response.token == ""){
				var success = function (response) {
					$rootScope.createPage = false;
					$rootScope.getAllStores();
				};

				var data = "";
				data += "email=" + $("#profileEmail").val();
				data += "&password=" + $("#profilePassword").val();
				data += "&is_customer=" + !rootScope.isMerchant;

				postOnServer("account/login", data, success, function (response) {});
			}
			else{
				navigateToPage(scope, location, rootScope, route, "validation");
			}
		};
		var failure = function (response) {
			if (response.status == 401) {
				showAlertBootStrapMsg("warning", "Account already exists.");
			}
		};
		postOnServer("account/signup", dataToSend, success, failure);
	}

	function getProfileFormData(scope, location, rootScope, route) {
		var data = "";
		data += "is_merchant=" + rootScope.isMerchant;

		data += "&email=" + $("#profileEmail").val();
		if(rootScope.createPage) {
			data += "&password=" + $("#profilePassword").val();
			data += "&password2=" + $("#profileConfirmPassword").val();
		}
		if(rootScope.isMerchant) {
			data += "&company=" + $("#profileCompanyName").val();
			data += "&website=" + $("#profileWebsite").val();
		}
		data += "&lphone=" + $("#profilePhoneNumber").val();
		data += "&civility=" + scope.profileCivilityModel.value;
		data += "&firstname=" + $("#profileFirstName").val();
		data += "&lastname=" + $("#profileLastName").val();
		data += "&birthday=" + $("#profileBirthDate").val();
		data += "&country=" + scope.profileCountriesModel.code;
		data += "&admin1=" + ((scope.profileStateModel != "") ? scope.profileStateModel.code : "");
		data += "&admin2=" + ((scope.profileRegionModel != "") ? scope.profileRegionModel.code : "");
		data += "&city=" + $("#profileCity").val();
		data += "&road=" + $("#profileRoad").val();
		data += "&zip_code=" + $("#profilePostalCode").val();
		if(rootScope.isMerchant)
			data += "&withShippingAddress=true";
		else
			data += "&withShippingAddress=" + $("#profileWithShippingAddress").is(":checked");
		data += "&validation_url=" + deployUrl + "validation.html";
		return data;
	}

	function validateProfileForm(scope, location, rootScope, route) {
		if($("#profileEmail").val() == "" || $("#profilePhoneNumber").val() == ""
			|| $("#profileFirstName").val() == "" || $("#profileLastName").val() == "" || $("#profileBirthDate").val() == ""
			|| !scope.profileCountriesModel || scope.profileCountriesModel == "" || $("#profileCity").val() == "" || $("#profileRoad").val() == ""
			|| $("#profilePostalCode").val() == "") {
			showAlertBootStrapMsg("warning", "Please fill all required fields");
			return false;
		}
		if (rootScope.isMerchant && $("#profileCompanyName").val() == "" || $("#profileWebsite").val() == "") {
			showAlertBootStrapMsg("warning", "Please fill all required fields");
			return false;
		}
		if(rootScope.createPage) {
			if($("#profilePassword").val() == "" || $("#profileConfirmPassword").val() == "") {
				$(".nav-tabs a[data-target='#profileInfo']").tab("show");
				showAlertBootStrapMsg("warning", "Please fill all required fields");
				return false;
			}
		}
		if(!$("#profileEmail")[0].checkValidity()) {
			$(".nav-tabs a[data-target='#profileInfo']").tab("show");
			$("#profileEmail").focus();
			showAlertBootStrapMsg("warning", "Invalid email !");
			return false;
		}
		if(rootScope.createPage) {
			if($("#profilePassword").val() != $("#profileConfirmPassword").val()) {
				$(".nav-tabs a[data-target='#profileInfo']").tab("show");
				$("#profileConfirmPassword").focus();
				showAlertBootStrapMsg("warning", "The two passwords must match !");
				return false;
			}
		}
		if(rootScope.isMerchant && !$("#profileCompanyName")[0].checkValidity()) {
			$(".nav-tabs a[data-target='#profileInfo']").tab("show");
			$("#profileCompanyName").focus();
			showAlertBootStrapMsg("warning", "Invalid company name !");
			return false;
		}
		if(rootScope.isMerchant && !$("#profileWebsite")[0].checkValidity()) {
			$(".nav-tabs a[data-target='#profileInfo']").tab("show");
			$("#profileWebsite").focus();
			showAlertBootStrapMsg("warning", "Invalid website !");
			return false;
		}
		if(!$("#profileBirthDate")[0].checkValidity()) {
			$("#profileBirthDate").focus();
			showAlertBootStrapMsg("warning", "Invalid birth date !");
			return false;
		}
		return true;
	}
}
