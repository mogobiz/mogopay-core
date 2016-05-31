/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */

function ProfileCtrl($scope, $location, $rootScope, $route) {
    if (!isConnectedUser($scope, $location, $rootScope, $route))
        return;

    if ($rootScope.userProfile && $rootScope.userProfile.account && $rootScope.userProfile.account.paymentConfig && $rootScope.userProfile.account.paymentConfig.cbParam)
        $scope.cbParam = JSON.parse($rootScope.userProfile.account.paymentConfig.cbParam);
    if ($rootScope.userProfile && $rootScope.userProfile.account && $rootScope.userProfile.account.paymentConfig && $rootScope.userProfile.account.paymentConfig.applePayParam)
        $scope.applePayParam = JSON.parse($rootScope.userProfile.account.paymentConfig.applePayParam);

    //Main Variables
    var profileCompanyNameChanged = false;
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
    if ($rootScope.userProfile) {
        $scope.birthDateValue = dateToDateValue(new Date($rootScope.userProfile.account.birthDate));
        $scope.userUUID = $rootScope.userProfile.account.secret;
        $scope.creditCards = $rootScope.userProfile.cards;
        for (var i = 0; i < $scope.creditCards.length; i++)
            $scope.creditCards[i].expiryDateVal = dateToMonthValue(new Date($scope.creditCards[i].expiryDate));
    }
    $scope.sipsCetificateFileLabel = $rootScope.resourceBundle.sips_upload_cetificate;
    $scope.sipsCetificateFileName = "";
    $scope.sipsCetificateFileContent = "";
    if ($rootScope.userProfile && $rootScope.userProfile.paymentProviderParam && $rootScope.userProfile.paymentProviderParam.sipsMerchantCertificateFile) {
        $scope.sipsCetificateFileLabel = $rootScope.resourceBundle.sips_current_cetificate + " : " + $rootScope.userProfile.paymentProviderParam.sipsMerchantCertificateFile;
    }
    $scope.sipsParcomFileLabel = $rootScope.resourceBundle.sips_upload_parcom;
    $scope.sipsParcomFileName = "";
    $scope.sipsParcomFileContent = "";
    if ($rootScope.userProfile && $rootScope.userProfile.paymentProviderParam && $rootScope.userProfile.paymentProviderParam.sipsMerchantParcomFile) {
        $scope.sipsParcomFileLabel = $rootScope.resourceBundle.sips_current_parcom + " : " + $rootScope.userProfile.paymentProviderParam.sipsMerchantParcomFile;
    }

    //Options and Model
    //Civility
    $scope.profileCivilityOptions = [{"value": "MR", "name": "Mr."}, {"value": "MRS", "name": "Mrs."}];
    $scope.profileCivilityModel = $scope.profileCivilityOptions[0];
    if ($rootScope.userProfile && $rootScope.userProfile.account && $rootScope.userProfile.account.civility.name == "MRS") {
        $scope.profileCivilityModel = $scope.profileCivilityOptions[1];
    }

    //Countries
    $scope.profileCountriesOptions = [];
    profileLoadCountries($scope, $location, $rootScope, $route);

    //Credit Card Mode
    $scope.creditCardModeOptions = [
        {"value": "EXTERNAL", "name": $rootScope.resourceBundle.card_option_external},
        {"value": "THREEDS_NO", "name": $rootScope.resourceBundle.card_option_threedsNo},
        {"value": "THREEDS_IF_AVAILABLE", "name": $rootScope.resourceBundle.card_option_threedsIfAvailable},
        {"value": "THREEDS_REQUIRED", "name": $rootScope.resourceBundle.card_option_threedsRequired}
    ];
    $scope.creditCardModeModel = null;
    if ($rootScope.userProfile && $rootScope.userProfile.account && $rootScope.userProfile.account.paymentConfig && $rootScope.userProfile.account.paymentConfig.paymentMethod) {
        switch ($rootScope.userProfile.account.paymentConfig.paymentMethod.name) {
            case "EXTERNAL":
                $scope.creditCardModeModel = $scope.creditCardModeOptions[0];
                break;
            case "THREEDS_NO":
                $scope.creditCardModeModel = $scope.creditCardModeOptions[1];
                break;
            case "THREEDS_IF_AVAILABLE":
                $scope.creditCardModeModel = $scope.creditCardModeOptions[2];
                break;
            case "THREEDS_REQUIRED":
                $scope.creditCardModeModel = $scope.creditCardModeOptions[3];
                break;
            default:
                break;
        }
    }

    //Card Provider
    $scope.creditCardProviderOptions = providersList; // see providersList.js
    $scope.creditCardProviderModel = null;
    $scope.creditCardProviderTemplate = "";

    if ($rootScope.userProfile && $rootScope.userProfile.account && $rootScope.userProfile.account.paymentConfig && $rootScope.userProfile.account.paymentConfig.cbProvider) {
        switch ($rootScope.userProfile.account.paymentConfig.cbProvider.name.toLowerCase()) {
            case "none":
                $scope.creditCardProviderModel = $scope.creditCardProviderOptions[0];
                break;
            case "payline":
                $scope.creditCardProviderModel = $scope.creditCardProviderOptions[1];
                break;
            case "paybox":
                $scope.creditCardProviderModel = $scope.creditCardProviderOptions[2];
                break;
            case "sips":
                $scope.creditCardProviderModel = $scope.creditCardProviderOptions[3];
                break;
            case "systempay":
                $scope.creditCardProviderModel = $scope.creditCardProviderOptions[4];
                break;
            case "authorizenet":
                $scope.creditCardProviderModel = $scope.creditCardProviderOptions[5];
                break;
            case "custom":
                $scope.creditCardProviderModel = $scope.creditCardProviderOptions[6];
                break;
            default:
                break;
        }
        if ($scope.creditCardProviderModel && $scope.creditCardProviderModel.value.toLowerCase() != "none")
            $scope.creditCardProviderTemplate = "partials/providers/" + $scope.creditCardProviderModel.value + ".html";
    }
    $scope.creditCardProviderChange = function () {
        creditCardProviderChange($scope, $location, $rootScope, $route)
    };

    //Paybox Contract
    $scope.payboxContractTypeOptions = [
        {"value": "PAYBOX_SYSTEM", "name": "PAYBOX SYSTEM"},
        {"value": "PAYBOX_DIRECT", "name": "PAYBOX DIRECT"},
        {"value": "PAYBOX_DIRECT_PLUS", "name": "PAYBOX DIRECT PLUS"}
    ];
    $scope.payboxContractTypeModel = $scope.payboxContractTypeOptions[0];
    if ($rootScope.userProfile && $rootScope.userProfile.paymentProviderParam && $rootScope.userProfile.paymentProviderParam.payboxContract) {
        switch ($rootScope.userProfile.paymentProviderParam.payboxContract) {
            case "PAYBOX_SYSTEM":
                $scope.payboxContractTypeModel = $scope.payboxContractTypeOptions[0];
                break;
            case "PAYBOX_DIRECT":
                $scope.payboxContractTypeModel = $scope.payboxContractTypeOptions[1];
                break;
            case "PAYBOX_DIRECT_PLUS":
                $scope.payboxContractTypeModel = $scope.payboxContractTypeOptions[2];
                break;
            default:
                break;
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
    if ($rootScope.userProfile && $rootScope.userProfile.cards) {
        for (var i = 0; i < $rootScope.userProfile.cards.length; i++) {
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
            switch ($rootScope.userProfile.cards[i].cardType.name) {
                case "VISA":
                    $scope.personalCardsTypeModel[$scope.personalCardsTypeModel.length] = $scope.personalCardsTypeOptions[len][0];
                    break;
                case "MASTER_CARD":
                    $scope.personalCardsTypeModel[$scope.personalCardsTypeModel.length] = $scope.personalCardsTypeOptions[len][1];
                    break;
                case "AMEX":
                    $scope.personalCardsTypeModel[$scope.personalCardsTypeModel.length] = $scope.personalCardsTypeOptions[len][2];
                    break;
                case "CB":
                    $scope.personalCardsTypeModel[$scope.personalCardsTypeModel.length] = $scope.personalCardsTypeOptions[len][3];
                    break;
                case "DISCOVER":
                    $scope.personalCardsTypeModel[$scope.personalCardsTypeModel.length] = $scope.personalCardsTypeOptions[len][4];
                    break;
                case "SWITCH":
                    $scope.personalCardsTypeModel[$scope.personalCardsTypeModel.length] = $scope.personalCardsTypeOptions[len][5];
                    break;
                case "SOLO":
                    $scope.personalCardsTypeModel[$scope.personalCardsTypeModel.length] = $scope.personalCardsTypeOptions[len][6];
                    break;
                default:
                    break;
            }
        }
    }

    //Main Functions
    $scope.goToListTrasactions = function () {
        navigateToPage($scope, $location, $rootScope, $route, "listTransactions");
    };
    $scope.goToListCustomers = function () {
        navigateToPage($scope, $location, $rootScope, $route, "listCustomers");
    };
    $scope.profileChangePassword = function () {
        navigateToPage($scope, $location, $rootScope, $route, "changePassword");
    };

    setTimeout(function () {
        $("#profileCompanyName").change(function () {
            profileCompanyNameChanged = true;
        });
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
        $("#applePayLogin").change(function () {
            if ($(this).val() != "")
                $("#applePayTransactionKey").attr("required", "required");
            else
                $("#applePayTransactionKey").removeAttr("required");
        });
        $("#applePayTransactionKey").change(function () {
            if ($(this).val() != "")
                $("#applePayLogin").attr("required", "required");
            else
                $("#applePayLogin").removeAttr("required");
        });
    }, 250);

    $scope.displayUserUUID = function () {
        $scope.showUserUUID = !$scope.showUserUUID;
    };
    $scope.renewUserUUID = function () {
        profileRenewUserUUID($scope, $location, $rootScope, $route);
    };

    $scope.profileResendMailConfirmation = function () {
        profileResendMailConfirmation($scope, $location, $rootScope, $route)
    };
    $scope.profileResendPhoneValidation = function () {
        profileResendPhoneValidation($scope, $location, $rootScope, $route)
    };

    $scope.profileCheckPhoneNumberForCountry = function () {
        profileCheckPhoneNumberForCountry($scope, $location, $rootScope, $route);
    };

    $scope.profileCheckCreditCardNumber = function (index) {
        var id = "personalCardNumber";
        if (index)
            id += "-" + index;
        $("#" + id)[0].setCustomValidity(luhn10($("#" + id).val()) == true ? "" : $rootScope.resourceBundle.error_invalid_card);
        if (!luhn10($("#" + id).val())) {
            $("#" + id).focus();
            showAlertBootStrapMsg("warning", $rootScope.resourceBundle.error_invalid_card);
        }
    };

    $scope.profileCheckPasswordRegExValidity = function () {
        profileCheckPasswordRegExValidity($scope, $location, $rootScope, $route);
    };

    $scope.profileLoadStatesForCountry = function () {
        profileLoadStatesForCountry($scope, $location, $rootScope, $route);
    };
    $scope.profileLoadRegionsForState = function () {
        profileLoadRegionsForState($scope, $location, $rootScope, $route);
    };

    $scope.saveProfile = function () {
        setTimeout(function () {
            saveProfile($scope, $location, $rootScope, $route);
        }, 250);
    };

    $scope.addCard = function () {
        profileAddCreditCard($scope, $location, $rootScope, $route);
    };
    $scope.updateCard = function (index) {
        profileUpdateCreditCard($scope, $location, $rootScope, $route, index);
    };
    $scope.deleteCard = function (index) {
        profileDeleteCreditCard($scope, $location, $rootScope, $route, index);
    };

    $scope.sipsCetificateFileChangeContent = function (evt) {
        sipsCetificateFileChangeContent(evt, $scope, $location, $rootScope, $route);
    };
    $scope.sipsParcomFileChangeContent = function (evt) {
        sipsParcomFileChangeContent(evt, $scope, $location, $rootScope, $route);
    };

    function luhn10(a, b, c, d, e) {
        if (a == "")
            return true;
        for (d = +a[b = a.length - 1], e = 0; b--;)
            c = +a[b], d += ++e % 2 ? 2 * c % 10 + (c > 4) : c;
        return !(d % 10);
    }

    function profileRenewUserUUID(scope, location, rootScope, route) {
        var success = function (response) {
            scope.userUUID = response.uuid;
            scope.$apply();
        }
        callServer("account/generate-new-secret", "", success, emptyFunc, "GET", "params", "pay", true, true, true);
    }

    function profileResendMailConfirmation(scope, location, rootScope, route) {
        callServer("account/generateNewEmailCode", "", emptyFunc, emptyFunc, "GET", "params", "pay", true, true, true);
    }

    function profileResendPhoneValidation(scope, location, rootScope, route) {
        callServer("account/generateNewPhoneCode", "", emptyFunc, emptyFunc, "GET", "params", "pay", true, true, true);
    }

    function profileCheckPhoneNumberForCountry(scope, location, rootScope, route) {
        if (!scope.profileCountriesModel || scope.profileCountriesModel == "") {
            showAlertBootStrapMsg("warning", rootScope.resourceBundle.error_select_country);
            $("#profilePhoneNumber").val("");
            return;
        }
        var success = function (response) {
            if (response['isValid'] == true) {
                $("#profilePhoneNumber").val(response['nationalFormat']);
            } else {
                showAlertBootStrapMsg("warning", rootScope.resourceBundle.error_select_country);
                $("#profilePhoneNumber").val("");
            }
        };
        var action = "country/" + scope.profileCountriesModel.code + "/check-phone-number/" + $("#profilePhoneNumber").val();
        callServer(action, "", success, emptyFunc, "GET", "params", "pay", false, false, false);
    }

    function profileCheckPasswordRegExValidity(scope, location, rootScope, route) {
        var success = function (response) {
            $("#authPasswordRegex")[0].setCustomValidity(response == "false" ? rootScope.resourceBundle.error_invalid_regex : "");
            if (response == "false") {
                $("#authPasswordRegex").focus();
                showAlertBootStrapMsg("warning", rootScope.resourceBundle.error_invalid_regex);
            }
        };
        var pattern = encodeURIComponent($("#authPasswordRegex").val());
        callServer("account/is-pattern-valid/" + pattern, "", success, emptyFunc, "GET", "params", "pay", false, false, false);
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
        callServer("country/cities", dataToSend, success, emptyFunc, "GET", "params", "pay", false, false, false);
    }

    function profileLoadCountries(scope, location, rootScope, route) {
        var success = function (response) {
            scope.profileCountriesOptions = response;
            scope.profileCountriesModel = "";
            if (rootScope.userProfile && rootScope.userProfile.account.address.country) {
                for (var i = 0; i < scope.profileCountriesOptions.length; i++) {
                    if (scope.profileCountriesOptions[i].code == rootScope.userProfile.account.address.country) {
                        scope.profileCountriesModel = scope.profileCountriesOptions[i];
                        scope.$apply();
                        profileLoadStatesForCountry(scope, location, rootScope, route);
                        break;
                    }
                }
            }
            scope.$apply();
        }
        callServer("country/countries-for-billing", "", success, emptyFunc, "GET", "params", "pay", false, true, true);
    }

    function profileLoadStatesForCountry(scope, location, rootScope, route) {
        if (!scope.profileCountriesModel || scope.profileCountriesModel == "") {
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
            if (rootScope.userProfile && rootScope.userProfile.account.address.admin1) {
                for (var i = 0; i < scope.profileStateOptions.length; i++) {
                    if (scope.profileStateOptions[i].code == rootScope.userProfile.account.address.admin1) {
                        scope.profileStateModel = scope.profileStateOptions[i];
                        scope.$apply();
                        profileLoadRegionsForState(scope, location, rootScope, route);
                        break;
                    }
                }
            }
            scope.$apply();
        };
        callServer("country/admins1/" + scope.profileCountriesModel.code, "", success, emptyFunc, "GET", "params", "pay", true, true, true);
    }

    function profileLoadRegionsForState(scope, location, rootScope, route) {
        if (!scope.profileStateModel || scope.profileStateModel == "") {
            scope.profileRegionOptions = [];
            scope.profileRegionModel = "";
            return;
        }
        var success = function (response) {
            scope.profileRegionOptions = response;
            scope.profileRegionModel = "";
            if (rootScope.userProfile && rootScope.userProfile.account.address.admin2) {
                for (var i = 0; i < scope.profileRegionOptions.length; i++) {
                    if (scope.profileRegionOptions[i].code == rootScope.userProfile.account.address.admin2) {
                        scope.profileRegionModel = scope.profileRegionOptions[i];
                        break;
                    }
                }
            }
            scope.$apply();
        };
        var dataToSend = "country=" + scope.profileCountriesModel.code + "&state=" + scope.profileStateModel.code;
        callServer("country/admins2/" + scope.profileStateModel.code, "", success, emptyFunc, "GET", "params", "pay", true, true, true);
    }

    function sipsCetificateFileChangeContent(evt, scope, location, rootScope, route) {
        var f = evt.target.files[0];
        if (f) {
            var r = new FileReader();
            r.onload = function (e) {
                scope.sipsCetificateFileName = f.name;
                scope.sipsCetificateFileContent = e.target.result;
                scope.sipsCetificateFileLabel = rootScope.resourceBundle.sips_current_cetificate + " : " + f.name;
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
                scope.sipsParcomFileLabel = rootScope.resourceBundle.sips_current_parcom + " : " + f.name;
                scope.$apply();
            }
            r.readAsText(f);
        }
    }

// CREDIT CARD PROVIDER FUNCTION
    function creditCardProviderChange(scope, location, rootScope, route) {
        if (scope.creditCardProviderModel.value == "none")
            scope.creditCardProviderTemplate = "";
        else
            scope.creditCardProviderTemplate = "partials/providers/" + scope.creditCardProviderModel.value + ".html";
    }

// PERSONAL CARDS FUNCTIONS
    function profileAddCreditCard(scope, location, rootScope, route) {
        if (scope.personalCardTypeModel.value == "") {
            showAlertBootStrapMsg("warning", rootScope.resourceBundle.error_choose_type);
            return;
        }
        if (!$("#personalCardNumber")[0].checkValidity()) {
            if ($("#personalCardNumber").val() == "")
                showAlertBootStrapMsg("warning", rootScope.resourceBundle.error_invalid_card);
            return;
        }
        if (!$("#personalCardHolderName")[0].checkValidity()) {
            showAlertBootStrapMsg("warning", rootScope.resourceBundle.error_invalid_holder);
            return;
        }
        if (!$("#personalCardExpiryDate")[0].checkValidity()) {
            showAlertBootStrapMsg("warning", rootScope.resourceBundle.error_invalid_date);
            return;
        }
        var success = function (response) {
            $("#personalCardNumber").val("");
            $("#personalCardHolderName").val("");
            $("#personalCardExpiryDate").val("");
            $("#personalCardType").prop('selectedIndex', 0);

            scope.creditCards.push(response);
            scope.creditCards[scope.creditCards.length - 1].expiryDateVal = dateToMonthValue(new Date(scope.creditCards[scope.creditCards.length - 1].expiryDate));

            var x = scope.creditCards.length - 1;
            scope.personalCardsTypeOptions.push([
                {"value": "VISA", "name": "VISA"},
                {"value": "MASTER_CARD", "name": "MASTER CARD"},
                {"value": "AMEX", "name": "AMEX"},
                {"value": "CB", "name": "CB"},
                {"value": "DISCOVER", "name": "DISCOVER"},
                {"value": "SWITCH", "name": "SWITCH"},
                {"value": "SOLO", "name": "SOLO"}
            ]);
            switch (response.cardType.name) {
                case "VISA":
                    scope.personalCardsTypeModel[scope.personalCardsTypeModel.length] = scope.personalCardsTypeOptions[x][0];
                    break;
                case "MASTER_CARD":
                    scope.personalCardsTypeModel[scope.personalCardsTypeModel.length] = scope.personalCardsTypeOptions[x][1];
                    break;
                case "AMEX":
                    scope.personalCardsTypeModel[scope.personalCardsTypeModel.length] = scope.personalCardsTypeOptions[x][2];
                    break;
                case "CB":
                    scope.personalCardsTypeModel[scope.personalCardsTypeModel.length] = scope.personalCardsTypeOptions[x][3];
                    break;
                case "DISCOVER":
                    scope.personalCardsTypeModel[scope.personalCardsTypeModel.length] = scope.personalCardsTypeOptions[x][4];
                    break;
                case "SWITCH":
                    scope.personalCardsTypeModel[scope.personalCardsTypeModel.length] = scope.personalCardsTypeOptions[x][5];
                    break;
                case "SOLO":
                    scope.personalCardsTypeModel[scope.personalCardsTypeModel.length] = scope.personalCardsTypeOptions[x][6];
                    break;
                default:
                    break;
            }
            scope.$apply();
        };

        var dataToSend = "";
        dataToSend += "type=" + scope.personalCardTypeModel.value;
        dataToSend += "&number=" + $("#personalCardNumber").val();
        dataToSend += "&holder=" + $("#personalCardHolderName").val();
        dataToSend += "&expiry_date=" + $("#personalCardExpiryDate").val();
        callServer("account/add-credit-card", dataToSend, success, emptyFunc, "GET", "params", "pay", true, true, true);
    }

    function profileUpdateCreditCard(scope, location, rootScope, route, index) {
        if (scope.personalCardsTypeModel[index].value == "") {
            showAlertBootStrapMsg("warning", rootScope.resourceBundle.error_choose_type);
            return;
        }
        if (!$("#personalCardHolderName-" + index)[0].checkValidity()) {
            showAlertBootStrapMsg("warning", rootScope.resourceBundle.error_invalid_holder);
            return;
        }
        if (!$("#personalCardExpiryDate-" + index)[0].checkValidity()) {
            showAlertBootStrapMsg("warning", rootScope.resourceBundle.error_invalid_date);
            return;
        }
        var success = function (response) {
            scope.$apply();
        };
        var dataToSend = "";
        dataToSend += "card_id=" + scope.creditCards[index].uuid;
        dataToSend += "&type=" + scope.personalCardsTypeModel[index].value;
        dataToSend += "&holder=" + $("#personalCardHolderName-" + index).val();
        dataToSend += "&expiry_date=" + $("#personalCardExpiryDate-" + index).val();
        callServer("account/add-credit-card", dataToSend, success, emptyFunc, "GET", "params", "pay", true, true, true);
    }

    function profileDeleteCreditCard(scope, location, rootScope, route, index) {
        var success = function (response) {
            scope.creditCards.splice(index, 1);
            scope.personalCardsTypeOptions.splice(index, 1);
            scope.personalCardsTypeModel.splice(index, 1);
            scope.$apply();
        };
        var dataToSend = "";
        dataToSend += "&card_id=" + scope.creditCards[index].uuid;
        callServer("account/delete-credit-card", dataToSend, success, emptyFunc, "GET", "params", "pay", true, true, true);
    }

// PROFILE FUNCTIONS
    function saveProfile(scope, location, rootScope, route) {
        if (!validateProfileForm(scope, location, rootScope, route))
            return;
        var dataToSend = getProfileFormData(scope, location, rootScope, route);
        var action = rootScope.isMerchant ? "update-merchant-profile" : "update-customer-profile"
        var success = function (response) {
            if (profileCompanyNameChanged)
                rootScope.getAllStores();
            else
                navigateToPage(scope, location, rootScope, route, "listTransactions");
        };
        callServer("account/" + action, dataToSend, success, emptyFunc, "POST", "params", "pay", true, true, true);
    }

    function getProfileFormData(scope, location, rootScope, route) {
        var data = "";
        data += "lphone=" + $("#profilePhoneNumber").val();
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
            data += "&company=" + $("#profileCompanyName").val();
            data += "&website=" + $("#profileWebsite").val();
            data += "&payment_method=" + scope.creditCardModeModel.value;
            data += "&cb_provider=" + ((scope.creditCardProviderModel != null) ? scope.creditCardProviderModel.value : "");

            switch (scope.creditCardProviderModel.value) {
                case "payline":
                    data += "&payline_account=" + $("#paylineAccount").val();
                    data += "&payline_key=" + $("#paylineKey").val();
                    data += "&payline_contract=" + $("#paylineContract").val();
                    data += "&payline_custom_payment_page_code=" + $("#paylineCustomPageCode").val();
                    data += "&payline_custom_payment_template_url=" + $("#paylineTemplateURL").val();
                    break;
                case "paybox":
                    data += "&paybox_contract=" + ((scope.payboxContractTypeModel != "") ? scope.payboxContractTypeModel.value : "");
                    data += "&paybox_site=" + $("#payboxSite").val();
                    data += "&paybox_key=" + $("#payboxKey").val();
                    data += "&paybox_rank=" + $("#payboxContract").val();
                    data += "&paybox_merchant_id=" + $("#payboxMerchantId").val();
                    break;
                case "sips":
                    data += "&sips_merchant_id=" + $("#sipsMerchantId").val();
                    data += "&sips_merchant_country=" + $("#sipsCountry").val();
                    data += "&sips_merchant_certificate_file_name=" + scope.sipsCetificateFileName;
                    data += "&sips_merchant_certificate_file_content=" + scope.sipsCetificateFileContent;
                    data += "&sips_merchant_parcom_file_name=" + scope.sipsParcomFileName;
                    data += "&sips_merchant_parcom_file_content=" + scope.sipsParcomFileContent;
                    data += "&sips_merchant_logo_path=" + $("#sipsLogoPath").val();
                    break;
                case "systempay":
                    data += "&systempay_shop_id=" + $("#systempayShopId").val();
                    data += "&systempay_contract_number=" + $("#systempayContract").val();
                    data += "&systempay_certificate=" + $("#systempayCertificate").val();
                    break;
                case "authorizenet":
                    data += "&anet_api_login_id=" + $("#authorizeNetLogin").val();
                    data += "&anet_transaction_key=" + $("#authorizeNetTransactionKey").val();
                    data += "&anet_md5=" + $("#authorizeNetMD5Key").val();
                    break;
                case "custom":
                    data += "&custom_provider_name=" + $("#customProviderName").val();
                    data += "&custom_provider_data=" + $("#customProviderData").val();
                    break;
                default:
                    break;
            }

//PAYPAL INFO
            data += "&paypal_user=" + $("#paypalUser").val();
            data += "&paypal_password=" + $("#paypalPassword").val();
            data += "&paypal_signature=" + $("#paypalSignature").val();

//APPLE PAY INFO
            data += "&apple_pay_anet_api_login_id=" + $("#applePayLogin").val();
            data += "&apple_pay_anet_transaction_key=" + $("#applePayTransactionKey").val();

//AUTH INFO
            data += "&email_field=" + $("#authEmailField").val();
            data += "&password_field=" + $("#authPasswordField").val();
            data += "&callback_prefix=" + $("#authCallbackdomainField").val();
            data += "&password_pattern=" + $("#authPasswordRegex").val();

// GROUP PAYMENT INFO
            data += "&group_payment_return_url_for_next_payers=" + $("#groupPaymentUrlNextPayer").val();
            data += "&group_payment_success_url=" + $("#groupPaymentSuccessURL").val();
            data += "&group_payment_failure_url=" + $("#groupPaymentFailureURL").val();

//EMAIL INFO
            data += "&sender_email=" + $("#emailInfoSenderMail").val();
            data += "&sender_name=" + $("#emailInfoSenderName").val();
        }
        return data;
    }

    function validateProfileForm(scope, location, rootScope, route) {
        if ($("#profileCompanyName").val() == "" || $("#profileWebsite").val() == "" || $("#profilePhoneNumber").val() == ""
            || $("#profileFirstName").val() == "" || $("#profileLastName").val() == "" || $("#profileBirthDate").val() == ""
            || !scope.profileCountriesModel || scope.profileCountriesModel == "" || $("#profileCity").val() == "" || $("#profileRoad").val() == ""
            || $("#profilePostalCode").val() == "") {
            $(".nav-tabs a[data-target='#profileInfo']").tab("show");
            showAlertBootStrapMsg("warning", rootScope.resourceBundle.error_required);
            return false;
        }
        if (rootScope.isMerchant && !$("#profileCompanyName")[0].checkValidity()) {
            $(".nav-tabs a[data-target='#profileInfo']").tab("show");
            $("#profileCompanyName").focus();
            showAlertBootStrapMsg("warning", rootScope.resourceBundle.error_invalid_company_name);
            return false;
        }
        if (rootScope.isMerchant && !$("#profileWebsite")[0].checkValidity()) {
            $(".nav-tabs a[data-target='#profileInfo']").tab("show");
            $("#profileWebsite").focus();
            showAlertBootStrapMsg("warning", rootScope.resourceBundle.error_invalid_website);
            return false;
        }
        if (!$("#profileBirthDate")[0].checkValidity()) {
            $(".nav-tabs a[data-target='#profileInfo']").tab("show");
            $("#profileBirthDate").focus();
            showAlertBootStrapMsg("warning", rootScope.resourceBundle.error_invalid_birthdate);
            return false;
        }
        if (rootScope.isMerchant) {
            if (!scope.creditCardModeModel || scope.creditCardModeModel == "") {
                $(".nav-tabs a[data-target='#creditCard']").tab("show");
                showAlertBootStrapMsg("warning", rootScope.resourceBundle.error_required);
                return false;
            }
            if (!scope.creditCardProviderModel || scope.creditCardProviderModel == "") {
                $(".nav-tabs a[data-target='#creditCard']").tab("show");
                showAlertBootStrapMsg("warning", rootScope.resourceBundle.error_required);
                return false;
            }
            var inputs = $("#creditCardForm input");
            for (var i = 0; i < inputs.length; i++) {
                if (!$(inputs[i])[0].checkValidity()) {
                    $(".nav-tabs a[data-target='#creditCard']").tab("show");
                    $(inputs[i]).focus();
                    showAlertBootStrapMsg("warning", $(inputs[i]).attr("errorMessage"));
                    return false;
                }
            }
            var selects = $("#creditCardForm select");
            for (var i = 0; i < selects.length; i++) {
                if (!$(selects[i])[0].checkValidity()) {
                    $(".nav-tabs a[data-target='#creditCard']").tab("show");
                    $(selects[i]).focus();
                    showAlertBootStrapMsg("warning", $(selects[i]).attr("errorMessage"));
                    return false;
                }
            }
            if (!$("#applePayLogin")[0].checkValidity()) {
                $(".nav-tabs a[data-target='#applePay']").tab("show");
                $("#applePayLogin").focus();
                showAlertBootStrapMsg("warning", rootScope.resourceBundle.error_appele_pay);
                return false;
            }
            if (!$("#applePayTransactionKey")[0].checkValidity()) {
                $(".nav-tabs a[data-target='#applePay']").tab("show");
                $("#applePayTransactionKey").focus();
                showAlertBootStrapMsg("warning", rootScope.resourceBundle.error_appele_pay);
                return false;
            }
            if (!$("#authPasswordRegex")[0].checkValidity()) {
                $(".nav-tabs a[data-target='#auth']").tab("show"); 
                $("#authPasswordRegex").focus();
                showAlertBootStrapMsg("warning", rootScope.resourceBundle.error_invalid_pass_regex);
                return false;
            }
            if (!$("#groupPaymentUrlNextPayer")[0].checkValidity()) {
                $(".nav-tabs a[data-target='#groupPayment']").tab("show");
                $("#groupPaymentUrlNextPayer").focus();
                showAlertBootStrapMsg("warning", rootScope.resourceBundle.error_invalid_url);
                return false;
            }
            if (!$("#groupPaymentSuccessURL")[0].checkValidity()) {
                $(".nav-tabs a[data-target='#groupPayment']").tab("show");
                $("#groupPaymentSuccessURL").focus();
                showAlertBootStrapMsg("warning", rootScope.resourceBundle.error_invalid_url);
                return false;
            }
            if (!$("#groupPaymentFailureURL")[0].checkValidity()) {
                $(".nav-tabs a[data-target='#groupPayment']").tab("show");
                $("#groupPaymentFailureURL").focus();
                showAlertBootStrapMsg("warning", rootScope.resourceBundle.error_invalid_url);
                return false;
            }
            if (!$("#emailInfoSenderMail")[0].checkValidity()) {
                $(".nav-tabs a[data-target='#emailInfo']").tab("show");
                $("#emailInfoSenderMail").focus();
                showAlertBootStrapMsg("warning", rootScope.resourceBundle.error_invalid_email);
                return false;
            }
        }
        return true;
    }
}

function anetChangeLogin() {
    if ($("#authorizeNetLogin").val() != "")
        $("#authorizeNetTransactionKey").attr("required", "required");
    else
        $("#authorizeNetTransactionKey").removeAttr("required");
}

function anetChangeTransactionKey() {
    if ($("#authorizeNetTransactionKey").val() != "")
        $("#authorizeNetLogin").attr("required", "required");
    else
        $("#authorizeNetLogin").removeAttr("required");
}