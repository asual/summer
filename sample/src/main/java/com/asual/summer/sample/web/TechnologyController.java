package com.asual.summer.sample.web;

import java.util.List;

import javax.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import com.asual.summer.core.ResponseView;
import com.asual.summer.sample.domain.License;
import com.asual.summer.sample.domain.Status;
import com.asual.summer.sample.domain.Technology;

@Controller
@RequestMapping("/technology")
public class TechnologyController {
    
    @RequestMapping
    @ResponseView("json")
    public ModelAndView list() {
        return new ModelAndView("/list", new ModelMap(Technology.list()));
    }

    @RequestMapping("/add")
    public ModelAndView add() {
        return new ModelAndView("/add");
    }
    
    @RequestMapping("/{value}")
    @ResponseView("json")
    public ModelAndView view(@PathVariable("value") String value) {
        return new ModelAndView("/view", new ModelMap(Technology.find(value)));
    }
    
    @RequestMapping("/{value}/edit")
    public ModelAndView edit(@PathVariable("value") String value) {
        return new ModelAndView("/edit", new ModelMap(Technology.find(value)));
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

    @ModelAttribute
    public List<License> getLicenseList() {
        return License.list();
    }
    
    @ModelAttribute
    public List<Status> getStatusList() {
        return Status.list();
    }    
}