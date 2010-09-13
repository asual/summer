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

package com.asual.summer.core.faces;

import javax.el.ELContext;
import javax.el.ELException;

import org.springframework.web.jsf.el.SpringBeanFacesELResolver;

import com.asual.summer.core.ErrorResolver;
import com.asual.summer.core.util.RequestUtils;
import com.asual.summer.core.util.ResourceUtils;
import com.asual.summer.core.util.StringUtils;

/**
 * 
 * @author Rostislav Hristov
 *
 */
public class FacesELResolver extends SpringBeanFacesELResolver {

	public Object getValue(ELContext elContext, Object base, Object property) throws ELException {
		Object target  = RequestUtils.getAttribute(ErrorResolver.ERRORS_TARGET);
		if (target != null) {
			String targetName = StringUtils.toCamelCase(target.getClass().getSimpleName());
			if (targetName.equals(property)) {
				elContext.setPropertyResolved(true);
				return target;
			}
		}
		Object value = super.getValue(elContext, base, property);
		String messages = "messages";
		String properties = "properties";
		if (value == null) {
			try {
				if (property instanceof String) {
					String prop = (String) property;
					if (base == null && (messages.equals(prop) || properties.equals(prop))) {
						elContext.setPropertyResolved(true);
						return new String(prop);
					}
					if (base instanceof String) {
						String bs = (String) base;
						if (messages.equals(bs)) {
							elContext.setPropertyResolved(true);
							return ResourceUtils.getMessage(prop);
						} else if (properties.equals(bs)) {
							elContext.setPropertyResolved(true);
							return ResourceUtils.getProperty(prop);
						} else if (bs.startsWith("{") && bs.endsWith("}")) {
							elContext.setPropertyResolved(true);
							String key = bs.substring(1, bs.length() - 1) + "." + prop;
							String message = ResourceUtils.getMessage(key);
							if (message.startsWith("{") && message.endsWith("}")) {
								return ResourceUtils.getProperty(key);
							} else {
								return message;
							}
						}
					}
				}
			} catch (Exception e) {
			}
		}
		return value;
	}

}