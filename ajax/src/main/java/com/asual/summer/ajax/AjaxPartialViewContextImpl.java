package com.asual.summer.ajax;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

import com.sun.faces.context.PartialViewContextImpl;

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
