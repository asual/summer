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
import java.util.Map;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.NamedThreadLocal;
import org.springframework.web.filter.OncePerRequestFilter;

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
    
	static class Request extends HttpServletRequestWrapper {
        
        public Request(HttpServletRequest request) {
            super(request);
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
        
        public String encode(String input) {
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

        public String getRemoteAddr() {
            String result = super.getHeader("X-Forwarded-For");
            if (result == null) {
                result = super.getRemoteAddr();
            }
            try {
                result = InetAddress.getByName(result).getHostAddress();
            } catch (UnknownHostException e) {
            }
            return result;
        }
        
        public String getRemoteHost() {
            String result = super.getHeader("X-Forwarded-For");
            if (result == null) {
                result = super.getRemoteHost();
            }
            try {
                result = InetAddress.getByName(result).getHostName();
            } catch (UnknownHostException e) {
            }
            return result;
        }
        
        public String getServerName() {
            String result = super.getHeader("X-Forwarded-Host");
            if (result == null) {
                result = super.getServerName();
            }
            try {
                result = InetAddress.getByName(result).getHostName();
            } catch (UnknownHostException e) {
            }
            return result;
        }
        
        public String getParameter(String paramName) {
            return encode(super.getParameter(paramName));
        }
        
        public String[] getParameterValues(String paramName) {
            String values[] = super.getParameterValues(paramName);
            if (values != null) {
                int length = values.length;
                for (int i = 0; i < length; i++) {
                    values[i] = encode(values[i]);
                }
            }
            return values;
        }
        
        public Map<String, String[]> getParameterMap() {
            Map<String, String[]> map = new HashMap<String, String[]>();
            for (Object name : super.getParameterMap().keySet()) {
                map.put((String) name, getParameterValues((String) name));
            }
            return map;
        }
        
        public String getRequestURI() {
            return encode(super.getRequestURI().replaceFirst("/$", ""));
        }

        public String getServletPath() {
            return encode(super.getServletPath());
        }
        
        public String getCharacterEncoding() {
        	return StringUtils.getEncoding();
        }
        
    	// TODO: Remove when https://bugs.webkit.org/show_bug.cgi?id=27267 gets fixed.
        public String getHeader(String name) {
        	if ("Accept".equals(name) && RequestUtils.isWebKit()) {
    			return "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8";
        	}
        	return super.getHeader(name);
        }
    }
    
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
    		FilterChain filterChain) throws ServletException, IOException {        
        
    	long time = System.currentTimeMillis();
    	
    	requestHolder.set(new Request(request));
    	
        if (request.getCharacterEncoding() == null) {
            request.setCharacterEncoding(StringUtils.getEncoding());
        }
        
        if (RequestUtils.isMSIE()) {
        	response.setHeader("X-UA-Compatible", "IE=8");
        }
        
        filterChain.doFilter(requestHolder.get(), response);
        
        logger.debug("The request for '" + request.getRequestURI() + "' took " + (System.currentTimeMillis() - time) + "ms.");
        
        requestHolder.set(null);
    }
    
    public static HttpServletRequest getRequest() {
    	return requestHolder.get();
    }
    
}