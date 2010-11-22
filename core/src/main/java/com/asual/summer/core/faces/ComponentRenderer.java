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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.el.ValueExpression;
import javax.faces.application.Resource;
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

import com.asual.summer.core.util.RequestUtils;
import com.asual.summer.core.util.StringUtils;
import com.sun.faces.facelets.compiler.UIInstructions;
import com.sun.faces.facelets.tag.jsf.ComponentSupport;

/**
 * 
 * @author Rostislav Hristov
 *
 */
public class ComponentRenderer extends Renderer {

    private final Log logger = LogFactory.getLog(getClass());

    public final static String ATTRIBUTES = "idx|com.sun.faces.facelets.APPLIED";
    
    private String getComponentTag(UIComponent component) {
    	return (String) ((Component) component).getConfig("componentTag");
    }
    
    private String getComponentClass(UIComponent component) {
        return (String) ((Component) component).getConfig("componentClass");
    }
    
    private List<String> getComponentClasses(UIComponent component) {
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
    
    private boolean isComponentWrapper(UIComponent component) {
    	Object config = ((Component) component).getConfig("componentWrapper");
    	if (config != null) {
    		return config instanceof String ? Boolean.valueOf((String) config) : Boolean.valueOf((Boolean) config);
    	}
    	return false;
    }
    
    public void encodeBegin(FacesContext context, UIComponent component) throws IOException {
        
    	if (!isComponentWrapper(component)) {
    		
	        ResponseWriter writer = context.getResponseWriter();
    		String componentTag = getComponentTag(component);
	        writer.startElement(componentTag == null ? "div" : componentTag, component);
	        writeIdAttributeIfNecessary(context, writer, component);
	        
	        if (getComponentClass(component) == null) {
	
	        	Map<String, ValueExpression> bindings = ((Component) component).getBindings();
		        for (String key : bindings.keySet()) {
		    		if (shouldWriteAttribute(key)) {
			        	writeAttribute(writer, component, key, bindings.get(key).getValue(context.getELContext()));
		        	}
		        }
		        
		        Map<String, Object> attrs = component.getAttributes();
		        for (String key : attrs.keySet()) {
		    		if (shouldWriteAttribute(key)) {
		            	writeAttribute(writer, component, key, attrs.get(key));
		        	}
				}
	        }
	        
	        List<String> classes = getComponentClasses(component);
	        if (classes.size() != 0) {
	        	writeAttribute(writer, component, "class", StringUtils.join(classes, " "));
	        }
    	}
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
    	beginElement(component, name, false);
    }
    
    @SuppressWarnings("unchecked")
	public void beginElement(Component component, String name, boolean nameAttr) throws IOException {
        
    	ResponseWriter writer = FacesContext.getCurrentInstance().getResponseWriter();
        writer.write("<");
        writer.write(name);

        Map<String, Object> attrs = new HashMap<String, Object>(component.getAttributes());
        
    	Map<String, ValueExpression> bindings = component.getBindings();
        for (String key : bindings.keySet()) {
    		if (shouldWriteAttribute(key)) {
				attrs.put(key, bindings.get(key).getValue(FacesContext.getCurrentInstance().getELContext()));
        	}
        }
        
        if ("form".equals(name)) {

        	String action = getAttrValue(component, "action");
        	attrs.put("action", StringUtils.isEmpty(action) ? RequestUtils.getRequestURI() : RequestUtils.contextRelative(action, true));
        	
        	String method = getAttrValue(component, "method");
        	attrs.put("method", StringUtils.isEmpty(method) ? "get" : (RequestUtils.isMethodBrowserSupported(method) ? method : "post"));
        	
        	String enctype = getAttrValue(component, "enctype");
        	attrs.put("enctype", StringUtils.isEmpty(enctype) ? "application/x-www-form-urlencoded" : enctype);
        	
        } else if ("input".equals(name)) {
        	
			if (nameAttr) {
	        	attrs.put("id", component.getFormId());
	        	attrs.put("name", component.getFormName());
	        	if (component.isMatch() || Boolean.valueOf(getAttrValue(component, "checked"))) {
	        		attrs.put("checked", "checked");
	        	}
			} else {
				// TODO: Handle the case where only name is provided instead of an id
	        	attrs.put("id", component.getFormId());
	        	attrs.put("name", component.getFormId());
			}
			
			Map<String, Map<String, Object>> errors = 
				(Map<String, Map<String, Object>>) RequestUtils.getAttribute("errors");
        	attrs.put("value", errors != null && errors.get(component.getFormId()) != null ? 
        			errors.get(component.getFormId()).get("value") : getAttrValue(component, "value"));
			
        } else if ("option".equals(name)) {
        	
        	attrs.put("value", getAttrValue(component, "value"));
        	if (component.isMatch() || Boolean.valueOf(getAttrValue(component, "checked"))) {
        		attrs.put("selected", "selected");
        	}
        	
        } else if ("select".equals(name) || "textarea".equals(name)) {
        	
        	attrs.put("id", component.getFormId());
        	attrs.put("name", component.getFormId());
        	
        }
        
        for (String key : attrs.keySet()) {
    		if (shouldWriteAttribute(key)) {
    			writer.write(" " + key + "=\"" + attrs.get(key) + "\"");
    		}
        }
        
        writer.write(">");
    }
    
    public void endElement(Component component, String name) throws IOException {
        ResponseWriter writer = FacesContext.getCurrentInstance().getResponseWriter();
        writer.write("</");
        writer.write(name);
        writer.write(">");
    }
    
    protected void writeAttribute(ResponseWriter writer, UIComponent component, String name, Object value) throws IOException {
    	
    	if (!"rendered".equalsIgnoreCase(name) && !"styleClass".equalsIgnoreCase(name)) {
        	if ("selected".equalsIgnoreCase(name) || "checked".equalsIgnoreCase(name)) {
        		if ((value instanceof Boolean && (Boolean) value) || (value instanceof String && Boolean.valueOf((String) value))) {
        			writer.writeAttribute(name, name, null);
        		}
        	} else if ("action".equalsIgnoreCase(name) || "href".equalsIgnoreCase(name) || "src".equalsIgnoreCase(name)) {
    			writer.writeAttribute(name, RequestUtils.contextRelative(value != null ? value.toString() : "", true), null);
        	} else {
        		writer.writeAttribute(name, value, null);        		
        	}
        }
    }
    
    protected void writeIdAttributeIfNecessary(FacesContext context,
            ResponseWriter writer,
            UIComponent component) {

        String id = null;
        if (shouldWriteIdAttribute(component)) {
            try {
            	// TODO: Write an unique id for children of repeat components 
                // writer.writeAttribute("id", id = component.getClientId(context), "id");
            	ValueExpression ve = ((Component) component).getBindings().get("idx");
            	if (ve != null) {
            		id = (String) ve.getValue(FacesContext.getCurrentInstance().getELContext());
            	} else {
            		id = (String) component.getAttributes().get("id");
            	}
            	writer.writeAttribute("id", id, "id");
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        }
    }    
    
    protected boolean shouldWriteIdAttribute(UIComponent component) {

        String id;
        return (null != (id = component.getId()) && (isComponentWrapper(component) || getComponentTag(component) != null) && 
                    (!id.startsWith(UIViewRoot.UNIQUE_ID_PREFIX) ||
                        ((component instanceof ClientBehaviorHolder) &&
                          !((ClientBehaviorHolder) component).getClientBehaviors().isEmpty())));
    }

    public void encodeChildren(FacesContext context, UIComponent component) throws IOException {

        Map<String,UIComponent> facets = component.getFacets();
        UIComponent compositeRoot = facets.get(UIComponent.COMPOSITE_FACET_NAME);
        if (null == compositeRoot) {
            throw new IOException("Unable to find composite " + 
                    " component root for composite component with id " + 
                    component.getId() + " and class " + 
                    component.getClass().getName());
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
	
    public boolean getRendersChildren() {
        return true;
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
    	return "";
    }    
    
    private boolean shouldWriteAttribute(String key) {
        return !Pattern.compile(ATTRIBUTES + "|" + 
			FacesDecorator.ATTRIBUTES + "|" + 
			FacesDecorator.QNAME + "|" + 
			ComponentSupport.MARK_CREATED + "|" + 
			Resource.COMPONENT_RESOURCE_KEY + "|" + 
			UIComponent.BEANINFO_KEY + "|" + 
			UIComponent.FACETS_KEY + "|" + 
			UIComponent.VIEW_LOCATION_KEY, Pattern.CASE_INSENSITIVE).matcher(key).matches();
    }
    
}