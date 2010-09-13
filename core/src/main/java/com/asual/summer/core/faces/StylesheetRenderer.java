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

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ComponentSystemEvent;
import javax.faces.event.ComponentSystemEventListener;
import javax.faces.event.ListenerFor;
import javax.faces.event.PostAddToViewEvent;
import javax.faces.render.Renderer;

import com.asual.summer.core.util.RequestUtils;

/**
 * 
 * @author Rostislav Hristov
 *
 */
@ListenerFor(systemEventClass=PostAddToViewEvent.class)
public class StylesheetRenderer extends Renderer implements ComponentSystemEventListener {
    
    private static final String COMP_KEY =
    	StylesheetRenderer.class.getName() + "_COMPOSITE_COMPONENT";
    
    private static final List<String> ATTRIBUTES = Arrays.asList(new String[] {
		"charset",
		"content",
		"href",
		"hreflang",
		"http-equiv",
		"media",
		"name",
		"rel",
		"sizes",
		"src",
		"type"
    });

    public void processEvent(ComponentSystemEvent event) throws AbortProcessingException {
        UIComponent component = event.getComponent();
        FacesContext context = FacesContext.getCurrentInstance();
        // TODO: Implement target for the base tag
        String target = verifyTarget((String) component.getAttributes().get("target"));
        if (target != null) {
            UIComponent cc = UIComponent.getCurrentCompositeComponent(context);
            if (cc != null) {
                component.getAttributes().put(COMP_KEY, cc.getClientId(context));
            }
            context.getViewRoot().addComponentResource(context, component, target);
        }
    }

    public void encodeBegin(FacesContext context, UIComponent component)
          throws IOException {

        String ccID = (String) component.getAttributes().get(COMP_KEY);
        UIComponent cc = context.getViewRoot().findComponent(':' + ccID);
        UIComponent curCC = UIComponent.getCurrentCompositeComponent(context);
        if (cc != curCC) {
            component.popComponentFromEL(context);
            component.pushComponentToEL(context, cc);
            component.pushComponentToEL(context, component);
        }
        
        ResponseWriter writer = context.getResponseWriter();
        Map<String, Object> attrs = component.getAttributes();
        
    	String qName = (String) attrs.get(FacesDecorator.QNAME);
        writer.startElement(qName, component);
        for (String attr : ATTRIBUTES) {
        	String value = (String) attrs.get(attr);
        	if (value == null && component.getValueExpression(attr) != null) {
        		value = (String) component.getValueExpression(attr).getValue(context.getELContext());
        	}
        	if (value != null) {
        		if ("href".equals(attr) || "src".equals(attr)) {
	        		writer.writeAttribute(attr, RequestUtils.contextRelative((String) attrs.get(attr), true), attr);        			
        		} else {
	        		writer.writeAttribute(attr, attrs.get(attr), attr);
        		}
        	}
		}
    }

    public void encodeEnd(FacesContext context, UIComponent component)
          throws IOException {
    	
        String ccID = (String) component.getAttributes().get(COMP_KEY);
        if (ccID != null) {
            component.popComponentFromEL(context);
            component.popComponentFromEL(context);
            component.pushComponentToEL(context, component);
        }
        
        ResponseWriter writer = context.getResponseWriter();
        Map<String,Object> attrs = component.getAttributes();        
    	String qName = (String) attrs.get("qName");
    	writer.endElement(qName);
        writer.write("\n");
    }
    
    protected String verifyTarget(String toVerify) {
        return toVerify;
    }
    
    public void decode(FacesContext context, UIComponent component) {
    }    
}