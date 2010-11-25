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

import java.util.Arrays;

import javax.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import com.asual.summer.core.ResponseViews;
import com.asual.summer.core.ViewNotFoundException;
import com.asual.summer.core.view.AbstractResponseView;
import com.asual.summer.core.view.JSONView;
import com.asual.summer.core.view.XMLView;

import com.asual.summer.sample.domain.License;
import com.asual.summer.sample.domain.Status;
import com.asual.summer.sample.domain.Technology;

/**
 * 
 * @author Rostislav Hristov
 *
 */
@Controller
@RequestMapping(Array("/technology"))
class TechnologyController {
	
    @RequestMapping(method=Array(RequestMethod.GET))
    @ResponseViews(Array(classOf[AbstractResponseView]))
    def list():ModelAndView = {
    	return new ModelAndView("/list", new ModelMap(Technology.list()));
    }
    
    @RequestMapping(method=Array(RequestMethod.POST))
    def persist(@Valid @ModelAttribute technology:Technology):ModelAndView = {
    	technology.persist();
        return new ModelAndView(new RedirectView("/technology/" + technology.getValue(), true));
    }
    
    @RequestMapping(value=Array("/{value}"), method=Array(RequestMethod.GET))
    @ResponseViews(Array(classOf[JSONView], classOf[XMLView]))
    def view(@PathVariable("value") value:String):ModelAndView = {
    	var technology:Technology = Technology.find(value);
    	if (technology == null) {
    		throw new ViewNotFoundException();
    	}
        return new ModelAndView("/view", new ModelMap(technology));
    }
    
    @RequestMapping(value=Array("/{value}"), method=Array(RequestMethod.PUT))
    def merge(@Valid @ModelAttribute technology:Technology):ModelAndView = {
    	technology.merge();
        return new ModelAndView(new RedirectView("/technology/" + technology.getValue(), true));
    }
    
    @RequestMapping(value=Array("/{value}"), method=Array(RequestMethod.DELETE))
    def remove(@Valid @ModelAttribute technology:Technology):ModelAndView = {
    	technology.remove();
        return new ModelAndView(new RedirectView("/technology", true));
    }
    
    @RequestMapping(Array("/add"))
    def add():ModelAndView = {
        var model:ModelMap = new ModelMap();
    	model.addAllAttributes(Arrays.asList(new Technology(), License.list(), Status.list()));
        return new ModelAndView("/add", model);
    }
    
    @RequestMapping(Array("/{value}/edit"))
    def edit(@PathVariable("value") value:String):ModelAndView = {
    	var technology:Technology = Technology.find(value);
    	if (technology == null) {
    		throw new ViewNotFoundException();
    	}
    	var model:ModelMap = new ModelMap();
    	model.addAllAttributes(Arrays.asList(technology, License.list(), Status.list()));
        return new ModelAndView("/edit", model);
    }
    
}