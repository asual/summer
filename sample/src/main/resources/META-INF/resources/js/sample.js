(function($) {

    $(window).load(function() {
        var clock = $('<p class="clock">WebSocket Clock: <span>subscribing...</span></p>').appendTo('body');
        setTimeout(function() {
	        $.atmosphere.subscribe(
	        	location.protocol + '//' + location.host + '/websocket/clock', function callback(response) {
	            if (response.status == 200 && response.responseBody && response.state != 'connected' && response.state != 'closed') {
	                $('span', clock).html(response.responseBody);
	            }
	        }, $.atmosphere.request = {
	            logLevel: 'none',
	            transport: 'websocket'
	        });
        }, 200);
    });

})(jQuery);