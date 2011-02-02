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

import javax.faces.component.UIComponent;
import javax.faces.component.UIPanel;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.render.Renderer;

import org.jboss.el.lang.EvaluationContext;
import org.jboss.el.lang.ExpressionBuilder;

import com.asual.summer.core.util.StringUtils;
import com.sun.faces.facelets.compiler.UIInstructions;

/**
 * 
 * @author Rostislav Hristov
 *
 */
public class CompositeComponentRenderer extends Renderer {

    public void encodeBegin(FacesContext context, UIComponent component) throws IOException {
        
    	if (!ComponentUtils.isComponentWrapper(component)) {
    		
    		String componentTag = ComponentUtils.getComponentTag(component);
	        ResponseWriter writer = context.getResponseWriter();
	        writer.startElement(componentTag == null ? "div" : componentTag, component);
	        
	        Map<String, Object> attrs;
	        
	        if (ComponentUtils.getComponentClass((Component) component) == null) {
	        	attrs = ComponentUtils.getAttributes((Component) component, componentTag);
	        } else {
	        	attrs = new HashMap<String, Object>();
	        }
	        
	        List<String> classes = ComponentUtils.getComponentClasses((Component) component);
	        if (classes.size() != 0) {
	        	attrs.put("class", StringUtils.join(classes, " "));
	        }
	        
	        for (String key : attrs.keySet()) {
	    		if (ComponentUtils.shouldWriteAttribute((Component) component, key) && attrs.get(key) != null) {
		        	ComponentUtils.writeAttribute(writer, key, attrs.get(key));
	    		}
	        }
    	}
    }
    
    public void encodeChildren(FacesContext context, UIComponent component) throws IOException {

        Map<String,UIComponent> facets = component.getFacets();
        UIComponent compositeRoot = facets.get(UIComponent.COMPOSITE_FACET_NAME);
        if (null == compositeRoot) {
            throw new IOException("Unable to find element [" + component.getClientId() + "].");
        }
        
        Map<String, Object> attrs = component.getAttributes();
        if ("false".equals((String) attrs.get("dataEscape"))) {
	        if (component.getFacetCount() == 1 && component.getFacet(UIComponent.COMPOSITE_FACET_NAME) instanceof UIPanel) {
	        	UIPanel panel = (UIPanel) component.getFacet(UIComponent.COMPOSITE_FACET_NAME);
	        	if (panel.getChildCount() == 1 && panel.getChildren().get(0) instanceof UIInstructions) {
		            ResponseWriter writer = context.getResponseWriter();
		            writer.write((String) ExpressionBuilder.createNode(panel.getChildren().get(0).toString())
		        			.getValue(new EvaluationContext(FacesContext.getCurrentInstance().getELContext(), null, null)));
		            return;
	        	}
	        }
        }
        
        compositeRoot.encodeAll(context);
    }
    
    public void encodeEnd(FacesContext context, UIComponent component)
          throws IOException {
    	
        ResponseWriter writer = context.getResponseWriter();
    	if (!ComponentUtils.isComponentWrapper(component)) {
    		String componentTag = ComponentUtils.getComponentTag(component);
	        writer.endElement(componentTag == null ? "div" : componentTag);
    	}
    }
    
	public void beginElement(Component component, String name) throws IOException {
        
    	FacesContext context = FacesContext.getCurrentInstance();
    	ResponseWriter writer = context.getResponseWriter();
        writer.write("<");
        writer.write(name);

        Map<String, Object> attrs = ComponentUtils.getAttributes(component, name);
        
        if (ComponentUtils.isComponentWrapper((UIComponent) component)) {
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

}