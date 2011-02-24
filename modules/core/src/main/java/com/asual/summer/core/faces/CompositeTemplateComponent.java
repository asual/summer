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

import java.util.HashMap;
import java.util.Map;

import javax.el.ELContext;
import javax.el.ValueExpression;
import javax.faces.context.FacesContext;


/**
 * 
 * @author Rostislav Hristov
 *
 */
public class CompositeTemplateComponent extends CompositeComponent {

	public static final String COMPONENT_TYPE = "summer.faces.CompositeTemplateComponent";
	public static final String COMPONENT_FAMILY = "summer.faces.NamingContainer";

	public Map<String, Object> getTemplateParams() {
		String match = "^dataTemplate.+$";
		Map<String, Object> params = new HashMap<String, Object>();
		Map<String, Object> attrs = getAttributes();
		for (String key : attrs.keySet()) {
			if (key.matches(match)) {
				params.put(getParamName(key), attrs.get(key));
			}
		}
		ELContext elContext = FacesContext.getCurrentInstance().getELContext();
		Map<String, ValueExpression> bindings = getBindings();
		for (String key : bindings.keySet()) {
			if (key.matches(match)) {
				params.put(getParamName(key), bindings.get(key).getValue(elContext));
			}
		}
		return params;
	}
	
	private String getParamName(String name) {
		name = name.replaceFirst("dataTemplate", "");
		return name.substring(0, 1).toLowerCase() + name.substring(1);
	}

}