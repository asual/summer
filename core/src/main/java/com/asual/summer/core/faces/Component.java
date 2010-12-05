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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.el.ValueExpression;
import javax.faces.component.UIComponent;
import javax.faces.component.UINamingContainer;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.render.Renderer;

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
    
    public Renderer getRenderer() {
    	return getRenderer(FacesContext.getCurrentInstance());
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
    
    public String getValueId() {
    	try {
    		ValueExpression value = getBindings().get("value");
    		ValueExpression dataValue = getBindings().get("dataValue");
        	try {
    	    	FacesContext context = FacesContext.getCurrentInstance();
    	    	return getExprId(getRepeatWrapper().getBindings().get("dataValue").getExpressionString()) + 
    	    		UINamingContainer.getSeparatorChar(context) + value.getValue(context.getELContext());
        	} catch (Exception e) {
        		if (dataValue != null) {
            		return getExprId(dataValue.getExpressionString());
        		}
        		if (value == null) {
            		return getExprId(getChildrenText());
        		}
        		return getExprId(value.getExpressionString());
        	}
    	} catch (Exception e) {}    	
    	return null;
    }   
    
    public String getExprId(String expr) {
    	String[] arr = expr.replaceAll("^(\\$|#)\\{|\\}$", "").split("\\.");
		if (arr.length != 0) {
			return arr[arr.length - 1];
		}
		return null;
    }
    
    public String getClientId(FacesContext context) {
    	ValueExpression ve = getBindings().get("idx");
    	if (ve != null) {
    		return (String) ve.getValue(FacesContext.getCurrentInstance().getELContext());
    	}
    	String id = getId();
    	if (!id.startsWith(UIViewRoot.UNIQUE_ID_PREFIX)) {
    		return id;
    	}
    	String valueId = getValueId();
    	if (valueId != null) {
    		return valueId;
    	}
        return id;
    }

    public void setValueExpression(String name, ValueExpression binding) {
    	super.setValueExpression(name, binding);
    	bindings.put(name, binding);
		if (binding instanceof TagValueExpression) {
    		String value = ((TagValueExpression) binding).getExpressionString().replaceAll("^(\\$|#)\\{|\\}$", "");
    		bindingValues.put(name, value);
		}
    }
    
    public Component getRepeatWrapper() {
    	try {
			return (Component) getParent().getParent().getParent().getParent();
    	} catch (Exception e) {
    		return null;
    	}
    } 
    
    private String getChildrenText() {
    	List<String> strings = new ArrayList<String>();
    	Iterator<UIComponent> fac = getFacetsAndChildren();
    	while (fac.hasNext()) {
    		UIComponent value = fac.next().findComponent("value");
        	for (UIComponent child : value.getChildren()) {
        		strings.add(child.toString());
        	}
    	}
    	return StringUtils.join(strings, "");
    }
}