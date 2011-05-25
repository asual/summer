/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *	  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.asual.summer.ajax;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.faces.component.UIComponent;
import javax.faces.component.visit.VisitCallback;
import javax.faces.component.visit.VisitHint;
import javax.faces.component.visit.VisitResult;
import javax.faces.context.FacesContext;

import com.sun.faces.component.visit.PartialVisitContext;

/**
 * 
 * @author Rostislav Hristov
 *
 */
public class AjaxVisitContext extends PartialVisitContext {
	
	private Collection<String> unknownClientIds;
	private Collection<String> unvisitedClientIds;
	private Map<String,Collection<String>> subtreeClientIds;
	
	public AjaxVisitContext(Collection<String> clientIds, Set<VisitHint> hints) {
		super(FacesContext.getCurrentInstance(), clientIds, hints);
		unknownClientIds = new HashSet<String>();
		unvisitedClientIds = new HashSet<String>(clientIds);
		subtreeClientIds = new HashMap<String,Collection<String>>();
		for (String id : unvisitedClientIds) {
			UIComponent component = findComponent(id, FacesContext.getCurrentInstance().getViewRoot());
			if (component != null) {
				String clientId = component.getClientId();
				while (component != null) {
					subtreeClientIds.put(component.getClientId(), Arrays.asList(clientId));
					component = component.getParent();
				}
			} else {
				unknownClientIds.add(id);
			}
		}
	}

	public VisitResult invokeVisitCallback(UIComponent component, 
			VisitCallback callback) {
		
		String clientId = component.getClientId();
		
		if (!unvisitedClientIds.contains(clientId)) {
			clientId = null;
			return VisitResult.ACCEPT;
		}
		
		VisitResult result = callback.visit(this, component);
		unvisitedClientIds.remove(clientId);
		unknownClientIds.remove(clientId);
		
		if (unvisitedClientIds.isEmpty()) {
			return VisitResult.COMPLETE;
		}
		
		return result;
	}
	
	private UIComponent findComponent(String expr, UIComponent component) {
		if (expr.equals(component.getClientId())) {
			return component;
		}
		UIComponent result = null;
		Iterator<UIComponent> children = component.getFacetsAndChildren();
		while(children.hasNext()) {
			UIComponent child = children.next();
			result = findComponent(expr, child);
			if (result != null) {
				return result;
			}
		}
		return result;
	}
	
	public Collection<String> getSubtreeIdsToVisit(UIComponent component) {
		if (unknownClientIds.size() != 0) {
			return unvisitedClientIds;
		}
		Collection<String> subtreeIdsToVisit = subtreeClientIds.get(component.getClientId());		
		if (subtreeIdsToVisit != null) {
			return subtreeIdsToVisit;
		} else {
			return Arrays.asList();
		}
	}

}