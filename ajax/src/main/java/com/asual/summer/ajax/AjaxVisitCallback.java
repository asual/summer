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

import javax.faces.component.UIComponent;
import javax.faces.component.visit.VisitCallback;
import javax.faces.component.visit.VisitContext;
import javax.faces.component.visit.VisitResult;
import javax.faces.context.FacesContext;
import javax.faces.context.PartialResponseWriter;
import javax.faces.event.PhaseId;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * @author Rostislav Hristov
 *
 */
public class AjaxVisitCallback implements VisitCallback {

    private PhaseId phaseId;
    private final Log logger = LogFactory.getLog(getClass());

    public AjaxVisitCallback(PhaseId phaseId) {
        this.phaseId = phaseId;
    }

    public VisitResult visit(VisitContext visitContext, UIComponent component) {
    	FacesContext ctx = FacesContext.getCurrentInstance();
        if (phaseId == PhaseId.RENDER_RESPONSE) {
            try {
                PartialResponseWriter writer = ctx.getPartialViewContext().getPartialResponseWriter();
                writer.startUpdate(component.getClientId(ctx));
                component.encodeAll(ctx);
                writer.endUpdate();
            } catch (IOException e) {
            	logger.error(e.getMessage(), e);
            }
        }

        return VisitResult.REJECT;
    }
}