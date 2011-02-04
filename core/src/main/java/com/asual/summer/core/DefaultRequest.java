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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import com.asual.summer.core.util.StringUtils;

/**
 * 
 * @author Rostislav Hristov
 *
 */
public class DefaultRequest extends HttpServletRequestWrapper {
	
    public DefaultRequest(HttpServletRequest request) {
        super(request);
    }
    
    public String getCharacterEncoding() {
    	return StringUtils.getEncoding();
    }

    public HttpServletRequest getRequest() {
    	return (HttpServletRequest) super.getRequest();
    }

    public String getRemoteAddr() {
    	return RequestFilter.getRemoteAddr(getRequest());
    }
    
    public String getRemoteHost() {
    	return RequestFilter.getRemoteHost(getRequest());
    }
    
    public String getServerName() {
    	return RequestFilter.getServerName(getRequest());
    }
    
    public String getHeader(String name) {
        return RequestFilter.getHeader(getRequest(), name);
    }
    
    public String getMethod() {
    	return RequestFilter.getMethod(getRequest(), super.getMethod(), getParameterMap());
    }
    
}