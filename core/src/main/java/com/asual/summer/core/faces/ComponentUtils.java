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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.el.ValueExpression;
import javax.faces.application.Resource;
import javax.faces.component.UIComponent;
import javax.faces.component.UINamingContainer;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import com.asual.summer.core.util.RequestUtils;
import com.asual.summer.core.util.StringUtils;
import com.sun.faces.facelets.el.TagValueExpression;
import com.sun.faces.facelets.tag.jsf.ComponentSupport;

/**
 * 
 * @author Rostislav Hristov
 *
 */
public class ComponentUtils {
    
    final static String ATTRIBUTES = "^-?\\d.*$|idx|varAttr|com.sun.faces.facelets.APPLIED";
	
    static Object getConfig(Component component, String name) {
    	BeanInfo info = (BeanInfo) component.getAttributes().get(UIComponent.BEANINFO_KEY);
    	if (info != null) {
	    	TagValueExpression ve = (TagValueExpression) info.getBeanDescriptor().getValue(name);
	    	if (ve != null) {
	    		return ve.getValue(FacesContext.getCurrentInstance().getELContext());
	    	}
    	}
    	return null;
    }
    
    static String getChildrenText(Component component) {
    	List<String> strings = new ArrayList<String>();
    	Iterator<UIComponent> fac = component.getFacetsAndChildren();
    	while (fac.hasNext()) {
    		UIComponent value = fac.next().findComponent("value");
        	for (UIComponent child : value.getChildren()) {
        		strings.add(child.toString());
        	}
    	}
    	return StringUtils.join(strings, "");
    }

    static String getValueId(Component component) {
    	try {
    		ValueExpression value = component.getBindings().get("value");
    		ValueExpression dataValue = component.getBindings().get("dataValue");
        	try {
    	    	FacesContext context = FacesContext.getCurrentInstance();
    	    	RepeatComponent repeatComponent = getRepeatComponent(component);
    	    	String componentTag = (String) repeatComponent.getAttributes().get(FacesDecorator.QNAME);
    	    	if (componentTag != null) {
	    	    	return getExprId(repeatComponent.getBindings().get("dataValue").getExpressionString()) + 
	    	    		UINamingContainer.getSeparatorChar(context) + value.getValue(context.getELContext());
    	    	}
        	} catch (Exception e) {}
    		if (dataValue != null) {
        		return getExprId(dataValue.getExpressionString());
    		}
    		if (value == null) {
        		return getExprId(getChildrenText(component));
    		}
    		return getExprId(value.getExpressionString());
    	} catch (Exception e) {}    	
    	return null;
    }
    
    static RepeatComponent getRepeatComponent(Component component) {
    	UIComponent parent = component.getParent();
    	if (parent instanceof RepeatComponent) {
    		return (RepeatComponent) parent;
    	} else {
    		return getRepeatComponent((Component) parent);
    	}
    }
    
    static String getClientId(Component component) {
    	ValueExpression ve = component.getBindings().get("idx");
    	if (ve != null) {
    		return (String) ve.getValue(FacesContext.getCurrentInstance().getELContext());
    	}
    	String id = component.getId();
    	if (!id.startsWith(UIViewRoot.UNIQUE_ID_PREFIX)) {
    		return id;
    	}
    	String valueId = getValueId(component);
    	if (valueId != null) {
    		return valueId;
    	}
        return id;
    }
    
    static String getComponentClass(Component component) {
        return (String) getConfig(component, "componentClass");
    }
    
    static List<String> getComponentClasses(Component component) {
        List<String> classes = new ArrayList<String>();
        String componentClass = getComponentClass(component);
        if (!StringUtils.isEmpty(componentClass)) {
        	classes.add(componentClass);
        }
        String styleClass = (String) component.getAttributes().get("styleClass");
        if (!StringUtils.isEmpty(styleClass)) {
        	classes.add(styleClass);
        }
        return classes;
    }

    static boolean shouldWriteAttribute(Component component, String key) {
        return !Pattern.compile(ATTRIBUTES + "|" + 
			FacesDecorator.ATTRIBUTES + "|" + 
			FacesDecorator.QNAME + "|" + 
			ComponentSupport.MARK_CREATED + "|" + 
			Resource.COMPONENT_RESOURCE_KEY + "|" + 
			UIComponent.BEANINFO_KEY + "|" + 
			UIComponent.FACETS_KEY + "|" + 
			UIComponent.VIEW_LOCATION_KEY, Pattern.CASE_INSENSITIVE).matcher(key).matches();
    }
    
    static void writeAttributes(Component component, ResponseWriter writer) throws IOException {

        if (ComponentUtils.getComponentClass(component) == null) {
        	
        	Map<String, ValueExpression> bindings = component.getBindings();
	        for (String key : bindings.keySet()) {
	    		if (shouldWriteAttribute(component, key)) {
		        	writeAttribute(writer, key, bindings.get(key).getValue(FacesContext.getCurrentInstance().getELContext()));
	        	}
	        }
	        
	        Map<String, Object> attrs = component.getAttributes();
	        for (String key : attrs.keySet()) {
	    		if (shouldWriteAttribute(component, key)) {
	            	writeAttribute(writer, key, attrs.get(key));
	        	}
			}
        }
        
        List<String> classes = ComponentUtils.getComponentClasses(component);
        if (classes.size() != 0) {
        	writeAttribute(writer, "class", StringUtils.join(classes, " "));
        }
    	
    }
    
    static String contextAttribute(String name, Object value) {
    	if (value != null && "data-ajax-url".equalsIgnoreCase(name) || 
    			"href".equalsIgnoreCase(name) || "src".equalsIgnoreCase(name)) {
			return RequestUtils.contextRelative(value.toString(), true);
    	} else {
			return value.toString();
    	}
    }
    
    static void writeAttribute(ResponseWriter writer, String name, Object value) throws IOException {
    	if (value != null && !"rendered".equalsIgnoreCase(name) && !"styleClass".equalsIgnoreCase(name)) {
    		writer.writeAttribute(name, contextAttribute(name, value), null);
        }
    }
    
    static String getExprId(String expr) {
		List<String> values = Arrays.asList(expr.replaceAll("^(\\$|#)\\{|\\}$", "").split("\\."));
    	if (values.size() > 1) {
	    	return StringUtils.join(values.subList(1, values.size()), ".");
    	} else if (values.size() > 0) {
    		return values.get(0);
    	}
		return null;
    }    

}