//var serverUrl = "http://mogopay.ebiznext.com/mogopay/";
var serverUrl = "/pay/";
var clientUrl = "/mogopay-client/";
var appUrl    = "/mogopay-admin/";

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