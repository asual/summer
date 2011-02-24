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

import java.util.Collection;
import java.util.HashSet;
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
	
	private Collection<String> unvisitedClientIds;
	
	public AjaxVisitContext(Collection<String> clientIds, Set<VisitHint> hints) {
		super(FacesContext.getCurrentInstance(), clientIds, hints);
		unvisitedClientIds = new HashSet<String>(clientIds);
	}

	public VisitResult invokeVisitCallback(UIComponent component, 
			VisitCallback callback) {
		
		String clientId = component.getClientId();
		
		if (!getIdsToVisit().contains(clientId)) {
			clientId = null;
			return VisitResult.ACCEPT;
		}
		
		VisitResult result = callback.visit(this, component);
		unvisitedClientIds.remove(clientId);
		
		if (unvisitedClientIds.isEmpty()) {
			return VisitResult.COMPLETE;
		}
		
		return result;
	}
	
	public Collection<String> getSubtreeIdsToVisit(UIComponent component) {
		return getIdsToVisit();	 
	}

}