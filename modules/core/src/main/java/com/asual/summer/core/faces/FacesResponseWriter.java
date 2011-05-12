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

import java.io.IOException;
import java.io.Writer;

import javax.faces.FacesException;
import javax.faces.component.UIComponent;
import javax.faces.context.ResponseWriter;

import com.sun.faces.util.HtmlUtils;

/**
 * 
 * @author Rostislav Hristov
 *
 */
public class FacesResponseWriter extends ResponseWriter {

    private String contentType;
    private String encoding = null;
    private Writer writer = null;

    private boolean closeStart;
    private boolean escapeUnicode = true;
    private boolean escapeIso = true;
    private boolean isPartial;
    private boolean scriptInAttributes;    
    
    private char[] buffer = new char[1028];
    private char[] textBuffer = new char[128];
    private char[] charHolder = new char[1];
    
    public FacesResponseWriter(Writer writer, String contentType, String encoding,
    		boolean scriptInAttributes, boolean isPartial) throws FacesException {

        this.writer = writer;
        this.contentType = contentType;
        this.encoding = encoding;
        this.isPartial = isPartial;
        this.scriptInAttributes = scriptInAttributes;
    }

    public String getContentType() {
        return contentType;
    }
    
    public ResponseWriter cloneWithWriter(Writer writer) {
        try {
            return new FacesResponseWriter(writer, getContentType(), 
            		getCharacterEncoding(), scriptInAttributes, isPartial);
        } catch (FacesException e) {
            throw new IllegalStateException();
        }
    }
    
    public void close() throws IOException {
        closeStartIfNecessary();
        writer.close();
    }
    
    public void flush() throws IOException {
        closeStartIfNecessary();
    }
    
    public void startDocument() throws IOException {
    }
    
    public void endDocument() throws IOException {
        writer.flush();
    }
    
    public void startElement(String name, UIComponent componentForElement) throws IOException {
        closeStartIfNecessary();
        writer.write('<');
        writer.write(name);
        closeStart = true;
    }
    
    public void endElement(String name) throws IOException {
        if (closeStart) {
            boolean isEmptyElement = HtmlUtils.isEmptyElement(name);
            if (isEmptyElement) {
                writer.write(" />");
                closeStart = false;
                return;
            }
            writer.write('>');
            closeStart = false;
        }

        writer.write("</");
        writer.write(name);
        writer.write('>');
    }
    
    public String getCharacterEncoding() {
        return encoding;
    }
    
    public void write(char[] cbuf) throws IOException {
        closeStartIfNecessary();
        writer.write(cbuf);
    }
    
    public void write(int c) throws IOException {
        closeStartIfNecessary();
        writer.write(c);
    }
    
    public void write(String str) throws IOException {
        closeStartIfNecessary();
        writer.write(str);
    }
    
    public void write(char[] cbuf, int off, int len) throws IOException {
        closeStartIfNecessary();
        writer.write(cbuf, off, len);
    }
    
    public void write(String str, int off, int len) throws IOException {
        closeStartIfNecessary();
        writer.write(str, off, len);
    }
    
    public void writeAttribute(String name, Object value, String componentPropertyName) throws IOException {
    	
        if (value == null) {
            return;
        }
        
        if (value instanceof Boolean) {
            if (Boolean.TRUE.equals(value)) {
            	writer.write(' ');
            	writer.write(name);
            }
        } else {
        	writer.write(' ');
        	writer.write(name);
        	writer.write('=');
            writer.write('"');
            String val = value.toString();
            ensureTextBufferCapacity(val);
            HtmlUtils.writeAttribute(writer, escapeUnicode, escapeIso, buffer,
            		val, textBuffer, scriptInAttributes);
            writer.write('"');
        }
    }
    
    public void writeURIAttribute(String name, Object value, String componentPropertyName)
    		throws IOException {
    	writeAttribute(name, value, componentPropertyName);
    }
    
    public void writeComment(Object comment) throws IOException {
        closeStartIfNecessary();
        writer.write("<!--");
        writer.write(comment.toString());
        writer.write("-->");
    }
    
    public void writeText(char text) throws IOException {
        closeStartIfNecessary();
        charHolder[0] = text;
        HtmlUtils.writeText(writer, escapeUnicode, escapeIso, buffer, charHolder);
    }
    
    public void writeText(char text[]) throws IOException {
        closeStartIfNecessary();
        HtmlUtils.writeText(writer, escapeUnicode, escapeIso, buffer, text);
    }
    
    public void writeText(Object text, String componentPropertyName) throws IOException {    	
        closeStartIfNecessary();
        String textStr = text.toString();
        ensureTextBufferCapacity(textStr);
        HtmlUtils.writeText(writer, escapeUnicode, escapeIso, buffer, textStr, textBuffer);
    }
    
    public void writeText(char text[], int off, int len) throws IOException {
        closeStartIfNecessary();
        HtmlUtils.writeText(writer, escapeUnicode, escapeIso, buffer, text, off, len);
    }
    
    private void ensureTextBufferCapacity(String source) {
        int len = source.length();
        if (textBuffer.length < len) {
            textBuffer = new char[len * 2];
        }
    }
    
    private void closeStartIfNecessary() throws IOException {
        if (closeStart) {
            writer.write('>');
            closeStart = false;
        }
    }
	
}