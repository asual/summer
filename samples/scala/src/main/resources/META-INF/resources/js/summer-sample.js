(function($) {
	
    $(function() {
        $('#options, #view').bind('beforeSend', function(e) {
            if (window.console) {
                window.console.log(e);
            }
        }).bind('complete', function(e) {
            if (window.console) {
                window.console.log(e);
            }
        });
    });

})(jQuery);