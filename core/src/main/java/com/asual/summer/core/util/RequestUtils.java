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

package com.asual.summer.core.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.faces.FactoryFinder;
import javax.faces.context.FacesContext;
import javax.faces.context.FacesContextFactory;
import javax.faces.event.PhaseId;
import javax.faces.lifecycle.LifecycleFactory;
import javax.inject.Named;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.util.WebUtils;

import com.asual.summer.core.RequestFilter;

/**
 * 
 * @author Rostislav Hristov
 *
 */
@Named
public class RequestUtils implements ApplicationContextAware {
	
	private static ApplicationContext applicationContext;
	private static LifecycleFactory lifecycleFactory;

	private static final String QUERY_STRING_SEPARATOR = "?";
	private static final String PARAMETER_SEPARATOR = "&";
	private static final String NAME_VALUE_SEPARATOR = "=";

    public static HttpServletRequest getRequest() {
        return RequestFilter.getRequest();
    }

    public static String getRequestUri() {
        String requestUri = (String) getAttribute(WebUtils.FORWARD_REQUEST_URI_ATTRIBUTE);
        if (requestUri != null) {
            return requestUri;
        }
        return getRequest().getRequestURI();
    }

    public static String getQueryString() {
        String requestURI = (String) getAttribute(WebUtils.FORWARD_REQUEST_URI_ATTRIBUTE);
        if (requestURI != null) {
            return (String) getAttribute(WebUtils.FORWARD_QUERY_STRING_ATTRIBUTE);
        }
        return getRequest().getQueryString();
    }
    
    public static String getUrl() {
        return getRequestUri() + (getQueryString() != null ? QUERY_STRING_SEPARATOR + getQueryString() : "");
    }
    
    public static UrlBuilder getUrlBuilder() {
    	return getUrlBuilder(getUrl());
    }
    
    public static UrlBuilder getUrlBuilder(String url) {
    	return new UrlBuilder(url);
    }
    
    public static Map<String, Object[]> getParameterMap() {
        Map<String, Object[]> normalized = new HashMap<String, Object[]>();
        Map<String, String[]> params = getRequest().getParameterMap();
        for (String key : params.keySet()) {
            String[] value = (String[]) params.get(key);
            Object[] result = new Object[value.length];
            for (int i = 0; i < value.length; i++) {
                result[i] = ObjectUtils.convert(value[i]);
            }
            normalized.put(key, result);
        }
        return normalized;
    }
    
    public static Object getParameter(String name) {
        if (getParameterMap().get(name) != null) {
        	return getParameterMap().get(name)[0];
        }
        return null;
    }
    
    public static Object[] getParameterValues(String name) {
        return getParameterMap().get(name);
    }
    
    public static String getHeader(String name) {
        return getRequest().getHeader(name);
    }

    public static String getUserAgent() {
        return getHeader("User-Agent");
    }
    
    public static boolean isValidation() {
    	return "Validation".equals(getHeader("X-Requested-Operation"));
    }
    
    public static boolean isAjaxRequest() {
        return "XMLHttpRequest".equals(getHeader("X-Requested-With"));
    }
        
    public static boolean isGetRequest() {
        return "GET".equalsIgnoreCase(getRequest().getMethod());
    }

    public static boolean isPostRequest() {
        return "POST".equalsIgnoreCase(getRequest().getMethod());
    }
    
    public static boolean isMethodBrowserSupported(String method) {
        return ("GET".equalsIgnoreCase(method) || "POST".equalsIgnoreCase(method));
    }
    
    public static boolean isMozilla() {
        return Pattern.compile("Mozilla").matcher(getUserAgent()).find() && !Pattern.compile("compatible|WebKit").matcher(getUserAgent()).find();
    }
    
    public static boolean isMSIE() {
        return Pattern.compile("MSIE").matcher(getUserAgent()).find() && !Pattern.compile("Opera").matcher(getUserAgent()).find();
    }
    
    public static boolean isOpera() {
        return Pattern.compile("Opera").matcher(getUserAgent()).find();
    }
    
    public static boolean isWebKit() {
        return Pattern.compile("WebKit").matcher(getUserAgent()).find();
    }
    
    public static boolean isMobile() {
        return Pattern.compile(" Mobile/").matcher(getUserAgent()).find();
    }

    public static String getClient() {
    	List<String> data = new ArrayList<String>();
    	if (isMozilla()) {
    		data.add("mozilla");
    	} else if (isMSIE()) {
    		data.add("msie");
    	} else if (isOpera()) {
    		data.add("opera");
    	} else if (isWebKit()) {
    		data.add("webkit");
    	}
    	if (isMobile()) {
    		data.add("mobile");
    	}
    	return StringUtils.join(data, " ");
    }
    
    public static void setAttribute(String name, Object value) {
        getRequest().setAttribute(name, value);
    }

    public static Object getAttribute(String name) {
        return getRequest().getAttribute(name);
    }
    
	public static String serializeParameters(Map<String, String[]> parameterMap) {
		List<String> pairs = new ArrayList<String>();
		for (String key : parameterMap.keySet()) {
			for (String value : parameterMap.get(key)) {
				pairs.add(key + NAME_VALUE_SEPARATOR + StringUtils.encode(value));
			}
		}
		return StringUtils.join(pairs, PARAMETER_SEPARATOR);
	}
	
    public static String contextRelative(String uri, boolean contextRelative) {
        if (uri != null && uri.startsWith("/")) {
            String contextPath = getRequest().getContextPath();
        	uri = uri.replaceFirst("^" + contextPath + "/?", "/");
            if (contextRelative) {
                uri = contextPath.concat(uri);
            }
        }
        return uri;
    }

    public static Throwable getError() {
        return (Throwable) getAttribute("javax.servlet.error.exception");
    }

    public static int getErrorCode() {
        return (Integer) getAttribute("javax.servlet.error.status_code");
    }

    public static FacesContext getFacesContext(HttpServletRequest request, HttpServletResponse response) {
		if (lifecycleFactory == null) {
			lifecycleFactory = (LifecycleFactory) FactoryFinder.getFactory(FactoryFinder.LIFECYCLE_FACTORY);
		}
		if (FacesContext.getCurrentInstance() == null) {
			FacesContextFactory facesContextFactory = (FacesContextFactory) FactoryFinder.getFactory(FactoryFinder.FACES_CONTEXT_FACTORY);
			FacesContext facesContext = facesContextFactory.getFacesContext(
					RequestUtils.getServletContext(), request, response, lifecycleFactory.getLifecycle(LifecycleFactory.DEFAULT_LIFECYCLE));
			facesContext.setCurrentPhaseId(PhaseId.RESTORE_VIEW);
            facesContext.getExternalContext().getFlash().doPrePhaseActions(facesContext);
		}
		return FacesContext.getCurrentInstance();
    }
    
    public static ServletContext getServletContext() {
    	if (applicationContext instanceof WebApplicationContext) {
    		return ((WebApplicationContext) applicationContext).getServletContext();
    	}
    	return null;
    }

	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		RequestUtils.applicationContext = applicationContext;
	}
	
	private static class UrlBuilder {
		
		private String path;
	    private String extension;
		private Map<String, List<String>> parameters = new LinkedHashMap<String, List<String>>();

		public UrlBuilder(String url) {
	        int index = url.indexOf(QUERY_STRING_SEPARATOR);
        	String urlPath = index != -1 ? url.substring(0, index) : url;
        	setPath(WebUtils.extractFilenameFromUrlPath(urlPath));
        	setExtension(urlPath.equals(path) ? null : urlPath.substring(path.length() + 1));
	        if (index != -1) {
	            addParameters(url.substring(index + 1));
	        }
		}

	    public UrlBuilder setPath(String path) {
            this.path = path;
            return this;
	    }
		
		public UrlBuilder setExtension(String extension) {
			this.extension = extension;
			return this;
		}
		
		@SuppressWarnings("unused")
		public UrlBuilder addPath(String path) {
			setPath(this.path.concat(path));
			return this;
		}
		
		@SuppressWarnings("unused")
		public UrlBuilder removePath(String path) {
			setPath(this.path.replaceFirst(path + "$", ""));
			return this;
		}
		
		public UrlBuilder addParameter(String parameter) {
			if (!StringUtils.isEmpty(parameter)) {
				String[] pair = parameter.split(NAME_VALUE_SEPARATOR);
				if (pair.length > 0) {
				    addParameter(pair[0], pair.length > 1 ? StringUtils.decode(pair[1]) : null);
				}
			}
			return this;
		}
		
		public UrlBuilder addParameter(String name, Object value) {
			if (value != null) {
				List<String> values = new ArrayList<String>();
				if (value instanceof String) {
					values.add((String) value);
				} else if (value.getClass().isArray()) {
					for (Object v : (Object[]) value) {
						values.add(String.valueOf(v));
					}
				} else if (value instanceof Collection) {
					for (Object v : (Collection<?>) value) {
						values.add(String.valueOf(v));
					}
				} else {
					values.add(String.valueOf(value));
				}
			    parameters.put(name, values);
			    return this;
			}
			removeParameter(name);
			return this;
		}
	    
		public UrlBuilder removeParameter(String name) {
		    parameters.remove(name);
		    return this;
	    }
	    
		public UrlBuilder addParameters(String parameters) {
			if (!StringUtils.isEmpty(parameters)) {
				String[] params = parameters.replaceAll("&amp;", PARAMETER_SEPARATOR).split(PARAMETER_SEPARATOR);
				for (String param : params) {
					addParameter(param);
				}
			}
			return this;
		}
		
		public String toString() {
			StringBuilder sb = new StringBuilder();
			if (!StringUtils.isEmpty(extension)) {
				sb.append(path.replaceAll("(.+)/$", "$1"));
				if (!extension.startsWith(".")) {
					sb.append(".");
				}
				sb.append(extension);
			} else {
				sb.append(path);
			}
			for (String key : parameters.keySet()) {
	            sb.append(sb.toString().contains(QUERY_STRING_SEPARATOR) ? PARAMETER_SEPARATOR : QUERY_STRING_SEPARATOR);
				for (String v : parameters.get(key)) {
					sb.append(key);
					sb.append(NAME_VALUE_SEPARATOR);
					sb.append(StringUtils.encode(v));
				}
			}
			return sb.toString();
		}
	}
}