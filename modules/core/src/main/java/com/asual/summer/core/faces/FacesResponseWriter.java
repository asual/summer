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
import java.io.StringWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.faces.FacesException;
import javax.faces.component.UIComponent;
import javax.faces.context.ResponseWriter;

import org.springframework.util.StringUtils;

import com.sun.faces.util.HtmlUtils;

/**
 * 
 * @author Rostislav Hristov
 *
 */
public class FacesResponseWriter extends ResponseWriter {

    private String contentType;
    private String encoding;
	private String previous;
    private Writer writer;

    private boolean closeStart;
    private boolean escapeUnicode = true;
    private boolean escapeIso = true;
    private boolean isPartial;
    private boolean scriptInAttributes;
	private boolean start;
    
    private char[] buffer = new char[1028];
    private char[] textBuffer = new char[128];
    private char[] charHolder = new char[1];
    private StringWriter textWriter = new StringWriter();
	
	private int depth = 0;
	private String[] nodeDepth = new String[100];
	private boolean[] nodeDepthContent = new boolean[100];
	
	private String INDENT = "    ";
	
	private List<String> block = Arrays.asList(
		"html", "head", "meta", "link", "title", "script", "style", "body", 
		"header", "footer", "section", "div", "h1", "h2", "h3", "h4", "h5", "h6", 
		"p", "dl", "dt", "dd", "ol", "ul", "li", "hr", "form", "fieldset", "legend", 
		"label", "input", "select", "option", "textarea", "button"
	);
	
	private List<String> pre = Arrays.asList(
		"pre", "textarea"
	);
	
    public FacesResponseWriter(Writer writer, String contentType, String encoding,
    		boolean scriptInAttributes, boolean isPartial) throws FacesException {

        this.writer = writer;
        this.contentType = contentType;
        this.encoding = encoding;
        this.isPartial = isPartial;
        this.scriptInAttributes = scriptInAttributes;
        
        String charsetName = encoding.toUpperCase();
        escapeUnicode = !HtmlUtils.isUTFencoding(charsetName);
        escapeIso = !HtmlUtils.isISO8859_1encoding(charsetName) && !HtmlUtils.isUTFencoding(charsetName);
    }
    
    public String getContentType() {
        return contentType;
    }
    
    public ResponseWriter cloneWithWriter(Writer writer) {
        try {
            return new FacesResponseWriter(writer, getContentType(), getCharacterEncoding(), 
            		scriptInAttributes, isPartial);
        } catch (FacesException e) {
            throw new IllegalStateException();
        }
    }
    
    public String getCharacterEncoding() {
        return encoding;
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
        
        boolean indent = textWriter.toString().isEmpty();
        
    	printTextIfNecessary(false);
		
    	if (block.contains(name)|| (indent && previous != null && block.contains(previous))) {
    		if (!"html".equals(name)) {
    			writer.write('\n');
    		}
    		writer.write(getIndent(depth));
    	}
    	writer.write('<');
        writer.write(name);
        closeStart = true;
        previous = name;
        
		depth++;
		nodeDepth[depth] = name;
		nodeDepthContent[depth] = false;
    }
    
    public void endElement(String name) throws IOException {
    	
    	boolean emptyNode = HtmlUtils.isEmptyElement(name);
    	
		start = start || closeStart;
		
        if (emptyNode) {
        	
            if (closeStart) {
	            writer.write('>');
	            closeStart = false;
            }
            
        } else {
    	
	        if (closeStart) {
	        	writer.write('>');
	            closeStart = false;
	        }
	        
	    	printTextIfNecessary(true);
	    	
			if (block.contains(name) && !pre.contains(name) &&
				(!ComponentUtils.isStyleOrScript(name) || nodeDepthContent[depth])) {
				writer.write('\n');
				writer.write(getIndent(depth - 1));
			}
	        writer.write("</" + name + ">");
	        previous = name;
        }
        
		nodeDepth[depth + 1] = null;
        depth--;
    }
    
    private void printTextIfNecessary(boolean end) throws IOException {
        String textStr = textWriter.toString();
        if (StringUtils.hasText(textStr)) {
            String name = nodeDepth[depth];
            textStr = textStr.replaceAll("\t", INDENT);
			if (ComponentUtils.isStyleOrScript(name)) {
				textStr = Pattern.compile("^\\s{" + calculateIndent(textStr) + "}", 
						Pattern.MULTILINE).matcher(textStr).replaceAll(getIndent(depth)).trim();
			}
			if (block.contains(name) && !pre.contains(name)) {
				Matcher m = Pattern.compile("^( *\\n)+.*", Pattern.DOTALL).matcher(textStr);
				if (m.matches()) {
					textStr = m.replaceFirst("$1") + getIndent(depth) + StringUtils.trimLeadingWhitespace(textStr);
				} else if (start) {
					textStr = "\n" + getIndent(depth) + StringUtils.trimLeadingWhitespace(textStr);
				}
				m = Pattern.compile(".*(\\n)+ *$", Pattern.DOTALL).matcher(textStr);
				if (m.matches()) {
					textStr = StringUtils.trimTrailingWhitespace(textStr) + m.replaceFirst("$1") + getIndent(depth);
				}
			}
			if (end) {
				textStr = StringUtils.trimTrailingWhitespace(textStr);
			}
			writer.write(textStr);
	        textWriter.getBuffer().setLength(0);
	        start = false;
	        
	        nodeDepth[depth + 1] = null;
	        for (int i = 0; i < depth + 1; i++) {
	        	nodeDepthContent[i] = true;
	        }
        }
    }
    
    private void closeStartIfNecessary() throws IOException {
    	start = start || closeStart;
        if (closeStart) {
        	writer.write('>');
            closeStart = false;
        }
    }
    
    public void writeAttribute(String name, Object value, String componentPropertyName) throws IOException {
        if (value instanceof Boolean) {
            if (Boolean.TRUE.equals(value)) {
            	writer.write(' ');
            	writer.write(name);
            }
        } else if (value != null) {
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
    
    public void write(char[] cbuf) throws IOException {
        closeStartIfNecessary();
        writer.write(cbuf);
    }
    
    public void write(int c) throws IOException {
        closeStartIfNecessary();
        writer.write(c);
    }
    
    public void write(char[] cbuf, int off, int len) throws IOException {
        closeStartIfNecessary();
        writer.write(cbuf, off, len);
    }
    
    public void write(String str) throws IOException {
        ensureTextBufferCapacity(str);
        textWriter.write(str);
        closeStartIfNecessary();
    }
    
    public void write(String str, int off, int len) throws IOException {
        ensureTextBufferCapacity(len);
        textWriter.write(str, off, len);
        closeStartIfNecessary();
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
        if(!textWriter.toString().equals("<!DOCTYPE html>\n")){
	    	String textStr = text.toString();
	        ensureTextBufferCapacity(textStr);
	        HtmlUtils.writeText(textWriter, escapeUnicode, escapeIso, buffer, textStr, textBuffer);
	        closeStartIfNecessary();
        }
    }
    
    public void writeText(char text[], int off, int len) throws IOException {
        closeStartIfNecessary();
        HtmlUtils.writeText(writer, escapeUnicode, escapeIso, buffer, text, off, len);
    }
    
    public void writeComment(Object comment) throws IOException {
        closeStartIfNecessary();
        writer.write("<!--");
        writer.write(comment.toString());
        writer.write("-->");
    }
    
    private int calculateIndent(String textStr) {
		int indent = 1000;
		String[] lines = textStr.split("\n|\r\n");
		for (String line : lines) {
			if (StringUtils.hasText(line)) {
				indent = Math.min(indent, line.length() - StringUtils.trimLeadingWhitespace(line).length());
			}
		}
		return indent;
    }
	
    private String getIndent(int depth) throws IOException {
    	StringBuilder sb = new StringBuilder();
    	for (int i = 0; i < depth; i++) {
    		sb.append(INDENT);
    	}
    	return sb.toString();
    }
    
    private void ensureTextBufferCapacity(int len) {
        if (textBuffer.length < len) {
            textBuffer = new char[len * 2];
        }
    }
    
    private void ensureTextBufferCapacity(String source) {
    	ensureTextBufferCapacity(source.length());
    }
    
}