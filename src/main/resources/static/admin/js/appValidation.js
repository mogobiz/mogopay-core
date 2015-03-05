"use strict";

/* App Module */

var mogopay = angular.module("mogopay",  ["ngRoute","ngI18n"]).
    config(["$routeProvider", "$httpProvider", function($routeProvider, $httpProvider) {
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