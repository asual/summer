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
import java.util.HashMap;
import java.util.Map;

import javax.el.ELContext;
import javax.el.ValueExpression;
import javax.faces.component.UIComponent;
import javax.faces.component.UINamingContainer;
import javax.faces.context.FacesContext;
import javax.faces.render.Renderer;

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
    
    public String getClientId(FacesContext context) {
    	ValueExpression ve = getBindings().get("idx");
    	ELContext elContext = FacesContext.getCurrentInstance().getELContext();
        return (String) ((ve != null) ? ve.getValue(elContext) : getId());
    }

    public void setValueExpression(String name, ValueExpression binding) {
    	super.setValueExpression(name, binding);
    	bindings.put(name, binding);
		if (binding instanceof TagValueExpression) {
    		String value = ((TagValueExpression) binding).getExpressionString().replaceAll("^(\\$|#)\\{|\\}$", "");
    		bindingValues.put(name, value);
		}
    }

}