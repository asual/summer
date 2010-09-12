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

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.view.AbstractView;

import com.asual.summer.core.util.MiscUtils;
import com.asual.summer.core.util.StringUtils;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.mapper.Mapper;
import com.thoughtworks.xstream.mapper.MapperWrapper;

@Component("xml")
public class XMLView extends AbstractView implements AbstractResponseView {

    private static final String DEFAULT_CONTENT_TYPE = "application/xml";
    private static final String DEFAULT_EXTENSION = "xml";

    private String extension;

    public XMLView() {
        super();
        setContentType(DEFAULT_CONTENT_TYPE);
        setExtension(DEFAULT_EXTENSION);
    }
    
	public String getExtension() {
		return extension;
	}

	public void setExtension(String extension) {
		this.extension = extension;
	}
	
    @SuppressWarnings("rawtypes")
    protected void renderMergedOutputModel(Map model,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        
    	XStream xstream = new XStream() {
		    protected MapperWrapper wrapMapper(MapperWrapper next) {
		        return new PackageStrippingMapper(next);
		    }
		};
		
		byte[] bytes = xstream.toXML(model).getBytes(MiscUtils.getEncoding());
		
        response.setContentType(getContentType());
        response.setCharacterEncoding(MiscUtils.getEncoding());
		response.setContentLength(bytes.length);
		response.getOutputStream().write(bytes);
		
	} 
    
    private static class PackageStrippingMapper extends MapperWrapper {
    	
        public PackageStrippingMapper(Mapper wrapped) {
            super(wrapped);
        }
        
        @SuppressWarnings("rawtypes")
        public String serializedClass(Class type) {
        	return StringUtils.toCamelCase(type.getSimpleName());
        }
    }

}