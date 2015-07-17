/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */

$(function() {
    var forms = document.getElementsByTagName("form");
    for (var j = 0; j < forms.length; j++) {
        var form = forms[j];
        form.onsubmit = function() {
            var e = this,
                inputs = e.elements;
            var required = [], att, val;
            for (var i = 0; i < inputs.length; i++) {
                att = inputs[i].getAttribute('required');
                if (att != null) {
                    val = inputs[i].value;
                    if (val.replace(/^\s+|\s+$/g, '') == '') {
                        if($(inputs[i]).is(':visible')){
                            $(inputs[i]).popover({content:'Please fill out this field',placement:'bottom'});
                            $(inputs[i]).popover('show');
                            $(inputs[i]).focus();
                            $(inputs[i]).blur(function() {
                                $(inputs[i]).popover('destroy')
                            })
                            $(inputs[i]).click(function() {
                                $(inputs[i]).popover('destroy')
                            })
                        }
                        return false;
                    }
                }
            }
        }
    }
});
