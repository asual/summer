package com.asual.summer.core.spring;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import com.asual.summer.core.DefaultMultipartRequest;

public class RequestMultipartResolver extends CommonsMultipartResolver {
	
	public MultipartHttpServletRequest resolveMultipart(final HttpServletRequest request) throws MultipartException {
		MultipartParsingResult parsingResult = parseRequest(request);
		return new DefaultMultipartRequest(
				request, parsingResult.getMultipartFiles(), parsingResult.getMultipartParameters());
	}
}
