(function($) {
	
	$(':header').each(function() {
		if (!$.support.opacity) {
			$(this).css({
				filter: 'progid:DXImageTransform.Microsoft.AlphaImageLoader(src=\"\",sizingMethod=\"crop\")',
				zoom: 1
			});
		}
	});
	
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