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
import javax.faces.application.ViewHandler;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseId;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.support.RequestContextUtils;
import org.springframework.web.servlet.view.InternalResourceView;

import com.asual.summer.core.ErrorResolver;
import com.asual.summer.core.RequestFilter;
import com.asual.summer.core.util.BeanUtils;
import com.asual.summer.core.util.RequestUtils;
import com.sun.faces.context.flash.ELFlash;

/**
 * 
 * @author Rostislav Hristov
 *
 */
@Named
public class HTMLView extends InternalResourceView implements ResponseView {

    private static final String DEFAULT_EXTENSION = "html";

    private String extension;
    
    public HTMLView() {
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
	
	@SuppressWarnings("unchecked")
	protected void renderMergedOutputModel(Map<String, Object> model, HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		FacesContext facesContext = RequestUtils.getFacesContext(request, response);
		
		ELFlash flash = (ELFlash) facesContext.getExternalContext().getFlash();
		Map<String, Map<String, Object>> errors = (Map<String, Map<String, Object>>) flash.get(RequestFilter.ERRORS);
		
		if (errors != null) {
			BeanUtils.getBeanOfType(ErrorResolver.class).prepareAttributes(model, request, errors, 
					(String) flash.get(RequestFilter.ERRORS_OBJECT_NAME), flash.get(RequestFilter.ERRORS_TARGET));
		}
		
		Iterator<String> i = model.keySet().iterator();
		while (i.hasNext()) {
			String key = i.next().toString();
			facesContext.getExternalContext().getRequestMap().put(key, model.get(key));
		}

		ViewHandler viewHandler = facesContext.getApplication().getViewHandler();
		viewHandler.initView(facesContext);

		UIViewRoot viewRoot = viewHandler.createView(facesContext, getUrl());
		viewRoot.setLocale(RequestContextUtils.getLocale(request));
		viewRoot.setTransient(true);
		
		facesContext.setCurrentPhaseId(PhaseId.RENDER_RESPONSE);
		//flash.doLastPhaseActions(facesContext, false);
		
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