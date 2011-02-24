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

package com.asual.summer.outprint;

import static com.sun.faces.config.WebConfiguration.BooleanWebContextInitParameter.EnableJSStyleHiding;
import static com.sun.faces.config.WebConfiguration.BooleanWebContextInitParameter.EnableScriptInAttributeValue;
import static com.sun.faces.config.WebConfiguration.WebContextInitParameter.DisableUnicodeEscaping;

import java.io.Writer;

import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import com.asual.summer.core.util.StringUtils;
import com.sun.faces.RIConstants;
import com.sun.faces.config.WebConfiguration;
import com.sun.faces.renderkit.RenderKitImpl;

/**
 * 
 * @author Rostislav Hristov
 *
 */
public class OutPrintRenderKitImpl extends RenderKitImpl {
	
	private WebConfiguration webConfig;

	public OutPrintRenderKitImpl() {
		FacesContext context = FacesContext.getCurrentInstance();
		webConfig = WebConfiguration.getInstance(context.getExternalContext());
	}
	
	public ResponseWriter createResponseWriter(Writer writer,
			String desiredContentTypeList,
			String characterEncoding) {
		
		if (writer == null) {
			return null;
		}
		
		FacesContext context = FacesContext.getCurrentInstance();
		String contentType = RIConstants.HTML_CONTENT_TYPE;
		
		boolean scriptHiding = webConfig.isOptionEnabled(EnableJSStyleHiding);
		boolean scriptInAttributes = webConfig.isOptionEnabled( EnableScriptInAttributeValue);
		WebConfiguration.DisableUnicodeEscaping escaping = 
			WebConfiguration.DisableUnicodeEscaping.getByValue(webConfig.getOptionValue(DisableUnicodeEscaping));
		boolean isPartial = context.getPartialViewContext().isPartialRequest();
		
		return new OutPrintResponseWriter(writer, contentType, characterEncoding != null ? characterEncoding : StringUtils.getEncoding(), 
				scriptHiding, scriptInAttributes, escaping, isPartial);
	}

}
