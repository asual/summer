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

import java.io.ByteArrayOutputStream;
import java.util.Map;

import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.JsonGenerator.Feature;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import com.asual.summer.core.util.StringUtils;

/**
 * 
 * @author Rostislav Hristov
 *
 */
@Named
public class JSONView extends AbstractResponseView {

    private static final String DEFAULT_CONTENT_TYPE = "application/json";
    private static final String DEFAULT_EXTENSION = "json";

    public JSONView() {
        super();
        setContentType(DEFAULT_CONTENT_TYPE);
        setExtension(DEFAULT_EXTENSION);
    }
	
    protected void renderMergedOutputModel(Map<String, Object> model,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {
       
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();

        String callback = (String) request.getParameter("callback");
        if (callback != null) {
        	byteStream.write((callback + " && " + callback + "(").getBytes(StringUtils.getEncoding()));
        }
        
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(Feature.AUTO_CLOSE_TARGET, false);
        mapper.configure(SerializationConfig.Feature.FAIL_ON_EMPTY_BEANS, false);
        mapper.configure(SerializationConfig.Feature.INDENT_OUTPUT, true);
        mapper.getSerializationConfig().setSerializationInclusion(JsonSerialize.Inclusion.NON_NULL);
        mapper.writeValue(byteStream, filterModel(model));

        if (callback != null) {
        	byteStream.write(");".getBytes(StringUtils.getEncoding()));
        }

        byte[] bytes = byteStream.toByteArray();
        
        byteStream.close();

        response.setContentLength(bytes.length);
        response.setContentType(getContentType());
        response.setCharacterEncoding(StringUtils.getEncoding());
        response.getOutputStream().write(bytes);
    }

}