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
import java.util.HashMap;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.event.PhaseId;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.AbstractUrlBasedView;

import com.asual.summer.core.RequestFilter;
import com.asual.summer.core.util.RequestUtils;
import com.sun.faces.context.flash.ELFlash;

/**
 * 
 * @author Rostislav Hristov
 *
 */
public class FlashView extends AbstractUrlBasedView {

	private boolean contextRelative = false;
	private boolean http10Compatible = true;
	private HttpStatus statusCode;
	
	public FlashView(String url) {
		super(url);
	}
	
	public FlashView(String url, boolean contextRelative) {
		super(url);
		this.contextRelative = contextRelative;
	}

	public FlashView(String url, boolean contextRelative, boolean http10Compatible) {
		super(url);
		this.contextRelative = contextRelative;
		this.http10Compatible = http10Compatible;
	}
	
	protected void renderMergedOutputModel(
			Map<String, Object> model, HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		
		StringBuilder targetUrl = new StringBuilder();
		if (this.contextRelative && getUrl().startsWith("/")) {
			targetUrl.append(request.getContextPath());
		}
		targetUrl.append(getUrl());
		
		FacesContext facesContext = RequestUtils.getFacesContext(request, response);
		facesContext.setCurrentPhaseId(PhaseId.ANY_PHASE);
		
		ELFlash flash = (ELFlash) facesContext.getExternalContext().getFlash();
		flash.put(RequestFilter.FLASH_MODEL, model);
		
		Map<String, String[]> map = new HashMap<String, String[]>();
		map.putAll(request.getParameterMap());
		flash.put(RequestFilter.FLASH_PARAMETER_MAP, map);
		
		flash.doLastPhaseActions(facesContext, true);
		
		sendRedirect(request, response, targetUrl.toString(), this.http10Compatible);
	}
	
	protected void sendRedirect(
			HttpServletRequest request, HttpServletResponse response, String targetUrl, boolean http10Compatible)
			throws IOException {
		if (http10Compatible) {
			response.sendRedirect(response.encodeRedirectURL(targetUrl));
		}
		else {
			HttpStatus statusCode = getHttp11StatusCode(request, response, targetUrl);
			response.setStatus(statusCode.value());
			response.setHeader("Location", response.encodeRedirectURL(targetUrl));
		}
	}	
	
	protected HttpStatus getHttp11StatusCode(
			HttpServletRequest request, HttpServletResponse response, String targetUrl) {

		if (this.statusCode != null) {
			return this.statusCode;
		}
		HttpStatus attributeStatusCode = (HttpStatus) request.getAttribute(View.RESPONSE_STATUS_ATTRIBUTE);
		if (attributeStatusCode != null) {
			return attributeStatusCode;
		}
		return HttpStatus.SEE_OTHER;
	}
	
}
