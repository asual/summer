package com.asual.summer.core.view;

import java.io.PrintWriter;
import java.util.Map;

import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Named
public class StringView extends AbstractResponseView{
	
	private static final String DEFAULT_CONTENT_TYPE = "*/*";
	private static final String DEFAULT_EXTENSION = "string";

	public StringView() {
		super();
		setContentType(DEFAULT_CONTENT_TYPE);
		setExtension(DEFAULT_EXTENSION);
	}

	@Override
	protected void renderMergedOutputModel(Map<String, Object> model,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		PrintWriter writer = new PrintWriter(response.getOutputStream());
		writer.write(filterModel(model).toString());
	}

}
