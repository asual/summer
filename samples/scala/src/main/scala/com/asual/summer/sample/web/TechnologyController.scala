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

package com.asual.summer.sample.web

import com.asual.summer.core._
import com.asual.summer.core.view._
import com.asual.summer.json.JsonView
import com.asual.summer.xml.XmlView

import com.asual.summer.sample.domain._
import com.asual.summer.sample.domain.Technology.Image

import java.util.Arrays

import javax.servlet.http.HttpServletResponse
import javax.validation.Valid

import org.springframework.stereotype.Controller
import org.springframework.ui.ModelMap
import org.springframework.web.bind.annotation._
import org.springframework.web.servlet._
import org.springframework.web.servlet.view.RedirectView

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
	def list:ModelAndView = {
		return new ModelAndView("/list", new ModelMap(Technology.list))
	}
	
	@RequestMapping(method=Array(RequestMethod.POST))
	def persist(@Valid @ModelAttribute technology:Technology):ModelAndView = {
		technology.merge
		return new ModelAndView(new RedirectView("/technology/" + technology.value, true))
	}
	
	@RequestMapping(value=Array("/{value}"), method=Array(RequestMethod.GET))
	@ResponseViews(Array(classOf[JsonView], classOf[XmlView]))
	def view(@PathVariable("value") value:String):ModelAndView = {
		var technology:Technology = Technology.find(value)
		if (technology == null) {
			throw new ViewNotFoundException
		}
		return new ModelAndView("/view", new ModelMap(technology))
	}
	
	@RequestMapping(value=Array("/{value}"), method=Array(RequestMethod.PUT))
	def merge(@Valid @ModelAttribute technology:Technology):ModelAndView = {
		technology.merge
		return new ModelAndView(new RedirectView("/technology/" + technology.value, true))
	}
	
	@RequestMapping(value=Array("/{value}"), method=Array(RequestMethod.DELETE))
	def remove(@PathVariable("value") value:String):ModelAndView = {
		var technology:Technology = Technology.find(value)
		if (technology != null) {
			technology.remove
		}
		return new ModelAndView(new RedirectView("/technology", true))
	}
	
	@RequestMapping(Array("/add"))
	def add:ModelAndView = {
		var model:ModelMap = new ModelMap
		model.addAllAttributes(Arrays.asList(new Technology, License.list, Status.list))
		return new ModelAndView("/add", model)
	}
	
	@RequestMapping(Array("/{value}/edit"))
	def edit(@PathVariable("value") value:String):ModelAndView = {
		var technology:Technology = Technology.find(value)
		if (technology == null) {
			throw new ViewNotFoundException
		}
		var model:ModelMap = new ModelMap
		model.addAllAttributes(Arrays.asList(technology, License.list, Status.list))
		return new ModelAndView("/edit", model)
	}
	
	@RequestMapping(Array("/image/{value}"))
	def image(@PathVariable("value") value:String, response:HttpServletResponse) {
		var image:Image = Technology.findImage(value)
		if (image != null) {
			response.setContentLength(image.getBytes.length)
			response.setContentType(image.contentType)
			response.getOutputStream.write(image.getBytes)
		} else {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND)
		}
		response.getOutputStream.flush
		response.getOutputStream.close
	}
}