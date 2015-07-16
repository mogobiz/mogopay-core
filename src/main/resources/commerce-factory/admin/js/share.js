/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */

var serverUrl = "/api/pay/";
var storeUrl = "/api/store/";
var clientUrl = "/pay-client/";
var deployUrl = window.location.origin + window.location.pathname.substring(0, window.location.pathname.lastIndexOf("/") + 1);

function callClient(action, dataToSend, success, error){
    $.ajax({
        url :  clientUrl + action,
        type : "GET",
        data : dataToSend,
        cache : false,
        async : true,
        success : success,
        error: error
    });
}

function callServer(action, dataToSend, success, error){
    $.ajax({
        url :  serverUrl + action,
        type : "GET",
        data : dataToSend,
        cache : false,
        async : true,
        success : success,
        error: error
    });
}

function postOnServer(action, dataToSend, success, error){
    $.ajax({
        url :  serverUrl + action,
        type : "POST",
        data : encodeURI(dataToSend),
        cache : false,
        async : true,
        success : success,
        error: error
    });
}

function callServerJson(action, dataToSend, success, error){
    var afterCallingSuccess = function (response) {
        success(response);
    };
    var afterCallingError = function (response) {
        error(response);
    };
    $.ajax({
        url :  serverUrl + action,
        type : "PUT",
        data : JSON.stringify(dataToSend),
        dataType : "json",
        contentType: "application/json; charset=utf-8",
        cache : false,
        async : true,
        success : afterCallingSuccess,
        error: afterCallingError
    });
}

function callStoreServer(action, dataToSend, success, error, storeCode, type){
    $.ajax({
        url :  storeUrl + storeCode + "/" + action,
        type : type,
        data : dataToSend,
        cache : false,
        async : true,
        success : success,
        error: error
    });
}

function callStoreServerJson(action, dataToSend, success, error, storeCode, type){
    var afterCallingSuccess = function (response) {
        success(response);
    };
    var afterCallingError = function (response) {
        error(response);
    };
    $.ajax({
        url :  storeUrl + storeCode + "/" + action,
        type : type,
        data : JSON.stringify(dataToSend),
        contentType: "application/json; charset=utf-8",
        cache : false,
        async : true,
        success : afterCallingSuccess,
        error: afterCallingError
    });
}

function dateToString(date, withHours){
    var dateString = "";
    if(date){
        var day = ""+(parseInt(date.getDate()));
        if(day.length == 1)
            dateString += "0"+day;
        else
            dateString += day;
        var month = ""+(parseInt(date.getMonth())+1);
        if(month.length == 1)
            dateString += "/0"+month;
        else
            dateString += "/"+month;
        dateString += "/"+date.getFullYear();
        if(withHours)
            dateString +=" "+date.getHours()+":"+date.getSeconds();
    }
    return dateString;
}

function dateToDateValue(date){
	var dateValue = "";
    if(date){
		dateValue = date.getFullYear();
        var month = ""+(parseInt(date.getMonth())+1);
        if(month.length == 1)
            dateValue += "-0" + month;
        else
            dateValue += "-" + month;
        var day = ""+(parseInt(date.getDate()));
        if(day.length == 1)
            dateValue += "-0" + day;
        else
            dateValue += "-" + day;
    }
    return dateValue;
}

function dateToMonthValue(date){
	var monthValue = "";
    if(date){
		monthValue = date.getFullYear();
        var month = ""+(parseInt(date.getMonth())+1);
        if(month.length == 1)
            monthValue += "-0" + month;
        else
            monthValue += "-" + month;
    }
    return monthValue;
}

function showAlertBootStrapMsg(type, msg, wait){
    $("#notification").html("<div class='" + type + "' style='display: none;'> " + msg + "  <img src='../images/close.png' alt='' class='close' data-dismiss='alert' ></div>");
    $("."+type).show('slow');
    if(!wait){
        setTimeout(function() {
            $("."+type).delay(500).hide("slow");
            $("#notification").html("");
        }, 7000);
    }
}
var isEmail_re = /^\s*[\w\-\+_]+(\.[\w\-\+_]+)*\@[\w\-\+_]+\.[\w\-\+_]+(\.[\w\-\+_]+)*\s*$/;
function isEmail (s) {
    return String(s).search (isEmail_re) != -1;
}

function getHTTPParameter(nomParam){
	var chaineParam = document.location.search;
	if (chaineParam != null && chaineParam.length > 0)
	{
		chaineParam = chaineParam.substring(1);
		var tableauNomValeur = chaineParam.split("&");
		for (var i = 0; i < tableauNomValeur.length; i++)
		{
			var nomValeur = tableauNomValeur[i].split("=");
			if (nomValeur[0] == nomParam)
			{
				return decodeURI(nomValeur[1]);
			}
		}
	}
	return "";
}

function isConnectedUser(scope, location, rootScope, route){
	if (rootScope.userProfile == undefined || rootScope.userProfile == null) {
        if(indexPage == true && location.$$path != "/home"){
			location.path("/home");
		}
		if((merchantPage == true || customerPage == true) && location.$$path != "/login"){
			location.path("/login");
		}
		location.replace();
		if (scope.$root.$$phase != "$apply" && scope.$root.$$phase != "$digest") {
			scope.$apply();
		}
		return false;
    }
	return true;
}