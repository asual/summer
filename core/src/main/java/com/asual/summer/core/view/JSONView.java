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

package com.asual.summer.core.view;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.JsonGenerator.Feature;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.view.AbstractView;

@Component("json")
public class JSONView extends AbstractView implements AbstractResponseView {

    private static final String DEFAULT_CONTENT_TYPE = "application/json;charset=UTF-8";
    private static final String DEFAULT_EXTENSION = "json";

    private String extension;

    public JSONView() {
        super();
        setContentType(DEFAULT_CONTENT_TYPE);
        setExtension(DEFAULT_EXTENSION);
    }
	
    @SuppressWarnings("rawtypes")
    protected void renderMergedOutputModel(Map model,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        
        response.setContentType(getContentType());
        
        String callback = (String) request.getParameter("callback");
        if (callback != null) {
            response.getOutputStream().print(callback + " && " + callback + "(");
        }
        
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(Feature.AUTO_CLOSE_TARGET, false);
        mapper.writeValue(response.getOutputStream(), model);
        
        if (callback != null) {
            response.getOutputStream().print(");");
        }
        
    }

	public String getExtension() {
		return extension;
	}

	public void setExtension(String extension) {
		this.extension = extension;
	}

}