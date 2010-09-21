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

package com.asual.summer.core;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.core.NamedThreadLocal;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.MultipartResolver;

import com.asual.summer.core.util.ObjectUtils;
import com.asual.summer.core.util.RequestUtils;
import com.asual.summer.core.util.StringUtils;

/**
 * 
 * @author Rostislav Hristov
 *
 */
public class RequestFilter extends OncePerRequestFilter {

	private static final Log logger = LogFactory.getLog(RequestFilter.class);
	
	private static final ThreadLocal<HttpServletRequest> requestHolder = new NamedThreadLocal<HttpServletRequest>("request");
    
	static void init(HttpServletRequest request) {
        String errors = request.getParameter(ErrorResolver.ERRORS);
        if (errors != null) {
        	try {
        		Object[] o = (Object[]) ObjectUtils.deserializeFromBase64(errors);
        		request.setAttribute(ErrorResolver.ERRORS, o[0]);
        		request.setAttribute(ErrorResolver.ERRORS_TARGET, o[1]);
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
        } 		
	}
	
	static String getRemoteAddr(HttpServletRequest request) {
        String result = request.getHeader("X-Forwarded-For");
        if (result == null) {
            result = request.getRemoteAddr();
        }
        try {
            result = InetAddress.getByName(result).getHostAddress();
        } catch (UnknownHostException e) {
        }
        return result;		
	}
	
	static String getRemoteHost(HttpServletRequest request) {
        String result = request.getHeader("X-Forwarded-For");
        if (result == null) {
            result = request.getRemoteHost();
        }
        try {
            result = InetAddress.getByName(result).getHostName();
        } catch (UnknownHostException e) {
        }
        return result;			
	}
	
	static String getServerName(HttpServletRequest request) {
        String result = request.getHeader("X-Forwarded-Host");
        if (result == null) {
            result = request.getServerName();
        }
        try {
            result = InetAddress.getByName(result).getHostName();
        } catch (UnknownHostException e) {
        }
        return result;			
	}
	
	static String getParameter(HttpServletRequest request, String paramName) {
        return encode(request.getParameter(paramName));
    }
    
	static String[] getParameterValues(HttpServletRequest request, String paramName) {
        String values[] = request.getParameterValues(paramName);
        if (values != null) {
            int length = values.length;
            for (int i = 0; i < length; i++) {
                values[i] = encode(values[i]);
            }
        }
        return values;        	
    }
	
    static Map<String, String[]> getParameterMap(HttpServletRequest request) {
        Map<String, String[]> map = new HashMap<String, String[]>();
        for (Object name : request.getParameterMap().keySet()) {
            map.put((String) name, getParameterValues(request, (String) name));
        }
        return map;
    }
	
    static String getMethod(HttpServletRequest request, Map<String, String[]> map) {
    	String method = map.get("_method") != null ? map.get("_method")[0] : null;
    	if ("POST".equalsIgnoreCase(request.getMethod()) && !StringUtils.isEmpty(method)) {
    		return method.toUpperCase(Locale.ENGLISH);
    	}
        return request.getMethod();
    }
    
    static String getRequestURI(HttpServletRequest request) {
        return encode(request.getRequestURI().replaceFirst("/$", ""));
    }

    static String getServletPath(HttpServletRequest request) {
        return encode(request.getServletPath());
    }
    
	// TODO: Remove when https://bugs.webkit.org/show_bug.cgi?id=27267 gets fixed.
    static String getHeader(HttpServletRequest request, String name) {
    	if ("Accept".equals(name) && RequestUtils.isWebKit()) {
			return "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8";
    	}
    	return request.getHeader(name);
    }
    
	static String encode(String input) {
        if (input != null && !"".equalsIgnoreCase(input.trim())) {
            try {
            	String encoding = StringUtils.getEncoding();
                String utf8 = new String(input.getBytes(encoding), encoding);
                String western = new String(input.getBytes("ISO-8859-1"), encoding);
                if (utf8.length() > western.length()) {
                    return western;
                } else {
                    return input;
                }
            } catch (UnsupportedEncodingException e) {
                return input;
            }
        }
        return input;
	}

    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
    		FilterChain filterChain) throws ServletException, IOException {        
        
		HttpServletRequest defaultRequest = new DefaultRequest(request);
		MultipartResolver multipartResolver = lookupMultipartResolver();
		
		if (multipartResolver != null && multipartResolver.isMultipart(request)) {
			defaultRequest = multipartResolver.resolveMultipart(defaultRequest);
		}		
		
    	try {
    		
	    	long time = System.currentTimeMillis();
	    	requestHolder.set(defaultRequest);
	    	
	        if (defaultRequest.getCharacterEncoding() == null) {
	        	defaultRequest.setCharacterEncoding(StringUtils.getEncoding());
	        }
	        
	        if (RequestUtils.isMSIE()) {
	        	response.setHeader("X-UA-Compatible", "IE=8");
	        }
	        
	        filterChain.doFilter(requestHolder.get(), response);
	        logger.debug("The request for '" + defaultRequest.getRequestURI() + "' took " + (System.currentTimeMillis() - time) + "ms.");
        
    	} finally {
        
			if (defaultRequest instanceof MultipartHttpServletRequest) {
				multipartResolver.cleanupMultipart((MultipartHttpServletRequest) defaultRequest);
			}
			
	        requestHolder.set(null);
    	}
    }
    
    public static HttpServletRequest getRequest() {
    	return requestHolder.get();
    }
    
	protected MultipartResolver lookupMultipartResolver() {
		try {
			WebApplicationContext wac = WebApplicationContextUtils.getRequiredWebApplicationContext(getServletContext());
			return wac.getBean("multipartResolver", MultipartResolver.class);
		} catch (NoSuchBeanDefinitionException e) {
			return null;
		}
	}    
}