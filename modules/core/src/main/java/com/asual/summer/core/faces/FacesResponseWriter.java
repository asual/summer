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
    private String encoding = null;
    private Writer writer = null;

    private boolean closeStart;
    private boolean escapeUnicode = true;
    private boolean escapeIso = true;
    private boolean isPartial;
    private boolean scriptInAttributes;
    private boolean hasChildren;
    
    private char[] buffer = new char[1028];
    private char[] textBuffer = new char[128];
    private char[] charHolder = new char[1];
    private StringWriter textWriter = new StringWriter();
	
	private String name;
	
	private List<String> autoClose = Arrays.asList(
		"meta", "img", "link", "input", "br", "hr"
	);
	
	private List<String> noSpace = Arrays.asList(
		"p", "div", "h1", "h2", "h3", "h4", "h5", "h6", "br", "hr"
	);
	
	private List<String> sameLine = Arrays.asList(
		"p", "li", "em", "strong", "title", "a", "option", "pre", 
		"textarea", "span", "sub", "sup", "small", "label", 
		"button", "h1", "h2", "h3", "h4", "h5", "h6", "legend"
	);
	
	private List<String> sameLineNoContent = Arrays.asList(
		"pre", "script", "textarea"
	);
	
	private List<String> pre = Arrays.asList(
		"pre", "script", "style", "textarea"
	);
	
	private int depth = 0;
	private boolean start = false;
	private boolean end = false;
	
	private String[] nodeDepth = new String[100];
	
	private static String INDENT = "    ";
	
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
    
    public void close() throws IOException {
        closeStartIfNecessary(false);
        writer.close();
    }
    
    public void flush() throws IOException {
        closeStartIfNecessary(false);
    }
    
	private boolean isNoSpace(String nodeName) {
		return noSpace.contains(nodeName);
	}
	
	private boolean isSameLine(String nodeName) {
		return sameLine.contains(nodeName);
	}
	
	private boolean isPre(String nodeName) {
		return pre.contains(nodeName);
	}
	
	private boolean isSameLineNoContent(String nodeName) {
		return sameLineNoContent.contains(nodeName);
	}
	
	private boolean isAutoClose(String nodeName) {
		return autoClose.contains(nodeName);
	}
	
	private boolean isStyleOrScript(String nodeName) {
		return "style".equals(nodeName) || "script".equals(nodeName);
	}
	
    public void startDocument() throws IOException {
    }
    
    public void endDocument() throws IOException {
        writer.flush();
    }
    
    public void startElement(String name, UIComponent componentForElement) throws IOException {
        closeStartIfNecessary(false);
    	printTextIfNecessary(true);
        
		this.name = name;
		
		if (!isSameLine(getParent())) {
			writer.write(getIndent(depth));
		}
        writer.write('<');
        writer.write(name);
        closeStart = true;
        
        nodeDepth[depth] = name;
        depth++;
    }
    
    public void endElement(String name) throws IOException {
    	
    	depth--;
    	
        if (closeStart) {
            boolean isEmptyElement = HtmlUtils.isEmptyElement(name);
            if (isEmptyElement) {
                writer.write(">");
                writer.write('\n');
                closeStart = false;
                return;
            }
            writer.write('>');
            closeStart = false;
        }
        
        if (!isAutoClose(name)) {
        	
	    	printTextIfNecessary(false);
	    	
			if (!isSameLine(name)) {
				if (!isSameLine(getParent())) {
					if (!(isSameLineNoContent(name) && !hasChildren)) {
				        writer.write(getIndent(depth));
					}
			        writer.write("</" + name + ">\n");
				} else {
			        writer.write(getIndent(depth) + "</" + name + ">\n");
				}
			} else if (!isSameLine(getParent())) {
		        writer.write("</" + name + ">\n");
			} else {
		        writer.write("</" + name + ">");
			}
        }
    }
    
    public String getCharacterEncoding() {
        return encoding;
    }
    
    public void write(char[] cbuf) throws IOException {
        closeStartIfNecessary(true);
        writer.write(cbuf);
    }
    
    public void write(int c) throws IOException {
        closeStartIfNecessary(true);
        writer.write(c);
    }
    
    public void write(String str) throws IOException {
        closeStartIfNecessary(true);
        writer.write(str);
    }
    
    public void write(char[] cbuf, int off, int len) throws IOException {
        closeStartIfNecessary(true);
        writer.write(cbuf, off, len);
    }
    
    public void write(String str, int off, int len) throws IOException {
        closeStartIfNecessary(true);
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
        closeStartIfNecessary(true);
        writer.write("<!--");
        writer.write(comment.toString());
        writer.write("-->");
    }
    
    public void writeText(char text) throws IOException {
        closeStartIfNecessary(true);
        charHolder[0] = text;
        HtmlUtils.writeText(writer, escapeUnicode, escapeIso, buffer, charHolder);
    }
    
    public void writeText(char text[]) throws IOException {
        closeStartIfNecessary(true);
        HtmlUtils.writeText(writer, escapeUnicode, escapeIso, buffer, text);
    }
    
    public void writeText(Object text, String componentPropertyName) throws IOException {    	
        closeStartIfNecessary(true);
        String textStr = text.toString();
        if (!isStyleOrScript(name) && textWriter.getBuffer().length() == 0) {
        	//textStr = StringUtils.trimLeadingWhitespace(textStr);
        }
        ensureTextBufferCapacity(textStr);
        HtmlUtils.writeText(textWriter, escapeUnicode, escapeIso, buffer, textStr, textBuffer);
    }
    
    public void writeText(char text[], int off, int len) throws IOException {
        closeStartIfNecessary(true);
        HtmlUtils.writeText(writer, escapeUnicode, escapeIso, buffer, text, off, len);
    }
    
    private void ensureTextBufferCapacity(String source) {
        int len = source.length();
        if (textBuffer.length < len) {
            textBuffer = new char[len * 2];
        }
    }
    
    private void closeStartIfNecessary(boolean hasChildren) throws IOException {
    	this.hasChildren = hasChildren;
        if (closeStart) {
        	writer.write('>');
			if (!isSameLine(name) && !isSameLine(name) && !(isSameLineNoContent(name) && !hasChildren)) {
				writer.write('\n');
			}
            closeStart = false;
        }
    }
    
    private void printTextIfNecessary(boolean start) throws IOException {
    	
        String textStr = textWriter.toString();
        
        if (StringUtils.hasText(textStr)) {
            
            textStr = textStr.replaceAll("\t", INDENT);
                        
			if (isStyleOrScript(name)) {
				
				String[] lines = textStr.split("\n");
				int l = 100;
				
				for (String line : lines) {
					if (StringUtils.hasText(line)) {
						int i = line.length() - StringUtils.trimLeadingWhitespace(line).length();
						if (i < l) l = i;
					}
				}
				
				textStr = Pattern.compile("^\\s{" + l + "}", 
						Pattern.MULTILINE).matcher(textStr).replaceAll(getIndent(depth + 1)).trim();
				
			} else {
				
				if (!isPre(name) && !textStr.startsWith("&lt;!--")) {
					textStr = textStr.replaceAll("\\n+", "").replaceAll("\\s+", " ").replaceAll("^ <", "<");
				}
			}
			
			if (isSameLine(name)) {
		        writer.write(textStr);
			} else {
		        writer.write(getIndent(depth + 1) + textStr + "\n");
			}
			
	        textWriter.getBuffer().setLength(0);
        }    	
    }
	
    private String getIndent(int depth) throws IOException {
    	StringBuilder sb = new StringBuilder();
    	for (int i = 0; i < depth; i++) {
    		sb.append(INDENT);
    	}
    	return sb.toString();
    }
    
    private String getParent() {
    	return (depth != 0 ? nodeDepth[depth - 1] : null);    	
    }
}