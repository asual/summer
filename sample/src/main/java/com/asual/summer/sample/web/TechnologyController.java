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

import com.asual.summer.core.ResponseFormat;
import com.asual.summer.sample.domain.License;
import com.asual.summer.sample.domain.Status;
import com.asual.summer.sample.domain.Technology;

@Controller
@RequestMapping("/technology")
public class TechnologyController {
    
    @RequestMapping
    @ResponseFormat({"json", "xml"})
    public ModelAndView list() {
        return new ModelAndView("/list", new ModelMap(Technology.list()));
    }

    @RequestMapping("/add")
    public ModelAndView add() {
    	ModelMap model = new ModelMap();
    	model.addAllAttributes(Arrays.asList(new Object[] {License.list(), Status.list()}));
        return new ModelAndView("/add", model);
    }
    
    @RequestMapping("/{value}")
    @ResponseFormat({"json", "xml"})
    public ModelAndView view(@PathVariable("value") String value) {
        return new ModelAndView("/view", new ModelMap(Technology.find(value)));
    }
    
    @RequestMapping("/{value}/edit")
    public ModelAndView edit(@PathVariable("value") String value) {
    	ModelMap model = new ModelMap();
    	model.addAllAttributes(Arrays.asList(new Object[] {Technology.find(value), License.list(), Status.list()}));
        return new ModelAndView("/edit", model);
    }
    
    @RequestMapping(value="/save", method={RequestMethod.POST})
    public ModelAndView save(@Valid @ModelAttribute Technology technology) {
    	technology.persist();
        return new ModelAndView(new RedirectView("/technology/" + technology.getValue(), true));
    }
    
    @RequestMapping(value="/update", method={RequestMethod.PUT})
    public ModelAndView update(@Valid @ModelAttribute Technology technology) {
    	technology.merge();
        return new ModelAndView(new RedirectView("/technology/" + technology.getValue(), true));
    }
    
    @RequestMapping(value="/delete", method={RequestMethod.DELETE})
    public ModelAndView delete(@Valid @ModelAttribute Technology technology) {
    	technology.remove();
        return new ModelAndView(new RedirectView("/technology", true));
    }
   
}