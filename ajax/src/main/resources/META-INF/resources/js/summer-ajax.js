/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *	  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

(function($) {
	
	$(function(event) {
		
		(function(scope, ready) {
			
			var fn = arguments.callee,
				re = /^(get|post)$/i,
				inputs = 'input, select, textarea';
			
			$('*', scope).filter(function(i) {
				
				return $(this).is('[data-ajax]') || 
					($(this).is(inputs) && !$(this).is('[type=hidden]') && $(this).parents('form[data-ajax-validation]').size());
				
			}).each(function() {
				
				// TODO: Both data-ajax-disabled and data-ajax-validation should work as HTML5 boolean properties
				var o = $(this),
					event = o.attr('data-ajax-event'),
					validation = $(this).parents('form[data-ajax-validation]').attr('data-ajax-validation') == 'true',
					ids = o.attr('data-ajax') ? o.attr('data-ajax') : (validation ? o.attr('id') : '');
				
				if (!ids) {
					return;
				}
				
				if (!event) {
					if (o.is(':text, textarea')) {
						event = 'blur';
					} else if (o.is('select, :file')) {
						event = 'change';
					} else if (o.is(':checkbox, :radio, :button, a')) {
						event = 'click';
					} else {
						event = 'ready';
					}
				}
				
				o.bind(event, function(event) {

					var url, 
						o = $(this), 
						data = o.attr('data-ajax-params'),
						method = o.attr('data-ajax-method'),
						disabled = o.attr('data-ajax-disabled'),
						selector = '#' + ids.replace(/:/g, '\\:').split(' ').join(', #'),
						params = '_ajax=' + ids,
						regions = $(selector),
						wrapper = function(o) {
							return $(o).is(inputs) ? ($(o).parent().hasClass('beauty') ? $(o).parent().parent() : $(o).parent()) : $(o);
						};
						
					if (o.is('a')) {
						url = o.attr('href');
						method = method ? method : 'get';
						if (!re.test(method)) {
							data += (data ? '&' : '') + '_method=' + method;
						}
					} else if (o.is('button') || o.is(inputs)) {
						var form = o.parents('form');
						url = form.attr('action');
						data = (data ? data + '&' : '') + form.serialize();					
						method = method ? method : (form.attr('method') ? form.attr('method') : 'post');
						if (!re.test(method)) {
							data = data.replace(/(^|&)_method=[^&]*(&|$)/, '$1_method=' + method + '$2')
						}
					}
	
					if (o.attr('data-ajax-url') !== undefined) {
						url = o.attr('data-ajax-url');
					}
					
					if (disabled != 'true') {
						$.ajax({
							url: url,
							type: re.test(method) ? method : 'post',
							data: params + (data ? '&' + data : ''),
							dataType: 'html',
							beforeSend: function(xhr) {
								if (validation) {
									xhr.setRequestHeader('X-Requested-Operation', 'Validation');
								}
								regions.trigger('beforeSend', [xhr]).each(function() {
									wrapper(this).addClass('loading');
								});
							},
							complete: function(xhr, status) {
								regions.trigger('complete', [xhr, status]).each(function() {
									wrapper(this).removeClass('loading');
								});
							},
							error: function(xhr, status, error) {
								if (xhr.status) {
									this.success(xhr.responseXML, [xhr, status, error]);
								}
								regions.trigger('error');
							},
							success: function(data, status, xhr) {
								var elements = $(data);
								if (elements.size() > 0) {
									regions.each(function(i) {
										var obj = $(this),
											target = wrapper(obj);
										// TODO: Copy events for form element wrappers
										// obj.data('events');
										var source = elements.filter('[data-ajax-response=' + this.id + ']')
											.contents()
											.filter(function() {
												return this.nodeType !== 3;
											});
										if (source.size()) {
											if (validation) {
												var selector = '.error',
													sourceError = $(selector, source),
													targetError = $(selector, target);
												if (sourceError.size() && targetError.size()) {
													targetError.replaceWith(sourceError);
												} else if (sourceError.size()) {
													target.append(sourceError);
												} else if (targetError.size()) {
													targetError.remove();
												}
											} else {
												target.replaceWith(source);
												fn(source, ready);
											}
										}
									});
									regions.trigger('success', [data, status, xhr]);
								} else {
									regions.trigger('error', [data, status, xhr]);								
								}
								regions = $(selector);
							}
						});
						
						if (o.is(':submit, :reset, a, button')) {
							event.preventDefault();
						}
						
					}
				});				
				
				if (ready && event == 'ready') {
					o.trigger(event, ready).bind('success', function() {
						o.trigger(event, ready);
					});
				}
				
			});
			
		})(document, event);
		
	});
	
})(jQuery);