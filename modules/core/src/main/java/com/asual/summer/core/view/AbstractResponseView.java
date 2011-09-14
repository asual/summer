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

package com.asual.summer.core.view;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.view.AbstractView;
import org.springframework.web.util.UrlPathHelper;
import org.springframework.web.util.WebUtils;

/**
 * 
 * @author Rostislav Hristov
 *
 */
public abstract class AbstractResponseView extends AbstractView implements ResponseView {

	private static final UrlPathHelper urlPathHelper = new UrlPathHelper();
	private String extension;
	
	public String getExtension() {
		return extension;
	}
	
	public void setExtension(String extension) {
		this.extension = extension;
	}
	
	protected Object filterModel(Map<String, Object> model) {
		Map<String, Object> result = new HashMap<String, Object>(model.size());
		for (Map.Entry<String, Object> entry : model.entrySet()) {
			if (!(entry.getValue() instanceof BindingResult)) {
				result.put(entry.getKey(), entry.getValue());
			}
		}
		return result;
	}
	
	protected String getFilename(HttpServletRequest request) {
		String requestUri = urlPathHelper.getLookupPathForRequest(request);
		String filename = WebUtils.extractFullFilenameFromUrlPath(requestUri);
		String extension = StringUtils.getFilenameExtension(filename);
		return filename.replaceFirst("\\." + extension + "$", "") + "." + getExtension();
	}
	
}
