/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009-2010 Rostislav Hristov, Asual DZZD. All rights reserved.
 *
 * This product is owned by Asual DZZD. All rights in the product including copyrights, licensing rights, 
 * patents, trademarks, engineering rights, moral rights, and any other intellectual property rights 
 * belong to Asual DZZD. These rights are not transferred as part of this agreement. 
 *
 * No part of the product may be reproduced, published, transmitted electronically, mechanically or 
 * otherwise, transcribed, stored in a retrieval system or translated into any language in any form, by 
 * any means, for any purpose other than the purchaser's personal use, without the express written 
 * permission of Asual DZZD.
 */

(function($) {
	
    $(function() {
	    
        var ajax = function(scope) {
            $('a,button', scope).filter('[data-render]').click(function() {
                var url, data,
                    o = $(this), 
                    render = o.attr('data-render'),
                    method = o.attr('data-method'),
                    regions = $('#' + render.replace(/:/g, '\\:').split(' ').join(', #'));
                if (o.is('a')) {
                    url = o.attr('href');
                } else if (o.is('button')) {
                    var form = o.parents('form');
                    url = form.attr('action');
                    data = form.serialize();
                }
                $.ajax({
                    url: url,
                    type: method ? method : 'post',
                    data: 'javax.faces.partial.ajax=true&javax.faces.partial.render=' + render + (data ? '&' + data : ''),
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
                            ajax($(this).html($(data.getElementsByTagName('update')[i].firstChild.nodeValue).html()));
                        });
                    	regions.trigger('success', [data, status, xhr]);
                    }
                });
                return false;
            });
        };
        ajax(document);
        
        //$('.region').bind('beforeSend', function() {
        //}).bind('success', function() {
        //});	    
		
    });
    
})(jQuery);