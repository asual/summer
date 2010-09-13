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

package com.asual.summer.sample.web;

import java.util.Date;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.asual.summer.core.ResponseFormat;

/**
 * 
 * @author Rostislav Hristov
 *
 */
@Controller
public class DefaultController {
	
	/**
	 * An optional controller method that creates a simple model for the index page of the sample.
	 * @return A standard model with attributes that can be accessed using EL expressions.
	 */
	@RequestMapping("/")
    public ModelMap index() {
		return new ModelMap("message", "A message from the sample controller!");
	}
	
	/**
	 * An extra method that maps a custom location and supports an extra JSON response representation.
	 * @return A simple model with a corresponding view path.
	 */
	@RequestMapping("/dynamic")
	@ResponseFormat({"json","xml"})
    public ModelAndView dynamic() {
		return new ModelAndView("/index", new ModelMap("message", "Dynamic content generated on " + new Date(System.currentTimeMillis()) + "."));
	}

}