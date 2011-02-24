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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

import javax.faces.FacesException;
import javax.faces.context.ResponseWriter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sun.faces.config.WebConfiguration;
import com.sun.faces.config.WebConfiguration.DisableUnicodeEscaping;
import com.sun.faces.renderkit.html_basic.HtmlResponseWriter;

/**
 * 
 * @author Rostislav Hristov
 *
 */
public class OutPrintResponseWriter extends HtmlResponseWriter {
	
	private final Log logger = LogFactory.getLog(getClass());
	private Writer writer;
	private DisableUnicodeEscaping disableUnicodeEscaping;
	private boolean isScriptHidingEnabled;
	private boolean isScriptInAttributeValueEnabled;
	private boolean isPartial;
	
	public OutPrintResponseWriter(Writer writer, String contentType, String encoding) 
			throws FacesException {
		this(writer, contentType, encoding, null, null, null, false);
	}

	public OutPrintResponseWriter(Writer writer, String contentType, String encoding,
			Boolean isScriptHidingEnabled, Boolean isScriptInAttributeValueEnabled,
			WebConfiguration.DisableUnicodeEscaping disableUnicodeEscaping, boolean isPartial)
			throws FacesException {
		super(writer, contentType, encoding, isScriptHidingEnabled, 
				isScriptInAttributeValueEnabled, disableUnicodeEscaping, isPartial);
		this.isScriptHidingEnabled = isScriptHidingEnabled;
		this.isScriptInAttributeValueEnabled = isScriptInAttributeValueEnabled;
		this.disableUnicodeEscaping = disableUnicodeEscaping;
		this.isPartial = isPartial;
		this.writer = writer;
	}

	public ResponseWriter cloneWithWriter(Writer writer) {
		try {
			return new OutPrintResponseWriter(new OutPrintWriter(writer, new ByteArrayOutputStream(), getCharacterEncoding()), 
					getContentType(), getCharacterEncoding(), isScriptHidingEnabled, isScriptInAttributeValueEnabled, 
					disableUnicodeEscaping, isPartial);
		} catch (Exception e) {
			throw new IllegalStateException();
		}
	}

	public void close() throws IOException {
		try {
			if (writer instanceof OutPrintWriter) {
				OutPrintWriter ppw = (OutPrintWriter) writer;
				String content = ((ByteArrayOutputStream) ppw.getOutputStream())
					.toString(getCharacterEncoding());
				ppw.getWriter().write(new OutPrintFormatter(content).toString());
				ppw.getWriter().close();
			} else {
				super.close();
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		writer.close();
		super.close();
	}   

	private class OutPrintWriter extends OutputStreamWriter {

		private Writer writer;
		private OutputStream out;
		
		public OutPrintWriter(Writer writer, OutputStream out, String charsetName) throws UnsupportedEncodingException {
			super(out, charsetName);
			this.writer = writer;
			this.out = out;
		}
		
		public Writer getWriter() {
			return writer;
		}
		
		public OutputStream getOutputStream() {
			return out;
		}
		
	}
}