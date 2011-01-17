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

package com.asual.summer.core.view;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import javax.faces.FacesException;
import javax.faces.FactoryFinder;
import javax.faces.application.ViewHandler;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.context.FacesContextFactory;
import javax.faces.event.PhaseId;
import javax.faces.lifecycle.LifecycleFactory;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.support.RequestContextUtils;
import org.springframework.web.servlet.view.InternalResourceView;

import com.asual.summer.core.util.RequestUtils;

/**
 * 
 * @author Rostislav Hristov
 *
 */
@Named
public class HtmlView extends InternalResourceView implements ResponseView {

    private static final String DEFAULT_EXTENSION = "html";
	private static LifecycleFactory lifecycleFactory;

    private String extension;
    
    public HtmlView() {
    	super();
    	setExtension(DEFAULT_EXTENSION);
    }

	public String getExtension() {
		return extension;
	}

	public void setExtension(String extension) {
		this.extension = extension;
	}
	
    protected boolean isUrlRequired() {
		return false;
	}
	
	protected void renderMergedOutputModel(Map<String, Object> model, HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		if (lifecycleFactory == null) {
			lifecycleFactory = (LifecycleFactory) FactoryFinder.getFactory(FactoryFinder.LIFECYCLE_FACTORY);
		}
		
		FacesContextFactory facesContextFactory = (FacesContextFactory) FactoryFinder.getFactory(FactoryFinder.FACES_CONTEXT_FACTORY);
		FacesContext facesContext = facesContextFactory.getFacesContext(
				RequestUtils.getServletContext(), request, response, lifecycleFactory.getLifecycle(LifecycleFactory.DEFAULT_LIFECYCLE));
		facesContext.setCurrentPhaseId(PhaseId.RESTORE_VIEW);
        facesContext.getExternalContext().getFlash().doPrePhaseActions(facesContext);
        
        Map<String, Object> requestMap = facesContext.getExternalContext().getRequestMap();
		Iterator<String> i = model.keySet().iterator();
		while (i.hasNext()) {
			String key = i.next().toString();
			if (!requestMap.containsKey(key)) {
				requestMap.put(key, model.get(key));
			}
		}
		
		ViewHandler viewHandler = facesContext.getApplication().getViewHandler();
		viewHandler.initView(facesContext);

		UIViewRoot viewRoot = viewHandler.createView(facesContext, getUrl());
		viewRoot.setLocale(RequestContextUtils.getLocale(request));
		viewRoot.setTransient(true);
		
		facesContext.setCurrentPhaseId(PhaseId.RENDER_RESPONSE);
		
		facesContext.setViewRoot(viewRoot);
		facesContext.renderResponse();
		
		try {
			facesContext.getApplication().getViewHandler().renderView(facesContext, viewRoot);
		} catch (IOException e) {
			throw new FacesException("An I/O error occurred during view rendering", e);
		} finally {
			facesContext.responseComplete();
			facesContext.release();
		}
	}

}