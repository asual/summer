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

package com.asual.summer.core.spring;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileUpload;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import com.asual.summer.core.DefaultMultipartRequest;

/**
 * 
 * @author Rostislav Hristov
 *
 */
public class RequestMultipartResolver extends CommonsMultipartResolver {
	
	public MultipartHttpServletRequest resolveMultipart(final HttpServletRequest request) throws MultipartException {
		MultipartParsingResult parsingResult = parseRequest(request);
		return new DefaultMultipartRequest(
				request, parsingResult.getMultipartFiles(), parsingResult.getMultipartParameters());
	}
	
	protected FileUpload prepareFileUpload(String encoding) {
		FileUpload fileUpload = getFileUpload();
		FileUpload actualFileUpload = fileUpload;
		if (encoding != null && !encoding.equals(fileUpload.getHeaderEncoding())) {
			actualFileUpload = newFileUpload(new StreamFileItemFactory(fileUpload.getSizeMax()));
			actualFileUpload.setSizeMax(fileUpload.getSizeMax());
			actualFileUpload.setHeaderEncoding(encoding);
		}
		return actualFileUpload;
	}	
}