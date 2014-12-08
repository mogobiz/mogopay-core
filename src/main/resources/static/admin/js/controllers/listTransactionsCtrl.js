function ListTransactionsCtrl($scope, $location, $rootScope, $route) {
    $scope.goToProfile = function () {
        var success = function (response) {
            $rootScope.userProfile = response;
            $location.path("/profile");
            $scope.$apply();
            $location.replace();
        };
        callServer("account/profile-info", "", success, function (response) {});
    };
    $scope.listTransactionsSearch =  function () {
        var success = function (response) {
            $scope.$apply(function () {
                $rootScope.transactions = response;
            });
        };
        var dataToSend = "";//"xtoken=" + $rootScope.xtoken;
        if ($.trim($("#listTransactionsEmail").val()) != "")
            dataToSend += "&email=" + $("#listTransactionsEmail").val();
        if ($.trim($("#listTransactionsUUID").val()) != "")
            dataToSend += "&transaction_uuid=" + $("#listTransactionsUUID").val();
        if ($.trim($("#listTransactionsAmount").val()) != "")
            dataToSend += "&amount=" + $("#listTransactionsAmount").val();
        if ($("#listTransactionsStartDate").val() != "")
            dataToSend += "&start_date=" + $("#listTransactionsStartDate").val();
        if ($("#listTransactionsStartTime").val() != "")
            dataToSend += "&start_time=" + $("#listTransactionsStartTime").val();
        if ($("#listTransactionsEndDate").val() != "")
            dataToSend += "&end_date=" + $("#listTransactionsEndDate").val();
        if ($("#listTransactionsEndTime").val() != "")
            dataToSend += "&end_time=" + $("#listTransactionsEndTime").val();
        callServer("backoffice/transactions", dataToSend, success, function (response) {});
    };
    $scope.transactionGetLogs =  function (id) {
        var success = function (response) {
            for (var i = 0; i < response.length; i++) {
                response[i].logTable = response[i].log.split("&");
            }
            $rootScope.transactionLogs = response;
            $location.path("/transactionLogs");
            $scope.$apply();
            $location.replace();
        };
        var dataToSend = "";//"xtoken=" + $rootScope.xtoken;
        callServer("backoffice/transactions/" + id + "/logs", dataToSend, success, function (response) {});
    };
    $scope.refreshStatusPopover = function () {refreshStatusPopover();};
    $scope.refreshCardPopover = function () {refreshCardPopover();};
}

function refreshStatusPopover() {
    $("[rel=popoverStatus]").popover({
        html : true,
        placement:"bottom",
        content: function () {
			$(".popover").removeClass("in").remove();
            var parent =  $(this).parent();
            var element = $(".dialog", parent);
            return element.html();
        }
    });
    $( window ).resize(function () {
        $("[rel=popoverStatus]").popover("hide");
    });
}

function refreshCardPopover() {
    $("[rel=popoverCard]").popover({
        html : true,
        placement:"bottom",
        content: function () {
			$(".popover").removeClass("in").remove();
            var parent =  $(this).parent();
            var element = $(".dialog", parent);
            return element.html();
        }
    });
    $( window ).resize(function () {
        $("[rel=popoverCard]").popover("hide");
    });
}