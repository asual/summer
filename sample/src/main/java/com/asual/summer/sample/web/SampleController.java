package com.asual.summer.sample.web;

import java.util.Date;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.asual.summer.core.ResponseView;

@Controller
public class SampleController {
	
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
	@ResponseView("json")
    public ModelAndView dynamic() {
		return new ModelAndView("/index", new ModelMap("message", "Dynamic content generated on " + new Date(System.currentTimeMillis()) + "."));
	}

}