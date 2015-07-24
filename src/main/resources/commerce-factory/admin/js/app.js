/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */

"use strict";

/* App Module */

var mogopay = angular.module("mogopay",  ["ngRoute","ngI18n"]).
    config(["$routeProvider", "$httpProvider", function($routeProvider, $httpProvider) {
        $routeProvider.
            when("/home",				{templateUrl: "partials/home.html",					controller: HomeCtrl}).
            when("/login",				{templateUrl: "partials/login.html",				controller: LoginCtrl}).
            when("/listTransactions",	{templateUrl: "partials/listTransactions.html",		controller: ListTransactionsCtrl}).
            when("/listCustomers",		{templateUrl: "partials/listCustomers.html",		controller: ListCustomersCtrl}).
            when("/profile",			{templateUrl: "partials/profile.html",				controller: ProfileCtrl}).
            when("/details",			{templateUrl: "partials/details.html",				controller: DetailsCtrl}).
            when("/signup",				{templateUrl: "partials/signup.html",				controller: SignupCtrl}).
            when("/passwordChange",		{templateUrl: "partials/passwordChange.html",		controller: PasswordChangeCtrl}).
            when("/validation",			{templateUrl: "partials/validation.html",			controller: ValidationCtrl}).
            when("/return",				{templateUrl: "partials/return.html",				controller: ReturnCtrl}).
            otherwise({redirectTo: "/home"} );
        $httpProvider.responseInterceptors.push(function($q, $rootScope) {
            return function (promise) {
                return promise.then(function (response) {
                    $rootScope.request = response.config;
                    return response;
                }, function (response) {
                    return $q.reject(response);
                });
            };
        });
    }]);

mogopay.value("ngI18nConfig", {
    //defaultLocale should be in lowercase and is required!!
    defaultLocale:"en",
    //supportedLocales is required - all locales should be in lowercase!!
    supportedLocales:["en", "fr"],
    //without leading and trailing slashes, default is i18n
    basePath:"../js/i18n",
    //default is false
    cache:true
});

mogopay.directive('ngFileChange', function() {
	return {
		restrict: 'A',
		link: function (scope, element, attrs) {
			var onChangeHandler = scope.$eval(attrs.ngFileChange);
			element.bind('change', function(evt){onChangeHandler.call(this, evt);});
		}
	};
});