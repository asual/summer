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

import java.util.HashMap;
import java.util.Map;

import javax.el.ValueExpression;
import javax.faces.component.visit.VisitCallback;
import javax.faces.component.visit.VisitContext;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseId;
import javax.faces.render.Renderer;

import com.sun.faces.facelets.component.UIRepeat;
import com.sun.faces.facelets.el.TagValueExpression;

/**
 * 
 * @author Rostislav Hristov
 *
 */
public class RepeatComponent extends UIRepeat implements Component {
	
    public static final String COMPONENT_TYPE = "summer.faces.RepeatComponent";
    public static final String COMPONENT_FAMILY = "summer.faces.Facelets";
    
    private String dataEmptyOption;
    private String styleClass;
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
    
    public Map<String, ValueExpression> getBindings() {
    	return bindings;
    }
    
    public String getClientId(FacesContext context) {
    	return ComponentUtils.getClientId(this);
    }
    
    public String getVar() {
        String var = (String) getAttributes().get("dataVar");
        if (var == null) {
        	return "var";
        }
        return var;
    }

    public String getVarStatus() {
        String varStatus = (String) getAttributes().get("dataVarStatus");
        if (varStatus == null) {
        	return "varStatus";
        }
        return varStatus;
    }
    
    public Integer getBegin() {
    	if (super.getBegin() == null) {
    		Object attr = getAttributeExpression("dataRepeatBegin");
    		if (attr != null) {
    			return Integer.valueOf(attr.toString());
    		}
    	}
    	return super.getBegin();
    }
    
    public Integer getEnd() {
    	if (super.getEnd() == null) {
    		Object attr = getAttributeExpression("dataRepeatEnd");
    		if (attr != null) {
    			return Integer.valueOf(attr.toString());
    		}
    	}
    	return super.getEnd();
    }
    
    public Integer getStep() {
    	if (super.getStep() == null) {
    		Object attr = getAttributeExpression("dataRepeatStep");
    		if (attr != null) {
    			return Integer.valueOf(attr.toString());
    		}
    	}
    	return super.getStep();
    }
    
    public Object getValue() {
        if (super.getValue() == null) {
            return getAttributeExpression("dataRepeat");
        }
        return super.getValue();
    }
    
    public String getEmpty() {
        return (String) getAttributeExpression("dataEmpty");
    }
    
    public String getDataEmptyOption() {
    	if (dataEmptyOption == null) {
    		return (String) getAttributeExpression("dataEmptyOption");
    	}
        return dataEmptyOption;
    }
    
    public void setDataEmptyOption(String dataEmptyOption) {
        this.dataEmptyOption = dataEmptyOption;
    }
    
    public void setValueExpression(String name, ValueExpression binding) {
    	super.setValueExpression(name, binding);
    	bindings.put(name, binding);
		if (binding instanceof TagValueExpression) {
    		String value = ((TagValueExpression) binding).getExpressionString().replaceAll("^(\\$|#)\\{|\\}$", "");
    		bindingValues.put(name, value);
		}
    }
    
    public void process(FacesContext faces, PhaseId phase) {
    	populateAttributes();
    	super.process(faces, phase);
    }
    
    public boolean visitTree(VisitContext context, VisitCallback callback) {
    	populateAttributes();
    	return super.visitTree(context, callback);
    }
    
    private Object getAttributeExpression(String name) {
        ValueExpression ve = this.getValueExpression(name);
        if (ve != null) {
            return ve.getValue(getFacesContext().getELContext());
        }
        return null;
    }
    
    private void populateAttributes() {
    	setVar(getVar());
    	setVarStatus(getVarStatus());
    	setBegin(getBegin());
    	setEnd(getEnd());    	
    	setStep(getStep());    	
    }

}