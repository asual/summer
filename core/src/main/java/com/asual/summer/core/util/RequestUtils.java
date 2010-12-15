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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.faces.context.FacesContext;
import javax.inject.Named;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

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

    public static HttpServletRequest getRequest() {
        return RequestFilter.getRequest();
    }

    public static String getRequestURI() {
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
    
    public static String getURL() {
        return getRequestURI() + (getQueryString() != null ? "?" + getQueryString() : "");
    }
    
    public static String getURL(String parameters) {
        Map<String, List<String>> map = new HashMap<String, List<String>>();
    	String[] params = parameters.split("&");
        for (String param : params) {
        	String[] pair = param.split("=");
            if (pair.length != 2 || pair[0].trim().length() == 0) {
                continue;
            }
        	if (!map.containsKey(pair[0])) {
        		map.put(pair[0], new ArrayList<String>());
        	}
        	map.get(pair[0]).add(pair[1]);
        }
    	return getRequestURI() + (map.size() != 0 ? StringUtils.decode(
    			FacesContext.getCurrentInstance().getExternalContext().encodeRedirectURL("?" + 
    					(getQueryString() != null ? getQueryString() : ""), map)) : "");
    }
    
    public static Map<String, Object[]> getParametersMap() {
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
        if (getParametersMap().get(name) != null) {
        	return getParametersMap().get(name)[0];
        }
        return null;
    }

    public static Object[] getParameterValues(String name) {
        return getParametersMap().get(name);
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
    
    public static String contextRelative(String uri, boolean contextRelative) {
        if (uri != null) {
            String contextPath = getRequest().getContextPath();
            if (contextRelative) {
                return uri.startsWith("/") ? contextPath.concat(uri) : uri;
            } else {
                return !"".equals(contextPath) ? uri.replaceFirst("^" + contextPath, "") : uri;
            }
        }
        return null;
    }

    public static Throwable getError() {
        return (Throwable) getAttribute("javax.servlet.error.exception");
    }

    public static int getErrorCode() {
        return (Integer) getAttribute("javax.servlet.error.status_code");
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

}