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

package com.asual.summer.core.faces;

import static com.sun.faces.config.WebConfiguration.BooleanWebContextInitParameter.EnableScriptInAttributeValue;

import java.io.Writer;

import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import com.asual.summer.core.util.ResourceUtils;
import com.sun.faces.RIConstants;
import com.sun.faces.config.WebConfiguration;
import com.sun.faces.renderkit.RenderKitImpl;

/**
 * 
 * @author Rostislav Hristov
 *
 */
public class FacesRenderKitImpl extends RenderKitImpl {
	
	private WebConfiguration webConfig;

	public FacesRenderKitImpl() {
		FacesContext context = FacesContext.getCurrentInstance();
		webConfig = WebConfiguration.getInstance(context.getExternalContext());
	}
	
	public ResponseWriter createResponseWriter(Writer writer,
			String desiredContentTypeList,
			String characterEncoding) {
		
		if (writer == null) {
			return null;
		}
		
		return new FacesResponseWriter(writer, RIConstants.HTML_CONTENT_TYPE, 
				characterEncoding != null ? characterEncoding : (String) ResourceUtils.getProperty("app.encoding"), 
						webConfig.isOptionEnabled(EnableScriptInAttributeValue), 
						FacesContext.getCurrentInstance().getPartialViewContext().isPartialRequest());
	}
	
}
