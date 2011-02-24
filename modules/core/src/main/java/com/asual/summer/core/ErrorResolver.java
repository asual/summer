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

package com.asual.summer.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.InternalResourceView;

import com.asual.summer.core.util.RequestUtils;
import com.asual.summer.core.util.ResourceUtils;
import com.asual.summer.core.util.StringUtils;

/**
 * 
 * @author Rostislav Hristov
 *
 */
@Named
public class ErrorResolver implements HandlerExceptionResolver {
	
	private static final String ERRORS = "errors";
	private final Log logger = LogFactory.getLog(getClass());
	
	@SuppressWarnings("unchecked")
	public ModelAndView resolveException(HttpServletRequest request,
			HttpServletResponse response, Object handler, Exception e) {
		
		if (e instanceof BindException) {
			
			BindException be = (BindException) e;
			Map<String, Map<String, Object>> errors = new HashMap<String, Map<String, Object>>();
			
			for (FieldError fe : (List<FieldError>) be.getFieldErrors()) {
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
				if (request.getAttribute(ERRORS) == null) {
					request.setAttribute(ERRORS, errors);
					request.setAttribute(be.getObjectName(), be.getTarget());
					return new ModelAndView(new InternalResourceView(form));
				}
			} else {
				List<String> pairs = new ArrayList<String>();
				for (String key : errors.keySet()) {
					pairs.add(key + "=" + errors.get(key).get("message"));
				}
				try {
					response.sendError(HttpServletResponse.SC_BAD_REQUEST, StringUtils.join(pairs, ";"));
				} catch (IOException ioe) {
					logger.error(ioe.getMessage(), ioe);
				}
			}
		}
		
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public Map<String, Map<String, Object>> getErrors() {
		return (Map<String, Map<String, Object>>) RequestUtils.getAttribute(ERRORS);
	}
	
}