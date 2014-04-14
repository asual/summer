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

package com.asual.summer.core.spring;

import java.util.Collection;
import java.util.List;
import java.util.Locale;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.MediaType;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.accept.ContentNegotiationManager;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.view.AbstractView;
import org.springframework.web.servlet.view.ContentNegotiatingViewResolver;

import com.asual.summer.core.ViewNotFoundException;

/**
 * 
 * @author Rostislav Hristov
 *
 */
public class ViewResolverConfiguration extends ContentNegotiatingViewResolver {
	
	private Log logger = LogFactory.getLog(ViewResolverConfiguration.class);
	
	private ContentNegotiationManager contentNegotiationManager;
	
	private List<ViewResolver> viewResolvers;
	
	public void setContentNegotiationManager(ContentNegotiationManager contentNegotiationManager){
		this.contentNegotiationManager = contentNegotiationManager;
		super.setContentNegotiationManager(contentNegotiationManager);
	}
	
	public List<ViewResolver> getViewResolvers() {
		return viewResolvers;
	}
	
	public void setViewResolvers(List<ViewResolver> viewResolvers) {
		this.viewResolvers = viewResolvers;
		super.setViewResolvers(viewResolvers);
	}

	public View resolveViewName(String viewName, Locale locale) throws Exception {
		View view = super.resolveViewName(viewName, locale);
		if (view == null) {
			throw new ViewNotFoundException();
		}
		return view;
	}
	
	public AbstractView handleViews(Collection<AbstractView> views,
			NativeWebRequest request) {
		List<MediaType> requestedMediaTypes;
		try {
			requestedMediaTypes = contentNegotiationManager.resolveMediaTypes(request);
			for(MediaType requestedMediaType : requestedMediaTypes){
				for (AbstractView view : views) {
					MediaType producableMediaType = MediaType.parseMediaType(view.getContentType());
					if(requestedMediaType.isCompatibleWith(producableMediaType) && !requestedMediaType.isWildcardType() && requestedMediaType.getQualityValue() > 0.9){
						return view;
					}
				}
				
			}
		} catch (HttpMediaTypeNotAcceptableException e) {
			logger.debug(e.getMessage(), e);
		}
		return null;
	}
	

}