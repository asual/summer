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
            
        	$('*', scope).filter('[data-ajax]').each(function() {

                var url, event, data,
	                o = $(this), 
	                ids = o.attr('data-ajax'),
	                method = o.attr('data-ajax-method'),
	                regions = $('#' + ids.replace(/:/g, '\\:').split(' ').join(', #'));
            
	            if (o.is('a')) {
	                url = o.attr('href');
	                event = 'click';
	            } else if (o.is('button')) {
	                var form = o.parents('form');
	                url = form.attr('action');
	                event = 'click';
	                data = form.serialize();
	            } else if (o.is('input, select, textarea')) {
	                var form = o.parents('form');
	                url = form.attr('action');
	                event = 'blur';
	                data = form.serialize();	            	
	            }
	            
	            if (o.attr('data-ajax-url')) {
	            	url = o.attr('data-ajax-url');
	            }
	            
	            if (o.attr('data-ajax-event')) {
	            	event = o.attr('data-ajax-event');
	            }
	            
            	o.bind(event, function() {
            		
                    $.ajax({
                        url: url,
                        type: method ? method : 'post',
                        data: 'javax.faces.partial.ajax=true&javax.faces.partial.render=' + ids + (data ? '&' + data : ''),
                        beforeSend: function(xhr) {
                            xhr.setRequestHeader('Faces-Request', 'partial/ajax');
                            regions.trigger('beforeSend', [xhr]);
                        },
                        complete: function(xhr, status) {
                        	regions.trigger('complete', [xhr, status]);
                        },
                        error: function(xhr, status, error) {
                            if (xhr.status) {
                                this.success(xhr.responseXML, [xhr, status, error]);
                            }
                            regions.trigger('error');
                        },
                        success: function(data, status, xhr) {
                        	regions.each(function(i) {
                        		fn($(this).html($(data.getElementsByTagName('update')[i].firstChild.nodeValue).html()));
                            });
                        	regions.trigger('success', [data, status, xhr]);
                        }
                    });
                    
                    return false;
                    
                });            	
            	
            });
        	
        })(document);
        
        //$('.region').bind('beforeSend', function() {
        //}).bind('success', function() {
        //});	    
		
    });
    
})(jQuery);