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

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.el.ValueExpression;
import javax.faces.component.UIComponent;
import javax.faces.component.UIPanel;
import javax.faces.component.UIViewRoot;
import javax.faces.component.behavior.ClientBehaviorHolder;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.render.Renderer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.el.lang.EvaluationContext;
import org.jboss.el.lang.ExpressionBuilder;

import com.asual.summer.core.ErrorResolver;
import com.asual.summer.core.util.BeanUtils;
import com.asual.summer.core.util.RequestUtils;
import com.asual.summer.core.util.StringUtils;
import com.sun.faces.facelets.compiler.UIInstructions;

/**
 * 
 * @author Rostislav Hristov
 *
 */
public class CompositeComponentRenderer extends Renderer {

    private final Log logger = LogFactory.getLog(getClass());

    public void encodeBegin(FacesContext context, UIComponent component) throws IOException {
        
    	if (!isComponentWrapper(component)) {
    		
    		String componentTag = getComponentTag(component);
	        ResponseWriter writer = context.getResponseWriter();
	        writer.startElement(componentTag == null ? "div" : componentTag, component);
	        writeIdAttributeIfNecessary(context, writer, component);
	        ComponentUtils.writeAttributes((Component) component, writer);
	        
        	CompositeComponent cc = (CompositeComponent) component;
	        
	        if ("input".equals(componentTag)) {

	        	String type = (String) component.getAttributes().get("type");
	        	
	        	if (type != null && type.matches("checkbox|radio")) {
	        		ComponentUtils.writeAttribute(writer, "name", getFormName(cc));
		        	if (isMatch(cc) && getAttrValue(cc, "checked") == null) {
		        		ComponentUtils.writeAttribute(writer, "checked", "checked");
		        	}
	        	} else {
					// TODO: Handle the case where only name is provided instead of an id
	        		ComponentUtils.writeAttribute(writer, "name", getFormId(cc));
	        	}
	        	
	        } else if ("option".equals(componentTag)) {
	        	if (isMatch(cc) && getAttrValue(cc, "selected") == null) {
	        		ComponentUtils.writeAttribute(writer, "selected", "selected");
	        	}
	        }
    	}
    }
    
    public void encodeChildren(FacesContext context, UIComponent component) throws IOException {

        Map<String,UIComponent> facets = component.getFacets();
        UIComponent compositeRoot = facets.get(UIComponent.COMPOSITE_FACET_NAME);
        if (null == compositeRoot) {
            throw new IOException("Unable to find element " + component.getClientId());
        }
        
        Map<String, Object> attrs = component.getAttributes();
        if ("false".equals((String) attrs.get("dataEscape"))) {
	        if (component.getFacetCount() == 1 && component.getFacet(UIComponent.COMPOSITE_FACET_NAME) instanceof UIPanel) {
	        	UIPanel panel = (UIPanel) component.getFacet(UIComponent.COMPOSITE_FACET_NAME);
	        	if (panel.getChildCount() == 1 && panel.getChildren().get(0) instanceof UIInstructions) {
		            ResponseWriter writer = context.getResponseWriter();
		            writer.write((String) getExpressionValue(panel.getChildren().get(0).toString()));
		            return;
	        	}
	        }
        }
        
        compositeRoot.encodeAll(context);
    }
    
    public void encodeEnd(FacesContext context, UIComponent component)
          throws IOException {
    	
        ResponseWriter writer = context.getResponseWriter();
    	if (!isComponentWrapper(component)) {
    		String componentTag = getComponentTag(component);
	        writer.endElement(componentTag == null ? "div" : componentTag);
    	}
    }
    
	public void beginElement(Component component, String name) throws IOException {
        
    	FacesContext context = FacesContext.getCurrentInstance();
    	ResponseWriter writer = context.getResponseWriter();
        writer.write("<");
        writer.write(name);

        Map<String, Object> attrs = new HashMap<String, Object>(component.getAttributes());
        
    	Map<String, ValueExpression> bindings = component.getBindings();
        for (String key : bindings.keySet()) {
    		if (ComponentUtils.shouldWriteAttribute(component, key)) {
				attrs.put(key, bindings.get(key).getValue(FacesContext.getCurrentInstance().getELContext()));
        	}
        }
        
        if ("form".equals(name)) {

        	String action = getAttrValue(component, "action");
        	attrs.put("action", StringUtils.isEmpty(action) ? RequestUtils.getRequestUri() : RequestUtils.contextRelative(action, true));
        	
        	String method = getAttrValue(component, "method");
        	attrs.put("method", StringUtils.isEmpty(method) ? "get" : (RequestUtils.isMethodBrowserSupported(method) ? method : "post"));
        	
        	String enctype = getAttrValue(component, "enctype");
        	attrs.put("enctype", StringUtils.isEmpty(enctype) ? "application/x-www-form-urlencoded" : enctype);
        	
        } else if ("input".equals(name)) {
        	
        	String type = (String) attrs.get("type");
        	
        	if (type != null && type.matches("checkbox|radio")) {
	        	attrs.put("id", getFormId(component));
	        	attrs.put("name", getFormName(component));
	        	if (isMatch(component) && getAttrValue(component, "checked") == null) {
	        		attrs.put("checked", "checked");
	        	}
			} else {
				// TODO: Handle the case where only name is provided instead of an id
	        	attrs.put("id", getFormId(component));
	        	attrs.put("name", getFormId(component));
			}
        	
        	Map<String, Map<String, Object>> errors = BeanUtils.getBeanOfType(ErrorResolver.class).getErrors();
        	attrs.put("value", errors != null && errors.get(getFormId(component)) != null ? 
        			errors.get(getFormId(component)).get("value") : getAttrValue(component, "value"));
        	
        } else if ("select".equals(name) || "textarea".equals(name)) {
        	
        	attrs.put("id", getFormId(component));
        	attrs.put("name", getFormId(component));
        }
		
        if (isComponentWrapper((UIComponent) component)) {
        	attrs.put("class", component.getStyleClass());
        }
        
        for (String key : attrs.keySet()) {
    		if (ComponentUtils.shouldWriteAttribute(component, key) && attrs.get(key) != null) {
    			writer.write(" " + key + "=\"" + ComponentUtils.contextAttribute(key, attrs.get(key)) + "\"");
    		}
        }
        
        writer.write(">");
    }
    
    public void endElement(CompositeComponent component, String name) throws IOException {
        ResponseWriter writer = FacesContext.getCurrentInstance().getResponseWriter();
        writer.write("</");
        writer.write(name);
        writer.write(">");
    }

    public boolean getRendersChildren() {
        return true;
    }
    
    public String getFormId(Component component) {
    	String id = component.getClientId();
    	if (StringUtils.isEmpty(id)) {
    		id = ComponentUtils.getValueId(component);
    	}
    	if (!id.startsWith(UIViewRoot.UNIQUE_ID_PREFIX)) {
    		return id;
    	}
    	return null;
    }
    
    public String getFormName(Component component) {
    	String name = (String) component.getAttributes().get("name");
    	if (!StringUtils.isEmpty(name)) {
    		return name;
    	}
    	try {
	    	return ComponentUtils.getExprId(ComponentUtils.getRepeatComponent(component).getBindings().get("dataValue").getExpressionString());
    	} catch (Exception e) {
        	return getFormId(component);
    	}
    }
    
    private String getComponentTag(UIComponent component) {
    	return (String) ComponentUtils.getConfig((Component) component, "componentTag");
    }
    
    private void writeIdAttributeIfNecessary(FacesContext context,
            ResponseWriter writer,
            UIComponent component) {
        String id = component.getClientId();
        if (shouldWriteIdAttribute(component, id)) {
            try {
            	writer.writeAttribute("id", id, "id");
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        }
    }    
    
    private boolean shouldWriteIdAttribute(UIComponent component, String id) {
    	String componentTag = getComponentTag(component);
        return (id != null && (isComponentWrapper(component) || componentTag != null) && 
        			!"option".equals(componentTag) &&
                    (!id.startsWith(UIViewRoot.UNIQUE_ID_PREFIX) ||
                        ((component instanceof ClientBehaviorHolder) &&
                          !((ClientBehaviorHolder) component).getClientBehaviors().isEmpty())));
    }
    
    private boolean isComponentWrapper(UIComponent component) {
    	Object config = ComponentUtils.getConfig((Component) component, "componentWrapper");
    	if (config != null) {
    		return config instanceof String ? Boolean.valueOf((String) config) : Boolean.valueOf((Boolean) config);
    	}
    	return false;
    }
    
    private Object getExpressionValue(String expr) {
		return ExpressionBuilder.createNode(expr)
			.getValue(new EvaluationContext(FacesContext.getCurrentInstance().getELContext(), null, null));
    }
    
    private String getAttrValue(Component component, String key) {
    	Object value = component.getAttributes().get(key);
    	if (value == null) {
    		ValueExpression expr = component.getBindings().get(key);
    		if (expr != null) {
	    		value = expr.getValue(new EvaluationContext(FacesContext.getCurrentInstance().getELContext(), null, null));
    		}
    	}
    	if (value != null) {
    		return value.toString();
    	}
    	return null;
    }
    
	private boolean isMatch(Component component) {
    	try {
    		RepeatComponent repeatComponent = ComponentUtils.getRepeatComponent(component);
			Map<String, ValueExpression> bindings = repeatComponent.getBindings();
			FacesContext context = FacesContext.getCurrentInstance();
			Object dataValue = bindings.get("dataValue").getValue(context.getELContext());
        	Map<String, Map<String, Object>> errors = BeanUtils.getBeanOfType(ErrorResolver.class).getErrors();
			if (errors != null && errors.get(getFormName(component)) != null) {
				dataValue = errors.get(getFormName(component)).get("value");
			}
			Object valueAttr = RequestUtils.getAttribute(repeatComponent.getVar());
			if (dataValue instanceof List<?>) {
			 	return ((List<?>) dataValue).contains(valueAttr);
			} else if (dataValue instanceof Boolean) {
				return dataValue.equals(Boolean.valueOf(valueAttr.toString()));
			} else {
				return dataValue.equals(valueAttr);
			}
    	} catch (Exception e) {
    		return false;
    	}
    }    

}