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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;

import javax.faces.component.visit.VisitHint;
import javax.faces.context.FacesContext;
import javax.faces.context.PartialViewContext;
import javax.faces.context.ResponseWriter;
import javax.faces.event.PhaseId;

import org.apache.commons.lang.StringUtils;

import com.asual.summer.core.util.RequestUtils;
import com.sun.faces.context.PartialViewContextImpl;

/**
 * 
 * @author Rostislav Hristov
 *
 */
public class AjaxViewContextImpl extends PartialViewContextImpl {

	private Boolean ajaxRequest;
	private Collection<String> renderIds;
	
	public AjaxViewContextImpl(FacesContext context) {
		super(context);
	}
	
	public void processPartial(PhaseId phaseId) {
		
		FacesContext context = FacesContext.getCurrentInstance();
		PartialViewContext partialViewContext = context.getPartialViewContext();
		Collection<String> renderIds = partialViewContext.getRenderIds();

		if (phaseId == PhaseId.RENDER_RESPONSE) {
			try {
				ResponseWriter writer = FacesContext.getCurrentInstance().getResponseWriter();
				writer.startDocument();
				writer.write("<!DOCTYPE html>\n");
				writer.startElement("html", null);
				writer.startElement("title", null);
				writer.write("Ajax Response");
				writer.endElement("title");
				if (renderIds != null && !renderIds.isEmpty()) {
					EnumSet<VisitHint> hints = EnumSet.of(VisitHint.SKIP_UNRENDERED, VisitHint.EXECUTE_LIFECYCLE);
					context.getViewRoot().visitTree(
							new AjaxVisitContext(renderIds, hints), 
							new AjaxVisitCallback(phaseId));
				}
				writer.endElement("html");
				writer.endDocument();
			} catch (IOException ioe) {
			} catch (RuntimeException e) {
				throw e;
			}
		}
	}
	
	public ResponseWriter getResponseWriter() {
		return FacesContext.getCurrentInstance().getResponseWriter();
	}
	
	public boolean isAjaxRequest() {
		if (ajaxRequest == null) {
			ajaxRequest = RequestUtils.isAjaxRequest() && !getRenderIds().isEmpty();
		}
		return ajaxRequest;
	}
	
	public Collection<String> getRenderIds() {
		if (renderIds == null) {
			String param = (String) RequestUtils.getParameter("_ajax");
			if (StringUtils.isEmpty(param)) {
				renderIds = new ArrayList<String>();
			} else {
				renderIds = new ArrayList<String>(Arrays.asList(param.split("\\s+")));
			}			
		}
		return renderIds;
	}
	
}