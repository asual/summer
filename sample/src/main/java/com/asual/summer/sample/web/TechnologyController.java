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

import java.io.IOException;
import java.util.Arrays;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import com.asual.summer.core.ResponseFormat;
import com.asual.summer.sample.domain.License;
import com.asual.summer.sample.domain.Status;
import com.asual.summer.sample.domain.Technology;
import com.asual.summer.sample.domain.Technology.Image;

/**
 * 
 * @author Rostislav Hristov
 *
 */
@Controller
@RequestMapping("/technology")
public class TechnologyController {
	
    @RequestMapping(method=RequestMethod.GET)
    @ResponseFormat({"json", "xml"})
    public ModelAndView list() {
        return new ModelAndView("/list", new ModelMap(Technology.list()));
    }
    
    @RequestMapping(method=RequestMethod.POST)
    public ModelAndView persist(@Valid @ModelAttribute Technology technology) {
    	technology.persist();
        return new ModelAndView(new RedirectView("/technology/" + technology.getValue(), true));
    }
    
    @RequestMapping(value="/{value}", method=RequestMethod.GET)
    @ResponseFormat("*")
    public ModelAndView view(@PathVariable("value") String value) {
        return new ModelAndView("/view", new ModelMap(Technology.find(value)));
    }

    @RequestMapping(value="/{value}", method=RequestMethod.PUT)
    public ModelAndView merge(@Valid @ModelAttribute Technology technology) {
    	technology.merge();
        return new ModelAndView(new RedirectView("/technology/" + technology.getValue(), true));
    }
    
    @RequestMapping(value="/{value}", method=RequestMethod.DELETE)
    public ModelAndView remove(@Valid @ModelAttribute Technology technology) {
    	technology.remove();
        return new ModelAndView(new RedirectView("/technology", true));
    }
    
    @RequestMapping("/{value}/edit")
    public ModelAndView edit(@PathVariable("value") String value) {
    	ModelMap model = new ModelMap();
    	model.addAllAttributes(Arrays.asList(Technology.find(value), License.list(), Status.list()));
        return new ModelAndView("/edit", model);
    }

    @RequestMapping("/{value}/image")
    public void image(@PathVariable("value") String value, HttpServletResponse response) throws IOException {
		Image image = Technology.find(value).getImage();
		if (image != null) {
			response.setContentLength(image.getBytes().length);
			response.setContentType(image.getContentType());
			response.getOutputStream().write(image.getBytes());
		} else {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
		}
		response.getOutputStream().flush();
		response.getOutputStream().close();
    }
    
    @RequestMapping("/add")
    public ModelAndView add() {
    	ModelMap model = new ModelMap();
    	model.addAllAttributes(Arrays.asList(new Technology(), License.list(), Status.list()));
        return new ModelAndView("/add", model);
    }
    
}