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

import java.beans.BeanInfo;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.el.ValueExpression;
import javax.faces.component.UIComponent;
import javax.faces.component.UINamingContainer;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;

import com.asual.summer.core.ErrorResolver;
import com.asual.summer.core.util.ArrayUtils;
import com.asual.summer.core.util.RequestUtils;
import com.asual.summer.core.util.StringUtils;
import com.sun.faces.facelets.el.TagValueExpression;

/**
 * 
 * @author Rostislav Hristov
 *
 */
public class Component extends UINamingContainer {

    public static final String COMPONENT_TYPE = "summer.faces.Component";
    public static final String COMPONENT_FAMILY = "summer.faces.NamingContainer";

    private String styleClass;
    private boolean escape;
    private Map<String, ValueExpression> bindings = new HashMap<String, ValueExpression>();
    private Map<String, String> bindingValues = new HashMap<String, String>();
    
    public String getFamily() {
        return (COMPONENT_FAMILY);
    }
    
    public String getStyleClass() {
        return styleClass;
    }

    public void setStyleClass(String styleClass) {
        this.styleClass = styleClass;
    }

    public boolean isEscape() {
        return escape;
    }

    public void setEscape(boolean escape) {
        this.escape = escape;
    }
    
    private String getChildrenText() {
    	List<String> strings = new ArrayList<String>();
    	for (UIComponent child : getChildren()) {
    		strings.add(child.toString());
    	}
    	return StringUtils.join(strings, "");
    }
    
    public Map<String, ValueExpression> getBindings() {
    	return bindings;
    }
    
    public Object getConfig(String name) {
    	BeanInfo info = (BeanInfo) getAttributes().get(UIComponent.BEANINFO_KEY);
    	TagValueExpression ve = (TagValueExpression) info.getBeanDescriptor().getValue(name);
    	if (ve != null) {
    		return ve.getValue(FacesContext.getCurrentInstance().getELContext());
    	}
    	return null;
    }
    
    public String getFormId() {
    	String id = getClientId();
    	if (id == null || id.startsWith(UIViewRoot.UNIQUE_ID_PREFIX)) {
	    	try {
        		ValueExpression value = bindings.get("value");
        		ValueExpression dataValue = bindings.get("dataValue");
            	try {
        	    	FacesContext context = FacesContext.getCurrentInstance();
        	    	return getExprId(getRepeatComponent().getBindings().get("dataValue").getExpressionString()) + 
        	    		UINamingContainer.getSeparatorChar(context) + value.getValue(context.getELContext());
            	} catch(Exception e) {
            		if (dataValue != null) {
                		return getExprId(dataValue.getExpressionString());
            		}
            		if (value == null) {
                		return getExprId(getChildrenText());
            		}
            		return getExprId(value.getExpressionString());
            	}
	    	} catch(Exception e) {
	        	return id;
	    	}
    	}
    	return id;
    }
    
    public String getFormName() {
    	try {
	    	return getExprId(getRepeatComponent().getBindings().get("dataValue").getExpressionString());
    	} catch(Exception e) {
        	return getFormId();
    	}
    }
    
    @SuppressWarnings("unchecked")
	public boolean isMatch() {
    	try {
			Component component = getRepeatComponent();
			Map<String, ValueExpression> bindings = component.getBindings();
			Object dataValue = bindings.get("dataValue").getValue(FacesContext.getCurrentInstance().getELContext());
			Map<String, Map<String, Object>> errors = (Map<String, Map<String, Object>>) RequestUtils.getAttribute(ErrorResolver.ERRORS);
			if (errors != null && errors.get(getFormName()) != null) {
				dataValue = errors.get(getFormName()).get("value");
			}
			Object current = component.getAttributes().get("current");
			if (dataValue instanceof List<?>) {
				return ((List<?>) dataValue).contains(current);
			} else if (dataValue instanceof Boolean) {
				return dataValue.equals(Boolean.valueOf(current.toString()));
			} else {
				return dataValue.equals(current);
			}
    	} catch (Exception e) {
    		return false;
    	}
    }
    
    public void setValueExpression(String name, ValueExpression binding) {
    	super.setValueExpression(name, binding);
    	bindings.put(name, binding);
		if (binding instanceof TagValueExpression) {
    		String value = ((TagValueExpression) binding).getExpressionString().replaceAll("^(\\$|#)\\{|\\}$", "");
    		bindingValues.put(name, value);
		}
    }
    
    private Component getRepeatComponent() {
    	try {
			return (Component) getParent().getParent().getParent().getParent();
    	} catch (Exception e) {
    		return null;
    	}
    }
    
    private String getExprId(String expr) {
    	return ArrayUtils.last(expr.replaceAll("^(\\$|#)\\{|\\}$", "").split("\\."));
    }
    
}
