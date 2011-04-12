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

package com.asual.summer.core.faces;

import javax.el.ELContext;
import javax.el.ELException;

import org.springframework.web.jsf.el.SpringBeanFacesELResolver;

import com.asual.summer.core.util.RequestUtils;
import com.asual.summer.core.util.ResourceUtils;

/**
 * 
 * @author Rostislav Hristov
 *
 */
public class FacesELResolver extends SpringBeanFacesELResolver {

	private static String MESSAGES = "messages";
	private static String PROPERTIES = "properties";
	
	public static final String KEY_ATTRIBUTE = FacesELResolver.class.getName() + ".KEY_ATTRIBUTE";
	
	public Object getValue(ELContext elContext, Object base, Object property) throws ELException {
		
		Object value = super.getValue(elContext, base, property);
		
		if (value == null) {
			try {
				if (property instanceof String) {
					final String prop = (String) property;
					if (base == null && (MESSAGES.equals(prop) || PROPERTIES.equals(prop))) {
						elContext.setPropertyResolved(true);
						RequestUtils.setAttribute(KEY_ATTRIBUTE, null);
						return new String(prop);
					}
					if (base instanceof String) {
						String bs = (String) base;
						if (MESSAGES.equals(bs)) {
							elContext.setPropertyResolved(true);
							String messageResult = ResourceUtils.getMessage(prop);
							return messageResult != null ? messageResult : "{" + prop + "}";
						} else if (PROPERTIES.equals(bs)) {
							elContext.setPropertyResolved(true);
							Object propertyResult = ResourceUtils.getProperty(prop);
							return propertyResult != null ? propertyResult : "{" + prop + "}";
						} else if (bs.startsWith("{") && bs.endsWith("}") || RequestUtils.getAttribute(KEY_ATTRIBUTE) != null) {
							elContext.setPropertyResolved(true);
							if (RequestUtils.getAttribute(KEY_ATTRIBUTE) != null) {
								bs = "{" + RequestUtils.getAttribute(KEY_ATTRIBUTE) + "}";
							}
							RequestUtils.setAttribute(KEY_ATTRIBUTE, bs.substring(1, bs.length() - 1) + "." + prop);
							String messageResult = ResourceUtils.getMessage((String) RequestUtils.getAttribute(KEY_ATTRIBUTE));
							String message = messageResult != null ? messageResult : "{" + prop + "}";
							if (message.startsWith("{") && message.endsWith("}")) {
								Object propertyResult = ResourceUtils.getProperty((String) RequestUtils.getAttribute(KEY_ATTRIBUTE));
								return propertyResult != null ? propertyResult : "{" + prop + "}";
							} else {
								return message;
							}
						}
					}
				}
			} catch (Exception e) {}
		}
		
		RequestUtils.setAttribute(KEY_ATTRIBUTE, null);
		return value;
	}

}