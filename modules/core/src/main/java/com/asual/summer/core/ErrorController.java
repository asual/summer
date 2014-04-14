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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

import com.asual.summer.core.view.AbstractResponseView;
import com.asual.summer.core.view.StringView;

/**
 * 
 * @author Rostislav Hristov
 *
 */
@ControllerAdvice
public class ErrorController { 
	
	private Log logger = LogFactory.getLog(ErrorController.class);
	
	@ResponseViews({AbstractResponseView.class, StringView.class})
	public ModelAndView error(Exception ex) {
		
		Writer stringWriter = new StringWriter();
		PrintWriter printWriter = new PrintWriter(stringWriter);
		ex.printStackTrace(printWriter);
		
		ModelMap model = new ModelMap();
		model.addAttribute("error", ex);
		model.addAttribute("stackTrace", stringWriter.toString());
		return new ModelAndView("/error", model);
	}
	
	@ExceptionHandler(Exception.class)
	public ModelAndView handleException(Exception ex) {
		logger.info("Catching: " + ex.getClass().getSimpleName());
		logger.error(ex.getMessage(), ex);
		return error(ex);
	}


}