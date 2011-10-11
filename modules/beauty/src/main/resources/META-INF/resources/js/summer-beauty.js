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
 * 
 * The following script extend the functionality provided by Uniform v1.5
 * Copyright 2009 Josh Pyles / Pixelmatrix Design LLC
 * 
 */
(function($) {

	$.fn.beauty = function(options) {

		var el = this;

		options = $.extend({
			fileClass: 'file',
			filenameClass: 'filename',
			fileBtnClass: 'action',
			fileBtnText: 'Browse',
			fileDefaultText: 'No file chosen',
			checkedClass: 'beauty-checked',
			focusClass: 'beauty-focus',
			disabledClass: 'beauty-disabled',
			activeClass: 'beauty-active',
			hoverClass: 'beauty-hover'
		}, options);

		function cleanWhitespace(elem) {
			textNodes = elem.contents()
				.filter(function() { 
					return (this.nodeType == 3 && !/\S/.test(this.nodeValue)); 
				})
				.remove();
		}
		
		function label(elem, ref) {
			(ref || $('label[for=\'' + elem.attr('id') + '\']'))
				.mousedown(function(e) {
					elem.trigger(e.type);
				})
				.mouseup(function(e) {
					elem.trigger(e.type);
				})
				.mouseover(function(e) {
					elem.trigger(e.type);
				})
				.mouseout(function(e) {
					elem.trigger(e.type);
				});
		}
		
		function disable(elem, tag) {
			if ($(elem).attr('disabled')) {
				tag.addClass(options.disabledClass);
			}
		}
		
		function check(elem, tag) {
			if ($(elem).attr('checked')) {
				tag.addClass(options.checkedClass);
			}
		}

		function noSelect(elem) {
			function f() {
				return false;
			}
			$(elem).each(function() {
				this.onselectstart = this.ondragstart = f; // Webkit & IE
				$(this)
					.mousedown(f) // Webkit & Opera
					.css({ MozUserSelect: 'none' }); // Firefox
			});
		}

		function init(elem, text, visible) {
			elem
				.wrap(
					$('<div class="beauty" />')
						.css({
							display: 'inline',
							position: 'relative'
						})
				)
				.before(
					$('<span class="el" />')
						.html(text ? text : '')
				)
				.before(
					$('<img width="0" />')
				)
				.css({
					position: 'absolute',
					top: 0,
					left: 0,
					width: elem.is(':button') ? elem.outerWidth(false) : elem.width() + 'px',
					opacity: visible ? 1 : 0
				});
			
			var className = elem.is('button, select, textarea') ? elem.get(0).tagName.toLowerCase() : elem.attr('type');
			if (!elem.parent().parent().is('.' + className)) {
				elem.parent().wrap('<div class="' + className + '" />');
			}
		}
		
		function resize(elem, tag, center) {
			var width = Math.round(elem.outerWidth(true)),
				tagWidth = Math.round(tag.width());
				left = parseInt(tag.css('left'), 10);
				
			tag.width(tagWidth).css('left', left);
			
			if (center) {
				var half = Math.ceil((width - tagWidth)/2);
				tag
					.css({
						paddingRight: half,
						paddingLeft: half - left
					});
			} else {
				tag
					.css('paddingRight', width - left - tagWidth);
			}
			tag
				.parent()
				.css('marginRight', width - left);
		}	  
		
		return this.each(function() {
			
			if (!($.browser.msie && $.browser.version < 8)) {
	
				var elem = $(this);
				
				if (elem.parent().hasClass('beauty')) {
					return;
				}
				
				if (elem.is(':text')) {

					init(elem, '', true);
					
					var divTag = elem.parent('div'),
						spanTag = elem.siblings('span');
					
					elem
						.focus(function() {
							divTag.addClass(options.focusClass);
						})
						.blur(function() {
							divTag.removeClass(options.focusClass);
						})
						.mousedown(function() {
							divTag.addClass(options.activeClass);
						})
						.mouseup(function() {
							divTag.removeClass(options.activeClass);
						})
						.hover(function() {
								divTag.addClass(options.hoverClass);
							}, function() {
								divTag.removeClass(options.hoverClass);
						});
					
					resize(elem, spanTag);
					disable(elem, divTag);
					
				} else if (elem.is(':checkbox')) {

					init(elem);
		
					var divTag = elem.parent('div'),
						spanTag = elem.siblings('span');
					
					cleanWhitespace(divTag.parent());
					
					//hide normal input and add focus classes
					$(elem)
						.focus(function() {
							divTag.addClass(options.focusClass);
						})
						.blur(function() {
							divTag.removeClass(options.focusClass);
						})
						.click(function() {
							if (!$(elem).attr('checked')) {
								//box was just unchecked, uncheck span
								spanTag.removeClass(options.checkedClass);
							} else {
								//box was just checked, check span.
								spanTag.addClass(options.checkedClass);
							}
						})
						.keydown(function(e) {
							if (e.currentTarget == elem.get(0) && e.keyCode == 32) {
								divTag.addClass(options.activeClass);
							}
						})
						.keyup(function(e) {
							if (e.currentTarget == elem.get(0) && e.keyCode == 32) {
								divTag.removeClass(options.activeClass);
							}
						})
						.mousedown(function() {
							divTag.addClass(options.activeClass);
						})
						.mouseup(function() {
							divTag.removeClass(options.activeClass);
						})
						.mouseout(function() {
							divTag.removeClass(options.activeClass);
						})
						.hover(function() {
								divTag.addClass(options.hoverClass);
							}, function() {
								divTag.removeClass(options.hoverClass);
						});
					
					disable(elem, divTag);
					check(elem, spanTag);
					label(elem);

				} else if (elem.is(':radio')) {

					init(elem);
		
					var divTag = elem.parent('div'),
						spanTag = elem.siblings('span');			

					cleanWhitespace(divTag.parent());
					
					//hide normal input and add focus classes
					$(elem)
						.focus(function() {
							divTag.addClass(options.focusClass);
						})
						.blur(function() {
							divTag.removeClass(options.focusClass);
						})
						.click(function() {
							if (!$(elem).attr('checked')) {
								//box was just unchecked, uncheck span
								spanTag.removeClass(options.checkedClass);
							} else {
								//box was just checked, check span
								$('span.'+options.checkedClass + ' ~ input[name=\'' 
									+ $(elem).attr('name') + '\']').siblings('span').removeClass(options.checkedClass);
								spanTag.addClass(options.checkedClass);
							}
						})
						.mousedown(function() {
							if (!$(elem).is(':disabled')) {
								divTag.addClass(options.activeClass);
							}
						})
						.mouseup(function() {
							divTag.removeClass(options.activeClass);
						})
						.mouseout(function() {
							divTag.removeClass(options.activeClass);
						})
						.hover(function() {
								divTag.addClass(options.hoverClass);
							}, function() {
								divTag.removeClass(options.hoverClass);
						});
					
					disable(elem, divTag);
					check(elem, spanTag);
					label(elem);

				} else if (elem.is(':file')) {
					
					init(elem, options.fileBtnText, false);
					
					var divTag = elem.parent('div'),
						spanTag = elem.siblings('span');
					
					elem.width(divTag.outerWidth(true) + spanTag.outerWidth(true));

					var filenameTag = $('<span class="value" />')
						.insertBefore(elem)
						.html(options.fileDefaultText)
						.css({
							left: elem.outerWidth(true)
						});
					
					cleanWhitespace(divTag.parent());
					
					elem
						.change(function() {
							var elem = $(this),
								filename = elem.val();
							filename = filename.split(/[\/\\]+/);
							filename = filename[(filename.length-1)];
							filenameTag.text(filename);
							divTag.removeClass(options.activeClass + ' ' + options.focusClass + ' ' + options.hoverClass);
							elem.width(divTag.outerWidth(true) + spanTag.outerWidth(true) + filenameTag.outerWidth(true));
						})
						.focus(function() {
							divTag.addClass(options.focusClass);
						})
						.blur(function() {
							divTag.removeClass(options.focusClass);
						})
						.keydown(function(e) {
							if (e.currentTarget == elem.get(0) && e.keyCode == 32) {
								divTag.addClass(options.activeClass);
							}
						})
						.keyup(function(e) {
							if (e.currentTarget == elem.get(0) && e.keyCode == 32) {
								divTag.removeClass(options.activeClass);
							}
						})
						.mousedown(function() {
							divTag.addClass(options.activeClass);
						})
						.mouseup(function() {
							divTag.removeClass(options.activeClass);
						})
						.mouseout(function() {
							divTag.removeClass(options.activeClass);
						})
						.hover(function() {
								divTag.addClass(options.hoverClass);
							}, function() {
								divTag.removeClass(options.hoverClass);
						});					
					
					resize(elem, spanTag, true);
					disable(elem, divTag);
					label(elem, filenameTag);
					elem.width(elem.outerWidth(true) + filenameTag.outerWidth(true));
					
				} else if (elem.is('textarea')) {
					
					init(elem, '', true);
					elem
						.before(
							$('<span class="el-bl" /><span class="el-br" />')
						);
					
					var divTag = elem.parent('div'),
						spanTag = elem.siblings('span:first'),
						spanBottomTags = elem.siblings('.el-bl, .el-br');
					
					elem
						.focus(function() {
							divTag.addClass(options.focusClass);
						})
						.blur(function() {
							divTag.removeClass(options.focusClass);
						})
						.mousedown(function() {
							divTag.addClass(options.activeClass);
						})
						.mouseup(function() {
							divTag.removeClass(options.activeClass);
						})
						.hover(function() {
								divTag.addClass(options.hoverClass);
							}, function() {
								divTag.removeClass(options.hoverClass);
						});
					
					resize(elem, spanTag);
					
					divTag
						.css({
							lineHeight: elem.outerHeight(true) + 'px',
							paddingTop: elem.outerHeight(true)/2 - divTag.height()/2,
							paddingBottom: elem.outerHeight(true)/2 - spanBottomTags.outerHeight() - divTag.height()/2 + 1
						});

					spanTag
						.css({
							paddingBottom: elem.outerHeight(true) - spanBottomTags.outerHeight()
						});
					
					resize(elem, $('.el-br'));

					spanBottomTags
						.css({
							top: spanTag.outerHeight()
						});				
					
					disable(elem, divTag);

				} else if (elem.is('select')) {
				
					//element is a select
					if (elem.attr('multiple') != true && 
						(elem.attr("size") == undefined || elem.attr("size") <= 1)) {
						
						var selected = elem.find(':selected:first');
						if (selected.length == 0) {
							selected = elem.find('option:first');
						}
						
						if (elem.get(0).style.width == '' && !$.browser.safari) {
							elem.css('width', (Math.ceil(elem.outerWidth(false)) + 1) + 'px');
						}
						
						init(elem, selected.text());
						
						//redefine variables
						var divTag = elem.parent('div'),
							spanTag = elem.siblings('span');

						elem
							.change(function() {
								divTag.removeClass(options.activeClass);
								spanTag.text(elem.find(':selected').text());
								resize(elem, spanTag);
							})
							.focus(function() {
								divTag.addClass(options.focusClass);
							})
							.blur(function() {
								divTag.removeClass(options.focusClass);
								divTag.removeClass(options.activeClass);
							})
							.mousedown(function() {
								divTag.addClass(options.activeClass);
							})
							.mouseup(function() {
								divTag.removeClass(options.activeClass);
							})
							.click(function(){
								divTag.removeClass(options.activeClass);
							})
							.mouseover(function() {
								divTag.addClass(options.hoverClass);
							})
							.mouseout(function() {
								divTag.removeClass(options.hoverClass);
							})
							.keyup(function(){
								spanTag.text(elem.find(':selected').text());
								resize(elem, spanTag);
							});
						
						resize(elem, spanTag);
						disable(elem, divTag);
						noSelect(spanTag);
					}
					
				} else if (elem.is('button')) {
				
					init(elem, elem.text());
					
					var divTag = elem.parent('div'),
						spanTag = elem.siblings('span');
					
					elem
						.focus(function() {
							divTag.addClass(options.focusClass);
						})
						.blur(function() {
							divTag.removeClass(options.focusClass);
						})
						.keydown(function(e) {
							if (e.currentTarget == elem.get(0) && e.keyCode == 32) {
								divTag.addClass(options.activeClass);
							}
						})
						.keyup(function(e) {
							if (e.currentTarget == elem.get(0) && e.keyCode == 32) {
								divTag.removeClass(options.activeClass);
							}
						})
						.mousedown(function() {
							divTag.addClass(options.activeClass);
						})
						.mouseup(function() {
							divTag.removeClass(options.activeClass);
						})
						.mouseout(function() {
							divTag.removeClass(options.activeClass);
						})
						.hover(function() {
								divTag.addClass(options.hoverClass);
							}, function() {
								divTag.removeClass(options.hoverClass);
						});
					
					resize(elem, spanTag, true);
					disable(elem, divTag);
				}
				
				var obj = $('span', elem),
					shadow = obj.css('text-shadow');
				if (!$.support.opacity && shadow && shadow != 'none') {
					shadow = shadow.split(' ');
					if (shadow.length > 1) {
						var color = shadow[0].substr(1).split(''),
							offx = parseInt(shadow[1]),
							offy = parseInt(shadow[2]);
						if (color.length == 3) {
							color[5] = color[2];
							color[4] = color[2];
							color[3] = color[1];
							color[2] = color[1];
							color[1] = color[0];
						}
						color = color.join('');
						$('span', obj.wrapInner('<span />')).css({
							display: 'inline-block',
							filter: offx == 0 && offy == 1 ? 
									'progid:DXImageTransform.Microsoft.Shadow(color=#' + color + ',direction=180,strength=1)' : 
									'progid:DXImageTransform.Microsoft.DropShadow(color=#' + color + ',offX=' + offx + ',offY=' + offy + ')'
						});
					}
				}
				
				elem.unbind('success').bind('success', function(e) {
					$(this).beauty();
				});
			}
			
		});
	};

	$(function() {
		$('input, textarea, select, button').beauty();
	});	
	
})(jQuery);