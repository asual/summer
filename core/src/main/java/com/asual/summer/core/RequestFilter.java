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
import java.net.UnknownHostException;
import java.util.Locale;
import java.util.Map;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.ClassUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.NamedThreadLocal;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.MultipartResolver;

import com.asual.summer.core.spring.RequestMultipartResolver;
import com.asual.summer.core.util.RequestUtils;
import com.asual.summer.core.util.ResourceUtils;
import com.asual.summer.core.util.StringUtils;

/**
 * 
 * @author Rostislav Hristov
 *
 */
public class RequestFilter extends OncePerRequestFilter {
	
	private static final Log logger = LogFactory.getLog(RequestFilter.class);
	private static final ThreadLocal<HttpServletRequest> requestHolder = new NamedThreadLocal<HttpServletRequest>("request");
	
	private MultipartResolver multipartResolver = null;
	
	static String getRemoteAddr(HttpServletRequest request) {
        String result = request.getHeader("X-Forwarded-For");
        if (result == null) {
            result = request.getRemoteAddr();
        }
        try {
            result = java.net.InetAddress.getByName(result).getHostAddress();
        } catch (NoClassDefFoundError e) {
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
            result = java.net.InetAddress.getByName(result).getHostName();
        } catch (NoClassDefFoundError e) {
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
            result = java.net.InetAddress.getByName(result).getHostName();
        } catch (NoClassDefFoundError e) {
        } catch (UnknownHostException e) {
        }
        return result;			
	}
	
    static String getMethod(HttpServletRequest request, String requestMethod, Map<String, String[]> map) {
    	String method = map.get("_method") != null ? map.get("_method")[0] : null;
    	if ("POST".equalsIgnoreCase(requestMethod) && !StringUtils.isEmpty(method)) {
    		return method.toUpperCase(Locale.ENGLISH);
    	}
        return requestMethod;
    }
    
	// TODO: Remove when https://bugs.webkit.org/show_bug.cgi?id=27267 gets fixed.
    static String getHeader(HttpServletRequest request, String name) {
    	if ("Accept".equals(name) && RequestUtils.isWebKit()) {
			return "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8";
    	}
    	return request.getHeader(name);
    }

    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
    		FilterChain filterChain) throws ServletException, IOException {
    	
    	requestHolder.set(request);
        
		HttpServletRequest defaultRequest = new DefaultRequest(request);
		
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
        
			if (multipartResolver != null && defaultRequest instanceof MultipartHttpServletRequest) {
				multipartResolver.cleanupMultipart((MultipartHttpServletRequest) defaultRequest);
			}
			
	        requestHolder.set(null);
    	}
    }
    
	protected void initFilterBean() throws ServletException {
		if (multipartResolver == null) {
			try {
				ClassUtils.getClass("org.apache.commons.fileupload.FileItemFactory");
				RequestMultipartResolver requestMultipartResolver = new RequestMultipartResolver();
				Object maxInMemorySize = ResourceUtils.getProperty("app.maxInMemorySize");
				if (maxInMemorySize != null) {
					requestMultipartResolver.setMaxInMemorySize((Integer) maxInMemorySize);
				}
				Object maxUploadSize = ResourceUtils.getProperty("app.maxUploadSize");
				if (maxUploadSize != null) {
					requestMultipartResolver.setMaxUploadSize((Integer) maxUploadSize);
				}
				multipartResolver = requestMultipartResolver;
			} catch (ClassNotFoundException e) {
			}
		}
	}
	
	public static HttpServletRequest getRequest() {
    	return requestHolder.get();
    }
	
}