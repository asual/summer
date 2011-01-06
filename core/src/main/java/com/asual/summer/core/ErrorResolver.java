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

package com.asual.summer.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.context.Flash;
import javax.faces.event.PhaseId;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.InternalResourceView;

import com.asual.summer.core.util.RequestUtils;
import com.asual.summer.core.util.ResourceUtils;
import com.asual.summer.core.util.StringUtils;
import com.asual.summer.core.view.FlashView;

/**
 * 
 * @author Rostislav Hristov
 *
 */
@Named
public class ErrorResolver implements HandlerExceptionResolver {
	
	private final Log logger = LogFactory.getLog(getClass());
	public static final String ERRORS = "errors";
	
    @SuppressWarnings("unchecked")
    public ModelAndView resolveException(HttpServletRequest request,
			HttpServletResponse response, Object handler, Exception e) {
        
	    if (e instanceof BindException) {
	        
	    	Map<String, Map<String, Object>> errors = new HashMap<String, Map<String, Object>>();
	        
	        for (FieldError fe : (List<FieldError>) ((BindException) e).getFieldErrors()) {
		        Map<String, Object> error = new HashMap<String, Object>();
		        Object[] args = fe.getArguments();
		        String key = fe.isBindingFailure() ? 
		        		fe.getCodes()[2].replaceFirst("typeMismatch", "conversion") : "validation." + fe.getCodes()[2];
		        String message = ResourceUtils.getMessage(key, args);
		        if (message == null) {
		        	if (!fe.isBindingFailure()) {
		        		if (key.split("\\.").length > 3) {
		        			message = ResourceUtils.getMessage(key.substring(0, 
		        					key.indexOf(".", key.indexOf(".") + 1)) + key.substring(key.lastIndexOf(".")), args);
		        		}
		        		if (message == null && key.split("\\.").length > 2) {
		        			message = ResourceUtils.getMessage(key.substring(0, 
		        					key.indexOf(".", key.indexOf(".") + 1)), args);
		        		}
		        	} else if (fe.isBindingFailure() && message == null && key.split("\\.").length > 2) {
			        	message = ResourceUtils.getMessage(key.substring(0, 
			        			key.indexOf(".")) + key.substring(key.lastIndexOf(".")), args);
			        }
		        }
		        error.put("message",  message != null ? message : "Error (" + key + ")");
		        error.put("value", fe.getRejectedValue());
	        	errors.put(fe.getField(), error);
	        }
	        
	        String form = (String) RequestUtils.getParameter("_form");
	        if (form != null) {
				if (RequestUtils.isValidation()) {
					ModelMap map = new ModelMap();
					prepareAttributes(map, request, errors, ((BindException) e).getObjectName(), ((BindException) e).getTarget());
					return new ModelAndView(new InternalResourceView(form), map);
				} else {
					FacesContext facesContext = RequestUtils.getFacesContext(request, response);
					facesContext.setCurrentPhaseId(PhaseId.ANY_PHASE);
					Flash flash = facesContext.getExternalContext().getFlash();
					flash.put(RequestFilter.ERRORS, errors);
					flash.put(RequestFilter.ERRORS_OBJECT_NAME, ((BindException) e).getObjectName());
					flash.put(RequestFilter.ERRORS_TARGET, ((BindException) e).getTarget());
		        	return new ModelAndView(new FlashView(form, false));
				}
	        } else {
	            List<String> pairs = new ArrayList<String>();
	            for (String key : errors.keySet()) {
	                pairs.add(key + "=" + errors.get(key).get("message"));
	            }
		        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);	            
		        response.setHeader("Warning", StringUtils.join(pairs, ";"));	        	
	        }
	        
	    }
	    
		return null;
    }
    
    public void prepareAttributes(Map<String, Object> model, HttpServletRequest request, 
    		Map<String, Map<String, Object>> errors, String objectName, Object source) {
    	request.setAttribute(ERRORS, errors);
		if (!model.containsKey(objectName)) {
			try {
				model.put(objectName, source.getClass().getConstructor().newInstance());
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
		Object target = model.get(objectName);
		BeanUtils.copyProperties(source, target);
		model.put(objectName, target);
    }
    
}