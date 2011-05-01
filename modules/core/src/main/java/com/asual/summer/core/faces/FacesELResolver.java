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
	
	public static final String MESSAGE_ATTRIBUTE = FacesELResolver.class.getName() + ".MESSAGE_ATTRIBUTE";
	public static final String PROPERTY_ATTRIBUTE = FacesELResolver.class.getName() + ".PROPERTY_ATTRIBUTE";
	
	public Object getValue(ELContext elContext, Object base, Object property) throws ELException {
		
		Object value = super.getValue(elContext, base, property);
		
		if (value == null) {
			try {
				if (property instanceof String) {
					final String current = (String) property;
					if (base == null && (MESSAGES.equals(current) || PROPERTIES.equals(current))) {
						elContext.setPropertyResolved(true);
						return current;
					}
					if (base instanceof String) {
						if (MESSAGES.equals(base)) {
							elContext.setPropertyResolved(true);
							RequestUtils.setAttribute(MESSAGE_ATTRIBUTE, current);
							String result = ResourceUtils.getMessage(current);
							return result != null ? result : "";
						} else if (PROPERTIES.equals(base)) {
							elContext.setPropertyResolved(true);
							RequestUtils.setAttribute(PROPERTY_ATTRIBUTE, current);
							Object result = ResourceUtils.getProperty(current);
							return result != null ? result : "";
						} else if (RequestUtils.getAttribute(MESSAGE_ATTRIBUTE) != null) {
							elContext.setPropertyResolved(true);
							RequestUtils.setAttribute(MESSAGE_ATTRIBUTE, RequestUtils.getAttribute(MESSAGE_ATTRIBUTE) + "." + current);
							Object result = ResourceUtils.getMessage((String) RequestUtils.getAttribute(MESSAGE_ATTRIBUTE));
							return result != null ? result : "";
						} else if (RequestUtils.getAttribute(PROPERTY_ATTRIBUTE) != null) {
							elContext.setPropertyResolved(true);
							RequestUtils.setAttribute(PROPERTY_ATTRIBUTE, RequestUtils.getAttribute(PROPERTY_ATTRIBUTE) + "." + current);
							Object result = ResourceUtils.getProperty((String) RequestUtils.getAttribute(PROPERTY_ATTRIBUTE));
							return result != null ? result : "";
						}
					}
				}
			} catch (Exception e) {}
		}
		
		RequestUtils.setAttribute(MESSAGE_ATTRIBUTE, null);
		RequestUtils.setAttribute(PROPERTY_ATTRIBUTE, null);

		return value;
	}

}