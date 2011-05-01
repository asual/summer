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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PushbackInputStream;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xerces.xni.QName;
import org.apache.xerces.xni.XMLAttributes;
import org.apache.xerces.xni.parser.XMLDocumentFilter;
import org.cyberneko.html.HTMLElements;
import org.cyberneko.html.HTMLEntities;
import org.cyberneko.html.filters.Writer;
import org.cyberneko.html.parsers.DOMParser;
import org.xml.sax.InputSource;

/**
 * 
 * @author Rostislav Hristov
 *
 */
public class FacesResourceProcessor {
	
	private static final Log logger = LogFactory.getLog(FacesResourceProcessor.class);

	public static byte[] execute(URL url, InputStream input, String encoding) throws IOException {
		
		byte[] bytes;
		
		try {
			
			StringBuilder sb = new StringBuilder();
			UnicodeReader reader = new UnicodeReader(input, encoding);
			
			try {
				
				char[] cbuf = new char[32];
				int r;
				while ((r = reader.read(cbuf, 0, 32)) != -1) {
					sb.append(cbuf, 0, r);
				}
				
				String str = sb.toString();
				
				if (!str.contains("ui:component")) {
					
					try {
					    
						String fileEncoding = reader.getEncoding();
						InputSource is = new InputSource(new StringReader(str));
						is.setEncoding(fileEncoding);

						final ByteArrayOutputStream baos = new ByteArrayOutputStream();
						
						XMLDocumentFilter[] filters = { new Writer(baos, fileEncoding) {
							
						    protected void printStartElement(QName element, XMLAttributes attributes) {
						        fPrinter.print('<');
						        fPrinter.print(element.rawname);
						        int attrCount = attributes != null ? attributes.getLength() : 0;
						        for (int i = 0; i < attrCount; i++) {
						            String aname = attributes.getQName(i);
						            String avalue = attributes.getValue(i);
						            fPrinter.print(' ');
						            fPrinter.print(aname);
						            fPrinter.print("=\"");
						            printAttributeValue(avalue);
						            fPrinter.print('"');
						        }
						        if (HTMLElements.getElement(element.rawname).isEmpty()) {
							        fPrinter.print(' ');						        	
							        fPrinter.print('/');
						        }
						        fPrinter.print('>');
						        fPrinter.flush();
						    }
						    
						    protected void printAttributeValue(String text) {
						        fPrinter.print(StringEscapeUtils.escapeHtml(text));
						        fPrinter.flush();
						    }
						    
						    protected void printEntity(String name) {
						        fPrinter.print('&');
						        fPrinter.print('#');
						        fPrinter.print(HTMLEntities.get(name));
						        fPrinter.print(';');
						        fPrinter.flush();
						    }
						    
						}};
						
				        DOMParser parser = new DOMParser();
						parser.setFeature("http://cyberneko.org/html/features/balance-tags", false);
						parser.setFeature("http://cyberneko.org/html/features/scanner/notify-builtin-refs", true);
						parser.setProperty("http://cyberneko.org/html/properties/default-encoding", fileEncoding);
						parser.setProperty("http://cyberneko.org/html/properties/names/elems", "lower");
						parser.setProperty("http://cyberneko.org/html/properties/filters", filters);
				        parser.parse(is);
				        
				        str = "<!DOCTYPE html>" + baos.toString(fileEncoding);
						
					} catch (Exception e) {
						logger.error(e.getMessage(), e);
					}					
					
					if (url.getFile().contains("META-INF/templates")) {
						str = "<ui:component xmlns:ui=\"http://java.sun.com/jsf/facelets\">" + 
							Pattern.compile("(<\\!DOCTYPE html>)|(</?html[^>]*>)|(<title>[^<]*</title>)", 
								Pattern.CASE_INSENSITIVE).matcher(str).replaceAll("").replaceAll(
										"\\$\\{template\\.body\\}", "<ui:insert />") + "</ui:component>";
					}
					
				}
				
				bytes = str.getBytes(reader.getEncoding());
				
			} finally {
				reader.close();
			}
		} catch (IOException e) {
			throw e;
		}
		
		return bytes;
	}
	
	/**
	 * Generic unicode textreader, which will use BOM mark
	 * to identify the encoding to be used. If BOM is not found
	 * then use a given default or system encoding.
	 * 
	 * @author Thomas Weidenfeller
	 * @author Aki Nieminen
	 */
	private static class UnicodeReader extends Reader {
		
		PushbackInputStream internalIn;
		InputStreamReader internalIn2 = null;
		String defaultEnc;

		private static final int BOM_SIZE = 4;

		/**
		 * @param in inputstream to be read
		 * @param defaultEnc default encoding if stream does not have 
		 *				   BOM marker. Give NULL to use system-level default.
		 */
		public UnicodeReader(InputStream in, String defaultEnc) {
			internalIn = new PushbackInputStream(in, BOM_SIZE);
			this.defaultEnc = defaultEnc;
		}
		
		/**
		 * Get stream encoding or NULL if stream is uninitialized.
		 * Call init() or read() method to initialize it.
		 */
		public String getEncoding() {
			if (internalIn2 == null) return null;
			return internalIn2.getEncoding();
		}

		/**
		 * Read-ahead four bytes and check for BOM marks. Extra bytes are
		 * unread back to the stream, only BOM bytes are skipped.
		 */
		protected void init() throws IOException {
			
			if (internalIn2 != null) return;

			String encoding;
			byte bom[] = new byte[BOM_SIZE];
			int n, unread;
			n = internalIn.read(bom, 0, bom.length);

			if ( (bom[0] == (byte)0x00) && (bom[1] == (byte)0x00) &&
					(bom[2] == (byte)0xFE) && (bom[3] == (byte)0xFF) ) {
				encoding = "UTF-32BE";
				unread = n - 4;
			} else if ( (bom[0] == (byte)0xFF) && (bom[1] == (byte)0xFE) &&
					(bom[2] == (byte)0x00) && (bom[3] == (byte)0x00) ) {
				encoding = "UTF-32LE";
				unread = n - 4;
			} else if (  (bom[0] == (byte)0xEF) && (bom[1] == (byte)0xBB) &&
					(bom[2] == (byte)0xBF) ) {
				encoding = "UTF-8";
				unread = n - 3;
			} else if ( (bom[0] == (byte)0xFE) && (bom[1] == (byte)0xFF) ) {
				encoding = "UTF-16BE";
				unread = n - 2;
			} else if ( (bom[0] == (byte)0xFF) && (bom[1] == (byte)0xFE) ) {
				encoding = "UTF-16LE";
				unread = n - 2;
			} else {
				// Unicode BOM mark not found, unread all bytes
				encoding = defaultEnc;
				unread = n;
			}

			if (unread > 0) internalIn.unread(bom, (n - unread), unread);

			// Use given encoding
			if (encoding == null) {
				internalIn2 = new InputStreamReader(internalIn);
			} else {
				internalIn2 = new InputStreamReader(internalIn, encoding);
			}
		}

		public void close() throws IOException {
			init();
			internalIn2.close();
		}

		public int read(char[] cbuf, int off, int len) throws IOException {
			init();
			return internalIn2.read(cbuf, off, len);
		}

	}
	
}