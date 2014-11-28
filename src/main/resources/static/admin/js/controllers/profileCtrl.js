function ProfileCtrl($scope, $location, $rootScope, $route) {
	$scope.cbParam = JSON.parse($rootScope.userProfile.account.paymentConfig.cbParam);
	
	$("#authorizeNetLogin").change(function(){
		if($(this).val() != "")
			$("#authorizeNetTransactionKey").attr("required", "required");
		else
			$("#authorizeNetTransactionKey").removeAttr("required");
	});
	$("#authorizeNetTransactionKey").change(function(){
		if($(this).val() != "")
			$("#authorizeNetLogin").attr("required", "required");
		else
			$("#authorizeNetLogin").removeAttr("required");
	})

	//Main Variables
	var minBirthDate = new Date();
	var maxBirthDate = new Date();
	minBirthDate.setFullYear(new Date().getFullYear() - 100);
	maxBirthDate.setFullYear(new Date().getFullYear() - 18);
	$scope.minBirthDate = dateToDateValue(minBirthDate);
	$scope.maxBirthDate = dateToDateValue(maxBirthDate);
	$scope.minEpiryDate = dateToMonthValue(new Date());
	$scope.showUserUUID = false;
	$scope.userUUID = "";
	$scope.birthDateValue = $scope.maxBirthDate;
	if($rootScope.userProfile) {
		$scope.birthDateValue = dateToDateValue(new Date($rootScope.userProfile.account.birthDate));
		$scope.userUUID = $rootScope.userProfile.account.secret;
		$scope.creditCards = $rootScope.userProfile.cards;
		for(var i = 0; i < $scope.creditCards.length; i++)
			$scope.creditCards[i].expiryDateVal = dateToMonthValue(new Date($scope.creditCards[i].expiryDate));
	}
	$scope.sipsCetificateFileLabel = "Upload Certificate File";
	$scope.sipsCetificateFileName = "";
	$scope.sipsCetificateFileContent = "";
	if($rootScope.userProfile && $rootScope.userProfile.paymentProviderParam && $rootScope.userProfile.paymentProviderParam.sipsMerchantCertificateFile) {
		$scope.sipsCetificateFileLabel = "Current Certificate : " + $rootScope.userProfile.paymentProviderParam.sipsMerchantCertificateFile;
	}
	$scope.sipsParcomFileLabel = "Upload Parcom File";
	$scope.sipsParcomFileName = "";
	$scope.sipsParcomFileContent = "";
	if($rootScope.userProfile && $rootScope.userProfile.paymentProviderParam && $rootScope.userProfile.paymentProviderParam.sipsMerchantParcomFile) {
		$scope.sipsParcomFileLabel = "Current Parcom File : " + $rootScope.userProfile.paymentProviderParam.sipsMerchantParcomFile;
	}

	//Options and Model
	//Civility
	$scope.profileCivilityOptions = [{"value": "MR", "name": "Mr."}, {"value": "MRS", "name": "Mrs."}];
	$scope.profileCivilityModel = $scope.profileCivilityOptions[0];
	if($rootScope.userProfile && $rootScope.userProfile.account && $rootScope.userProfile.account.civility.name == "MRS") {
		$scope.profileCivilityModel = $scope.profileCivilityOptions[1];
	}

	//Countries
	$scope.profileCountriesOptions = [];
	profileLoadCountries($scope, $location, $rootScope, $route);

	//Credit Card Mode
	$scope.creditCardModeOptions = [
		{"value": "EXTERNAL", "name": "Payment user Interface managed by external provider"},
		{"value": "THREEDS_NO", "name": "Payment without Card holder verification (No 3DSecure)"},
		{"value": "THREEDS_IF_AVAILABLE", "name": "3DSecure Payment if customer card enrolled"},
		{"value": "THREEDS_REQUIRED", "name": "Require customer card to be enrolled for 3DSecure"}
	];
	$scope.creditCardModeModel = null;
	// TODO
	if($rootScope.userProfile && $rootScope.userProfile.account && $rootScope.userProfile.account.paymentConfig && $rootScope.userProfile.account.paymentConfig.paymentMethod) {
		switch($rootScope.userProfile.account.paymentConfig.paymentMethod.name) {
			case "EXTERNAL":$scope.creditCardModeModel = $scope.creditCardModeOptions[0];break;
			case "THREEDS_NO":$scope.creditCardModeModel = $scope.creditCardModeOptions[1];break;
			case "THREEDS_IF_AVAILABLE":$scope.creditCardModeModel = $scope.creditCardModeOptions[2];break;
			case "THREEDS_REQUIRED":$scope.creditCardModeModel = $scope.creditCardModeOptions[3];break;
			default:break;
		}
	}

	//Card Provider
	$scope.creditCardProviderOptions = [
		{"value": "NONE", "name": "NONE"},
		{"value": "PAYLINE", "name": "PAYLINE"},
		{"value": "PAYBOX", "name": "PAYBOX"},
		{"value": "SIPS", "name": "SIPS"},
		{"value": "SYSTEMPAY", "name": "SYSTEMPAY"},
		{"value": "AUTHORIZENET", "name": "AUTHORIZENET"}
	];
	$scope.creditCardProviderModel = null;
	// TODO
	if($rootScope.userProfile && $rootScope.userProfile.account && $rootScope.userProfile.account.paymentConfig && $rootScope.userProfile.account.paymentConfig.cbProvider) {
		switch($rootScope.userProfile.account.paymentConfig.cbProvider.name) {
			case "NONE":$scope.creditCardProviderModel = $scope.creditCardProviderOptions[0];break;
			case "PAYLINE":$scope.creditCardProviderModel = $scope.creditCardProviderOptions[1];break;
			case "PAYBOX":$scope.creditCardProviderModel = $scope.creditCardProviderOptions[2];break;
			case "SIPS":$scope.creditCardProviderModel = $scope.creditCardProviderOptions[3];break;
			case "SYSTEMPAY":$scope.creditCardProviderModel = $scope.creditCardProviderOptions[4];break;
			case "AUTHORIZENET":$scope.creditCardProviderModel = $scope.creditCardProviderOptions[5];break;
			default:break;
		}
	}

	//Paybox Contract
	$scope.payboxContractTypeOptions = [
		{"value": "PAYBOX_SYSTEM", "name": "PAYBOX SYSTEM"},
		{"value": "PAYBOX_DIRECT", "name": "PAYBOX DIRECT"},
		{"value": "PAYBOX_DIRECT_PLUS", "name": "PAYBOX DIRECT PLUS"}
	];
	$scope.payboxContractTypeModel = $scope.payboxContractTypeOptions[0];
	if($rootScope.userProfile && $rootScope.userProfile.paymentProviderParam && $rootScope.userProfile.paymentProviderParam.payboxContract) {
		switch($rootScope.userProfile.paymentProviderParam.payboxContract) {
			case "PAYBOX_SYSTEM":$scope.payboxContractTypeModel = $scope.payboxContractTypeOptions[0];break;
			case "PAYBOX_DIRECT":$scope.payboxContractTypeModel = $scope.payboxContractTypeOptions[1];break;
			case "PAYBOX_DIRECT_PLUS":$scope.payboxContractTypeModel = $scope.payboxContractTypeOptions[2];break;
			default:break;
		}
	}

	//New Personal Card
	$scope.personalCardTypeOptions = [
		{"value": "", "name": ""},
		{"value": "VISA", "name": "VISA"},
		{"value": "MASTER_CARD", "name": "MASTER CARD"},
		{"value": "AMEX", "name": "AMEX"},
		{"value": "CB", "name": "CB"},
		{"value": "DISCOVER", "name": "DISCOVER"},
		{"value": "SWITCH", "name": "SWITCH"},
		{"value": "SOLO", "name": "SOLO"}
	];
	$scope.personalCardTypeModel = $scope.personalCardTypeOptions[0];

	//Personal Cards
	$scope.personalCardsTypeOptions = [];
	$scope.personalCardsTypeModel = [];
	if($rootScope.userProfile && $rootScope.userProfile.cards) {
		for(var i = 0; i < $rootScope.userProfile.cards.length; i++) {
			var len = $scope.personalCardsTypeOptions.length;
			$scope.personalCardsTypeOptions[len] = [
				{"value": "VISA", "name": "VISA"},
				{"value": "MASTER_CARD", "name": "MASTER CARD"},
				{"value": "AMEX", "name": "AMEX"},
				{"value": "CB", "name": "CB"},
				{"value": "DISCOVER", "name": "DISCOVER"},
				{"value": "SWITCH", "name": "SWITCH"},
				{"value": "SOLO", "name": "SOLO"}
			];
			switch($rootScope.userProfile.cards[i].cardType.name) {
				case "VISA":$scope.personalCardsTypeModel[$scope.personalCardsTypeModel.length] = $scope.personalCardsTypeOptions[len][0];break;
				case "MASTER_CARD":$scope.personalCardsTypeModel[$scope.personalCardsTypeModel.length] = $scope.personalCardsTypeOptions[len][1];break;
				case "AMEX":$scope.personalCardsTypeModel[$scope.personalCardsTypeModel.length] = $scope.personalCardsTypeOptions[len][2];break;
				case "CB":$scope.personalCardsTypeModel[$scope.personalCardsTypeModel.length] = $scope.personalCardsTypeOptions[len][3];break;
				case "DISCOVER":$scope.personalCardsTypeModel[$scope.personalCardsTypeModel.length] = $scope.personalCardsTypeOptions[len][4];break;
				case "SWITCH":$scope.personalCardsTypeModel[$scope.personalCardsTypeModel.length] = $scope.personalCardsTypeOptions[len][5];break;
				case "SOLO":$scope.personalCardsTypeModel[$scope.personalCardsTypeModel.length] = $scope.personalCardsTypeOptions[len][6];break;
				default:break;
			}
		}
	}

	//Main Functions
	$scope.goToListTrasactions = function () {
		$location.path("/listTransactions");
		$location.replace();
	};

	setTimeout(function () {
		$("#profileCity").autocomplete({
			source: function (request, response) {
				var valueContry = ($scope.profileCountriesModel && $scope.profileCountriesModel != "") ? $scope.profileCountriesModel.code : "";
				var valueState = ($scope.profileStateModel && $scope.profileStateModel != "") ? $scope.profileStateModel.code : "";
				var valueRegion = ($scope.profileRegionModel && $scope.profileRegionModel != "") ? $scope.profileRegionModel.code : "";
				var valueCity = $("#profileCity").val();
				profileGetCitiesForAutoComplete(valueContry, valueState, valueRegion, valueCity, response);
			},
			select: function (event, ui) {
				$(event.target).val(ui.item.value);
			},
			minLength: 3,
			delay: 0
		});
	},1000);

	$scope.displayUserUUID = function () {$scope.showUserUUID = !$scope.showUserUUID;};
	$scope.renewUserUUID = function () {profileRenewUserUUID($scope, $location, $rootScope, $route);};

	$scope.profileResendMailConfirmation = function () {profileResendMailConfirmation($scope, $location, $rootScope, $route)};
	$scope.profileResendPhoneValidation = function () {profileResendPhoneValidation($scope, $location, $rootScope, $route)};

	$scope.profileCheckPhoneNumberForCountry = function () {profileCheckPhoneNumberForCountry($scope, $location, $rootScope, $route);};
	$scope.profileCheckPasswordConfrimation = function () {
		$("#profileConfirmPassword")[0].setCustomValidity($("#profileConfirmPassword").val() != $("#profilePassword").val() ? "The two passwords must match !" : "");
		if($("#profileConfirmPassword").val() != $("#profilePassword").val()) {
			showAlertBootStrapMsg("warning", "The two passwords must match !");
		}
	};

	$scope.profileCheckCreditCardNumber = function (index) {
		var id = "personalCardNumber";
		if(index)
			id += "-" + index;
		$("#" + id)[0].setCustomValidity(luhn10($("#" + id).val()) == true ? "" : "Invalid credit card number");
		if(!luhn10($("#" + id).val())) {
			$("#" + id).focus();
			showAlertBootStrapMsg("warning", "Invalid credit card number");
		}
	};

	$scope.profileCheckPasswordRegExValidity = function () {profileCheckPasswordRegExValidity($scope, $location, $rootScope, $route);};

	$scope.profileLoadStatesForCountry = function () {profileLoadStatesForCountry($scope, $location, $rootScope, $route);};
	$scope.profileLoadRegionsForState = function () {profileLoadRegionsForState($scope, $location, $rootScope, $route);};

	$scope.saveProfile = function () {setTimeout(function () {saveProfile($scope, $location, $rootScope, $route);}, 250);};

	$scope.addCard = function () {profileAddCreditCard($scope, $location, $rootScope, $route)};
	$scope.updateCard = function (index) {profileUpdateCreditCard($scope, $location, $rootScope, $route, index)};
	$scope.deleteCard = function (index) {profileDeleteCreditCard($scope, $location, $rootScope, $route, index)};

	$("#sipsCetificateFile").change(function (evt) {sipsCetificateFileChangeContent(evt, $scope, $location, $rootScope, $route);});
	$("#sipsParcomFile").change(function (evt) {sipsParcomFileChangeContent(evt, $scope, $location, $rootScope, $route);});

	function luhn10(a,b,c,d,e) {
		if(a == "")
			return true;
		for(d = +a[b = a.length-1], e=0; b--;)
			c = +a[b], d += ++e % 2 ? 2 * c % 10 + (c > 4) : c;
		return !(d%10);
	}

	function profileRenewUserUUID(scope, location, rootScope, route) {
		var success = function (response) {
			scope.userUUID = response.uuid;
			scope.$apply();
		}
		var dataToSend = "";//"xtoken=" + rootScope.xtoken;
		callServer("account/generate-new-secret", dataToSend, success, function () {})
	}

	function profileResendMailConfirmation(scope, location, rootScope, route) {
		var dataToSend = "";//"xtoken=" + rootScope.xtoken;
		callServer("account/generateNewEmailCode", dataToSend, function (response) {}, function (response) {});
	}

	function profileResendPhoneValidation(scope, location, rootScope, route) {
		var dataToSend = "";//"xtoken=" + rootScope.xtoken;
		callServer("account/generateNewPhoneCode", dataToSend, function (response) {}, function (response) {});
	}

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

	function sipsCetificateFileChangeContent(evt, scope, location, rootScope, route) {
		var f = evt.target.files[0];
		if (f) {
			var r = new FileReader();
			r.onload = function (e) {
				scope.sipsCetificateFileName = f.name;
				scope.sipsCetificateFileContent = e.target.result;
				scope.sipsCetificateFileLabel = "Current Certificate : " + f.name;
				scope.$apply();
			}
			r.readAsText(f);
		}
	}

	function sipsParcomFileChangeContent(evt, scope, location, rootScope, route) {
		var f = evt.target.files[0];
		if (f) {
			var r = new FileReader();
			r.onload = function (e) {
				scope.sipsParcomFileName = f.name;
				scope.sipsParcomFileContent = e.target.result;
				scope.sipsParcomFileLabel = "Current Parcom File : " + f.name;
				scope.$apply();
			}
			r.readAsText(f);
		}
	}

// CREDIT CARD FUNCTIONS
	function profileAddCreditCard(scope, location, rootScope, route) {
		if (scope.personalCardTypeModel.value == "") {
			showAlertBootStrapMsg("warning", "Please choose a type !");
			return;
		}
		if (!$("#personalCardNumber")[0].checkValidity()) {
			if($("#personalCardNumber").val() == "")
				showAlertBootStrapMsg("warning", "Invalid credit card number!");
			return;
		}
		if (!$("#personalCardHolderName")[0].checkValidity()) {
			showAlertBootStrapMsg("warning", "Invalid holder name!");
			return;
		}
		if (!$("#personalCardExpiryDate")[0].checkValidity()) {
			showAlertBootStrapMsg("warning", "Invalid date !");
			return;
		}
		var success = function (response) {
			$("#personalCardNumber").val("");
			$("#personalCardHolderName").val("");
			$("#personalCardExpiryDate").val("");
			$("#personalCardType").prop('selectedIndex', 0);

			scope.creditCards.push(response);

			var x = scope.creditCards.length - 1;
			scope.personalCardsTypeOptions.push([
				{"value": "VISA",        "name": "VISA"},
				{"value": "MASTER_CARD", "name": "MASTER CARD"},
				{"value": "AMEX",        "name": "AMEX"},
				{"value": "CB",          "name": "CB"},
				{"value": "DISCOVER",    "name": "DISCOVER"},
				{"value": "SWITCH",      "name": "SWITCH"},
				{"value": "SOLO",        "name": "SOLO"}
			]);
			switch (response.cardType.name) {
				case "VISA":        scope.personalCardsTypeModel[scope.personalCardsTypeModel.length] = scope.personalCardsTypeOptions[x][0]; break;
				case "MASTER_CARD": scope.personalCardsTypeModel[scope.personalCardsTypeModel.length] = scope.personalCardsTypeOptions[x][1]; break;
				case "AMEX":        scope.personalCardsTypeModel[scope.personalCardsTypeModel.length] = scope.personalCardsTypeOptions[x][2]; break;
				case "CB":          scope.personalCardsTypeModel[scope.personalCardsTypeModel.length] = scope.personalCardsTypeOptions[x][3]; break;
				case "DISCOVER":    scope.personalCardsTypeModel[scope.personalCardsTypeModel.length] = scope.personalCardsTypeOptions[x][4]; break;
				case "SWITCH":      scope.personalCardsTypeModel[scope.personalCardsTypeModel.length] = scope.personalCardsTypeOptions[x][5]; break;
				case "SOLO":        scope.personalCardsTypeModel[scope.personalCardsTypeModel.length] = scope.personalCardsTypeOptions[x][6]; break;
				default:            break;
			}

			scope.$apply();
		};
		// TODO
		var dataToSend = "";//"xtoken=" + rootScope.xtoken;
		dataToSend += "&type=" + scope.personalCardTypeModel.value;
		dataToSend += "&number=" + $("#personalCardNumber").val();
		dataToSend += "&holder=" + $("#personalCardHolderName").val();
		dataToSend += "&expiry_date=" + $("#personalCardExpiryDate").val();
		callServer("account/add-credit-card", dataToSend, success, function (response) {});
	}

	function profileUpdateCreditCard(scope, location, rootScope, route, index) {
		if (scope.personalCardsTypeModel[index].value == "") {
			showAlertBootStrapMsg("warning", "Please choose a type !");
			return;
		}
		if (!$("#personalCardHolderName-" + index)[0].checkValidity()) {
			showAlertBootStrapMsg("warning", "Invalid holder name!");
			return;
		}
		if (!$("#personalCardExpiryDate-" + index)[0].checkValidity()) {
			showAlertBootStrapMsg("warning", "Invalid date !");
			return;
		}
		var success = function (response) {
			scope.$apply();
		};
		var dataToSend = "";//"xtoken=" + rootScope.xtoken;
		dataToSend += "&card_id=" + scope.creditCards[index].uuid;
		dataToSend += "&type=" + scope.personalCardsTypeModel[index].value;
		dataToSend += "&holder=" + $("#personalCardHolderName-" + index).val();
		dataToSend += "&expiry_date=" + $("#personalCardExpiryDate-" + index).val();
		callServer("account/add-credit-card", dataToSend, success, function (response) {});
	}

	function profileDeleteCreditCard(scope, location, rootScope, route, index) {
		var success = function (response) {
			scope.creditCards.splice(index, 1);
			scope.personalCardsTypeOptions.splice(index, 1);
			scope.personalCardsTypeModel.splice(index, 1);
			scope.$apply();
		};
		var dataToSend = "";//"xtoken=" + rootScope.xtoken;
		dataToSend += "&card_id=" + scope.creditCards[index].uuid;
		callServer("account/delete-credit-card", dataToSend, success, function (response) {});
	}

// PROFILE FUNCTIONS
	function saveProfile(scope, location, rootScope, route) {
		if(!validateProfileForm(scope, location, rootScope, route))
			return;
		var dataToSend = getProfileFormData(scope, location, rootScope, route);
		var success = function (response) {
			if (rootScope.createPage) {
				if(indexPage == true)
					$location.path("/home");
				if(merchantPage == true || customerPage == true)
					$location.path("/login");
			} else {
				location.path("/listTransactions");
			}
			scope.$apply();
			location.replace();
		};
		postOnServer("account/update-profile", dataToSend, success, function (response) {});
	}

	function getProfileFormData(scope, location, rootScope, route) {
		var data = "";//"xtoken=" + rootScope.xtoken;
		/*
		 if(!rootScope.createPage) {
		 data += "&user.id=" + rootScope.userProfile.account.uuid;
		 }
		 */
//    data += "&user.type=" + ((rootScope.isMerchant) ? "MERCHANT" : "CUSTOMER");

//    data += "&user.email=" + $("#profileEmail").val();
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

		if (rootScope.isMerchant) {
			data += "&payment_method=" + scope.creditCardModeModel.value;
			data += "&cb_provider=" + ((scope.creditCardProviderModel != "") ? scope.creditCardProviderModel.value : "");
			switch (scope.creditCardProviderModel.value) {
				case "PAYLINE":
					data += "&payline_account=" + $("#paylineAccount").val();
					data += "&payline_key=" + $("#paylineKey").val();
					data += "&payline_contract=" + $("#paylineContract").val();
					data += "&payline_custom_payment_page_code=" + $("#paylineCustomPageCode").val();
					data += "&payline_custom_payment_template_url=" + $("#paylineTemplateURL").val();
					break;
				case "PAYBOX":
					data += "&paymentProviderParam.payboxContract=" + ((scope.payboxContractTypeModel != "") ? scope.payboxContractTypeModel.value : "");
					data += "&paybox_site=" + $("#payboxSite").val();
					data += "&paybox_key=" + $("#payboxKey").val();
					data += "&paybox_rank=" + $("#payboxContract").val();
					data += "&paybox_merchant_id=" + $("#payboxMerchantId").val();
					break;
				case "SIPS":
					data += "&sips_merchant_id=" + $("#sipsMerchantId").val();
					data += "&sips_merchant_country=" + $("#sipsCountry").val();
					data += "&sips_merchant_certificate_file_name=" + scope.sipsCetificateFileName;
					data += "&sips_merchant_certificate_file_content=" + scope.sipsCetificateFileContent;
					data += "&sips_merchant_parcom_file_name=" + scope.sipsParcomFileName;
					data += "&sips_merchant_parcom_file_content=" + scope.sipsParcomFileContent;
					data += "&sips_merchant_logo_path=" + $("#sipsLogoPath").val();
					break;
				case "SYSTEMPAY":
					data += "&systempay_shop_id=" + $("#systempayShopId").val();
					data += "&systempay_contract_number=" + $("#systempayContract").val();
					data += "&systempay_certificate=" + $("#systempayCertificate").val();
					break;
				case "AUTHORIZENET":
					data += "&authorize_net_api_login_id=" + $("#authorizeNetLogin").val();
					data += "&authorize_net_transaction_key=" + $("#authorizeNetTransactionKey").val();
					break;
				default:
					break;
			}

//PAYPAL INFO
			data += "&paypal_user=" + $("#paypalUser").val();
			data += "&paypal_password=" + $("#paypalPassword").val();
			data += "&paypal_signature=" + $("#paypalSignature").val();

//KWIXO INFO
			data += "&kwixo_params=" + $("#kwixoParams").val();

//AUTH INFO
			data += "&email_field=" + $("#authEmailField").val();
			data += "&password_field=" + $("#authPasswordField").val();
			data += "&callback_prefix=" + $("#authCallbackdomainField").val();
			data += "&password_pattern=" + $("#authPasswordRegex").val();

//PASSWORD INFO
			if(!rootScope.createPage) {
				data += "&password_subject=" + $("#passwordSubject").val();
				data += "&password_content=" + $("#passwordContent").val();
			}
		}

		return data;
	}

	function validateProfileForm(scope, location, rootScope, route) {
		if($("#profileEmail").val() == "" || $("#profileCompanyName").val() == "" || $("#profileWebsite").val() == "" || $("#profilePhoneNumber").val() == ""
			|| $("#profileFirstName").val() == "" || $("#profileLastName").val() == "" || $("#profileBirthDate").val() == ""
			|| !scope.profileCountriesModel || scope.profileCountriesModel == "" || $("#profileCity").val() == "" || $("#profileRoad").val() == ""
			|| $("#profilePostalCode").val() == "") {
			$(".nav-tabs a[data-target='#profileInfo']").tab("show");
			showAlertBootStrapMsg("warning", "Please fill all required fields");
			return false;
		}
		if(rootScope.createPage) {
			if($("#profilePassword").val() == "" || $("#profileConfirmPassword").val() == "" || $("#captchaText").val() == "") {
				$(".nav-tabs a[data-target='#profileInfo']").tab("show");
				showAlertBootStrapMsg("warning", "Please fill all required fields");
				return false;
			}
		}
		if(rootScope.isMerchant) {
			if(!scope.creditCardModeModel || scope.creditCardModeModel == "") {
				$(".nav-tabs a[data-target='#creditCard']").tab("show");
				showAlertBootStrapMsg("warning", "Please fill all required fields");
				return false;
			}
			if(!scope.creditCardProviderModel || scope.creditCardProviderModel == "") {
				$(".nav-tabs a[data-target='#creditCard']").tab("show");
				showAlertBootStrapMsg("warning", "Please fill all required fields");
				return false;
			}
//			if($("#authEmailField").val() == "" || $("#authPasswordField").val() == "") {
//				$(".nav-tabs a[data-target='#auth']").tab("show");
//				showAlertBootStrapMsg("warning", "Please fill all required fields");
//				return false;
//			}
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
			$(".nav-tabs a[data-target='#profileInfo']").tab("show");
			$("#profileBirthDate").focus();
			showAlertBootStrapMsg("warning", "Invalid birth date !");
			return false;
		}
		if(rootScope.isMerchant) {
			if(scope.creditCardProviderModel != "" && scope.creditCardProviderModel.value == "PAYLINE" && !$("#paylineTemplateURL")[0].checkValidity()) {
				$(".nav-tabs a[data-target='#creditCard']").tab("show");
				$("#paylineTemplateURL").focus();
				showAlertBootStrapMsg("warning", "Invalid URL!");
				return false;
			}
			if(scope.creditCardProviderModel != "" && scope.creditCardProviderModel.value == "AUTHORIZENET") {
				if(!$("#authorizeNetLogin")[0].checkValidity()){
					$(".nav-tabs a[data-target='#creditCard']").tab("show");
					$("#authorizeNetLogin").focus();
					showAlertBootStrapMsg("warning", "AUTHORIZENET API Login ID and Transaction Key are related!");
					return false;
				}
				else if(!$("#authorizeNetTransactionKey")[0].checkValidity()){
					$(".nav-tabs a[data-target='#creditCard']").tab("show");
					$("#authorizeNetTransactionKey").focus();
					showAlertBootStrapMsg("warning", "AUTHORIZENET API Login ID and Transaction Key are related!");
					return false;
				}
			}
			if(!$("#authPasswordRegex")[0].checkValidity()) {
				$(".nav-tabs a[data-target='#auth']").tab("show");
				$("#authPasswordRegex").focus();
				showAlertBootStrapMsg("warning", "Invalid password regex !");
				return false;
			}
		}
		return true;
	}
}
