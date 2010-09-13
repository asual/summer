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

package com.asual.summer.ajax;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

import com.sun.faces.context.PartialViewContextImpl;

/**
 * 
 * @author Rostislav Hristov
 *
 */
public class AjaxPartialViewContextImpl extends PartialViewContextImpl {

	public AjaxPartialViewContextImpl(FacesContext ctx) {
        super(ctx);
    }
	
    public Collection<String> getRenderIds() {
        Collection<String> renderIds = super.getRenderIds();
        List<String> ids = new ArrayList<String>();
        for (String id : renderIds) {
        	UIComponent c = findComponent(id, FacesContext.getCurrentInstance().getViewRoot());
        	if (c != null) {
        		ids.add(c.getClientId());
        	} else {
        		ids.add(id);
        	}			
		}
        return ids;
    }	

    private UIComponent findComponent(String expr, UIComponent component) {
    	UIComponent result = component.findComponent(expr);
    	if (result == null) {
    		Iterator<UIComponent> children = component.getFacetsAndChildren();
	    	while(children.hasNext()) {
	    		UIComponent child = children.next();
	    		if ((result = child.findComponent(expr)) != null || 
	    			(result = findComponent(expr, child)) != null) {
	    			return result;
	    		}
	    	}
    	}
		return result;
    }
}
