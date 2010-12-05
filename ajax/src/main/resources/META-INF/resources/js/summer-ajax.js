/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

(function($) {
	
    $(function() {
	    
        (function(scope) {
        	
        	var fn = arguments.callee;
        	
        	$('*', scope).filter(function(index) {
        		return $(this).is('[data-ajax]') || $(this).is('[data-ajax-validation]');
        	}).each(function() {

        		var o = $(this),
        			event = o.attr('data-ajax-event'),
        			validation = o.attr('data-ajax-validation') == 'true';
        		
	            if (!event) {
	            	if (o.is(':text, textarea')) {
	            		event = 'blur';
	            	} else if (o.is('select')) {
	            		event = 'change';
	            	} else if (o.is(':checkbox, :radio, :submit, :reset, a, button')) {
	            		event = 'click';
	            	}
	            }
        		
        		o.bind(event, function(event) {

                    var url, 
		                o = $(this), 
		                re = /^(get|post)$/i,
		                ids = o.attr('data-ajax') ? o.attr('data-ajax') : (validation ? o.attr('id') : ''),
		                data = o.attr('data-ajax-params'),
		                method = o.attr('data-ajax-method'),
		                tags = 'input, select, textarea',
		                selector = '#' + ids.replace(/:/g, '\\:').split(' ').join(', #'),
		                params = '_ajax=' + ids,
		                regions = $(selector),
		                find = function(arr, id) {
                    		for (var i = 0; i < arr.length; i++) {
                    			if (id == arr[i].getAttribute('id')) {
                    				return arr[i];
                    			}
                    		}
	                    };
	                    
		            if (o.is('a')) {
		                url = o.attr('href');
		                method = method ? method : 'get';
		                if (!re.test(method)) {
		                	data += (data ? '&' : '') + '_method=' + method;
		                }
	                } else if (o.is('button') || o.is(tags)) {
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
		            
                    $.ajax({
                        url: url,
                        type: re.test(method) ? method : 'post',
                        data: params + (data ? '&' + data : ''),
                        beforeSend: function(xhr) {
                        	if (validation) {
                                xhr.setRequestHeader('X-Requested-Operation', 'Validation');
                        	}
                            regions.trigger('beforeSend', [xhr]).each(function() {
                            	($(this).is(tags) ? $(this).parent() : $(this)).addClass('loading');
                            });
                        },
                        complete: function(xhr, status) {
                        	regions.trigger('complete', [xhr, status]).each(function() {
                        		($(this).is(tags) ? $(this).parent() : $(this)).removeClass('loading');
                            });
                        },
                        error: function(xhr, status, error) {
                            if (xhr.status) {
                                this.success(xhr.responseXML, [xhr, status, error]);
                            }
                            regions.trigger('error');
                        },
                        success: function(data, status, xhr) {
                        	if (data && data.getElementsByTagName('update').length > 0) {
                        		regions.each(function(i) {
	                        		var obj = $(this),
	                        			el = find(data.getElementsByTagName('update'), this.id);
	                        		// TODO: Copy events for form element wrappers
	                        		// obj.data('events');
	                        		if (el) {
	                        			var target = obj.is(tags) ? obj.parent() : obj;
		                        		fn(target.html($(el.firstChild.nodeValue).html()));
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
                    
                });            	
            	
            });
        	
        })(document);
        
    });
    
})(jQuery);