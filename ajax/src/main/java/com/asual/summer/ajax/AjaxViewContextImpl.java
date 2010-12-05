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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;

import javax.faces.component.visit.VisitHint;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.context.PartialResponseWriter;
import javax.faces.context.PartialViewContext;
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
	
    public AjaxViewContextImpl(FacesContext ctx) {
        super(ctx);
    }
    
    public void processPartial(PhaseId phaseId) {
    	
    	FacesContext context = FacesContext.getCurrentInstance();
        PartialViewContext partialViewContext = context.getPartialViewContext();
        Collection<String> renderIds = partialViewContext.getRenderIds();

        if (phaseId == PhaseId.RENDER_RESPONSE) {
            try {
                PartialResponseWriter writer = partialViewContext.getPartialResponseWriter();
                context.setResponseWriter(writer);
                ExternalContext exContext = context.getExternalContext();
                exContext.setResponseContentType("text/xml");
                exContext.addResponseHeader("Cache-Control", "no-cache");
                writer.startDocument();
                if (renderIds != null && !renderIds.isEmpty()) {
                    EnumSet<VisitHint> hints = EnumSet.of(VisitHint.SKIP_UNRENDERED, VisitHint.EXECUTE_LIFECYCLE);
                    context.getViewRoot().visitTree(
                    		new AjaxVisitContext(renderIds, hints), 
                    		new AjaxVisitCallback(phaseId));
                }
                writer.endDocument();
            } catch (IOException ioe) {
            } catch (RuntimeException e) {
                throw e;
            }
        }
    }
    
    public boolean isAjaxRequest() {
        if (ajaxRequest == null) {
            ajaxRequest = RequestUtils.isAjaxRequest();
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