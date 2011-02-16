/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *	  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.asual.summer.xml;

import java.util.Map;

import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.JsonGenerator.Feature;
import org.codehaus.jackson.map.SerializationConfig;

import com.asual.summer.core.util.StringUtils;
import com.asual.summer.core.view.AbstractResponseView;
import com.fasterxml.jackson.xml.XmlMapper;
import com.fasterxml.jackson.xml.ser.ToXmlGenerator;

/**
 * 
 * @author Rostislav Hristov
 *
 */
@Named
public class XmlView extends AbstractResponseView {

	private static final String DEFAULT_CONTENT_TYPE = "application/xml";
	private static final String DEFAULT_EXTENSION = "xml";

	public XmlView() {
		super();
		setContentType(DEFAULT_CONTENT_TYPE);
		setExtension(DEFAULT_EXTENSION);
	}
	
	protected void renderMergedOutputModel(Map<String, Object> model,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		response.setContentType(getContentType());
		response.setCharacterEncoding(StringUtils.getEncoding());
		
		XmlMapper mapper = new XmlMapper();
		mapper.configure(Feature.AUTO_CLOSE_TARGET, false);
		mapper.configure(SerializationConfig.Feature.INDENT_OUTPUT, true);
		mapper.configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, true);
		mapper.writeValue(response.getOutputStream(), filterModel(model));
	}

}