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

package com.asual.summer.core.spring;

import java.net.URL;
import java.util.List;

import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

import org.springframework.util.StringUtils;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.view.DefaultRequestToViewNameTranslator;

import com.asual.summer.core.ViewNotFoundException;
import com.asual.summer.core.util.BeanUtils;
import com.asual.summer.core.util.ClassUtils;
import com.asual.summer.core.util.RequestUtils;

/**
 * 
 * @author Rostislav Hristov
 *
 */
@Named("viewNameTranslator")
public class ViewNameTranslator extends DefaultRequestToViewNameTranslator {
	
	private final static String INDEX = "/index";
	
	private String findViewName(String prefix, String path, String suffix, boolean greedy) {

    	URL uri = ClassUtils.getClassLoader().getResource(prefix.replaceAll("^/", "") + path + suffix);
        if (uri != null) {
            return path;
        }
        
        uri = ClassUtils.getClassLoader().getResource(prefix.replaceAll("^/", "") + path + INDEX + suffix);
        if (uri != null) {
            return path + INDEX;
        }
        
        if (greedy && path.lastIndexOf("/") != -1) {
            return findViewName(prefix, path.substring(0, path.lastIndexOf("/")), suffix, greedy);
        }
        
	    return null;
	}
	
    public String getViewName(HttpServletRequest request) {

        String uri = StringUtils.stripFilenameExtension(
        		RequestUtils.contextRelative(request.getRequestURI().replaceAll("/+", "/"), false)).replaceFirst("/$", "");

        ExtendedInternalResourceViewResolver pagesResolver = null;
        
        ViewResolverConfiguration viewResolverConfiguraton = BeanUtils.getBeanOfType(ViewResolverConfiguration.class);
        List<ViewResolver> viewResolvers = viewResolverConfiguraton.getViewResolvers();
        
        for (ViewResolver vr : viewResolvers) {
            if (vr instanceof ExtendedInternalResourceViewResolver) {
            	ExtendedInternalResourceViewResolver resolver = (ExtendedInternalResourceViewResolver) vr;
            	if (resolver.getPrefix().endsWith("/pages")) {
	            	pagesResolver = resolver;
            	}
            }
        }
        
        String prefix = pagesResolver.getPrefix().replaceAll("/$", "");
        String suffix = pagesResolver.getSuffix();
        
        String viewName = findViewName(prefix, uri, suffix, false);
        if (viewName != null) {
            return viewName;
        }
        
        throw new ViewNotFoundException("The view [" + uri + "] does not exist.");
    }
    
}
