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

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.MediaType;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.view.ContentNegotiatingViewResolver;

public class ViewResolverConfiguration extends ContentNegotiatingViewResolver {
	
    private List<ViewResolver> viewResolvers;
    private ConcurrentMap<String, String> mediaTypes = new ConcurrentHashMap<String, String>();
	private String parameterName = "format";
	private boolean favorPathExtension = true;
	private boolean favorParameter = false;
	private boolean ignoreAcceptHeader = false;
	
    public List<MediaType> getMediaTypes(HttpServletRequest request) {
        return super.getMediaTypes(request);
    }
	
	public void setMediaTypes(Map<String, String> mediaTypes) {
		this.mediaTypes.putAll(mediaTypes);
		super.setMediaTypes(mediaTypes);
	}
	
    public MediaType getMediaTypeFromFilename(String filename) {
        return super.getMediaTypeFromFilename(filename);
    }

    public List<ViewResolver> getViewResolvers() {
        return viewResolvers;
    }
    
    public void setViewResolvers(List<ViewResolver> viewResolvers) {
        this.viewResolvers = viewResolvers;
        super.setViewResolvers(viewResolvers);
    }

    public ExtendedInternalResourceViewResolver getPrivateViewResolver() {
        for (ViewResolver vr : getViewResolvers()) {
            if (vr instanceof ExtendedInternalResourceViewResolver) {
            	ExtendedInternalResourceViewResolver resolver = (ExtendedInternalResourceViewResolver) vr;
            	if (resolver.getPrefix().endsWith("/private")) {
	            	return resolver;
            	}
            }
        }
        return null;
    }
    
    public ExtendedInternalResourceViewResolver getPublicViewResolver() {
        for (ViewResolver vr : getViewResolvers()) {
            if (vr instanceof ExtendedInternalResourceViewResolver) {
            	ExtendedInternalResourceViewResolver resolver = (ExtendedInternalResourceViewResolver) vr;
            	if (resolver.getPrefix().endsWith("/public")) {
	            	return resolver;
            	}
            }
        }
        return null;
    }
    
    public View resolveViewName(String viewName, Locale locale) throws Exception {
        View view = super.resolveViewName(viewName, locale);
        if (view == null) {
            for (ViewResolver vr : getViewResolvers()) {
                if (vr instanceof ExtendedInternalResourceViewResolver) {
                    view = vr.resolveViewName(viewName, locale);
                }
            }
        }
        return view;
    }
	
	public boolean getFavorPathExtension() {
		return favorPathExtension;
	}
	
	public void setFavorPathExtension(boolean favorPathExtension) {
		super.setFavorPathExtension(favorPathExtension);
		this.favorPathExtension = favorPathExtension;
	}

	public boolean getFavorParameter() {
		return favorParameter;
	}

	public void setFavorParameter(boolean favorParameter) {
		super.setFavorParameter(favorParameter);
		this.favorParameter = favorParameter;
	}

	public String getParameterName() {
		return parameterName;
	}
	
	public void setParameterName(String parameterName) {
		super.setParameterName(parameterName);
		this.parameterName = parameterName;
	}

	public boolean getIgnoreAcceptHeader() {
		return ignoreAcceptHeader;
	}
	
	public void setIgnoreAcceptHeader(boolean ignoreAcceptHeader) {
		super.setIgnoreAcceptHeader(ignoreAcceptHeader);
		this.ignoreAcceptHeader = ignoreAcceptHeader;
	}

}