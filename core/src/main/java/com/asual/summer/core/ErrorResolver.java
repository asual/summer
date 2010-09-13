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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import com.asual.summer.core.util.ArrayUtils;
import com.asual.summer.core.util.ObjectUtils;
import com.asual.summer.core.util.ResourceUtils;

/**
 * 
 * @author Rostislav Hristov
 *
 */
@Component
public class ErrorResolver implements HandlerExceptionResolver {

    private final Log logger = LogFactory.getLog(getClass());
	
	public static final String ERRORS = "errors";
	public static final String ERRORS_TARGET = "errorsTarget";
	
    @SuppressWarnings("unchecked")
    public ModelAndView resolveException(HttpServletRequest request,
			HttpServletResponse response, Object handler, Exception e) {
        
    	Map<String, Map<String, Object>> errors = new HashMap<String, Map<String, Object>>();
    	
	    if (e instanceof BindException) {
	        
	        BindException be = (BindException) e;
	        Object target = be.getBindingResult().getTarget();
	        
	        for (FieldError fe : (List<FieldError>) be.getFieldErrors()) {
		        Map<String, Object> error = new HashMap<String, Object>();
		        error.put("message", fe.isBindingFailure() ? 
	        			ResourceUtils.getMessage(fe.getCodes()[2].replaceFirst("typeMismatch", "conversion")) : 
	        				ResourceUtils.getMessage("validation." + ArrayUtils.last(fe.getCodes()), fe.getArguments()));
		        error.put("value", fe.getRejectedValue());
	        	errors.put(fe.getField(), error);
	        }
	        
            List<String> pairs = new ArrayList<String>();
            for (String key : errors.keySet()) {
                pairs.add(key + "=" + errors.get(key));
            }

	        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
	        response.setHeader("Warning", StringUtils.join(pairs, ";"));

	        String form = request.getParameter("form");
	        if (form != null) {
		        try {
					return new ModelAndView(new RedirectView(form, false), 
							new ModelMap(ERRORS, ObjectUtils.serializeToBase64(new Object[] {errors, target})));
		        } catch (IOException ioe) {
					logger.error(ioe.getMessage(), ioe);
				}
	        } else {
	        	request.setAttribute(ERRORS, errors);
	        }
	    }
	    
		return null;
    }
    
}