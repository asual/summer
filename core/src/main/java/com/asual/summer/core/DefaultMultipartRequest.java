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

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.support.DefaultMultipartHttpServletRequest;

import com.asual.summer.core.util.StringUtils;

/**
 * 
 * @author Rostislav Hristov
 *
 */
public class DefaultMultipartRequest extends DefaultMultipartHttpServletRequest {
	
	public DefaultMultipartRequest(HttpServletRequest request, MultiValueMap<String, MultipartFile> mpFiles, Map<String, String[]> mpParams) {
		super(request, mpFiles, mpParams);
		Map<String, String[]> multipartParameters = super.getMultipartParameters();
		for (String key : multipartParameters.keySet()) {
			for (int i = 0; i < multipartParameters.get(key).length; i++) {
				multipartParameters.get(key)[i] = RequestFilter.encode(multipartParameters.get(key)[i]);
			}
		}
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
    
    public String getRequestURI() {
        return RequestFilter.getRequestURI(getRequest());
    }

    public String getServletPath() {
        return RequestFilter.getServletPath(getRequest());
    }
    
    public String getHeader(String name) {
        return RequestFilter.getHeader(getRequest(), name);
    }
    
    public String getMethod() {
    	return RequestFilter.getMethod(getRequest(), super.getMethod(), getParameterMap());
    }
    
	protected Map<String, String[]> getMultipartParameters() {
		Map<String, String[]> multipartParameters = super.getMultipartParameters();
		return multipartParameters;
	}
	
}