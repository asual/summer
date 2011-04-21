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

import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.html.dom.HTMLDocumentImpl;
import org.cyberneko.html.parsers.DOMFragmentParser;
import org.springframework.util.StringUtils;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.html.HTMLDocument;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.asual.summer.core.util.ResourceUtils;

/**
 * 
 * @author Rostislav Hristov
 *
 */
public class OutPrintFormatter {

	private StringBuilder sb;
	private static String INDENT = "	";
	
	private List<String> autoClose = Arrays.asList(
			new String[] {
					"meta", 
					"img", 
					"link", 
					"input", 
					"br",
					"hr"
			});
	
	private List<String> noSpace = Arrays.asList(
			new String[] {
					"p",
					"div",
					"h1", 
					"h2", 
					"h3", 
					"h4", 
					"h5", 
					"h6",
					"br",
					"hr"
			});
	
	private List<String> sameLine = Arrays.asList(
			new String[] {
					"p",
					"li",
					"em",
					"strong",
					"title", 
					"a", 
					"option", 
					"textarea", 
					"span", 
					"sub", 
					"sup", 
					"small", 
					"label", 
					"button", 
					"h1", 
					"h2", 
					"h3", 
					"h4", 
					"h5", 
					"h6",
					"legend",
					"pre"
			});
	
	private List<String> sameLineNoContent = Arrays.asList(
			new String[] {
					"pre",
					"script", 
					"textarea"
			});
	
	private List<String> pre = Arrays.asList(
			new String[] {
					"pre",
					"script",
					"style",
					"textarea"
			});
	
	public OutPrintFormatter(String str) throws SAXException, IOException {
		sb = new StringBuilder();
		str = str.replaceAll("<!--\\[if ([^\\]]*)\\]>", "&lt;!--[if $1]&gt;").replaceAll("<!\\[endif\\]-->", "&lt;![endif]--&gt;");
		str = str.replaceAll("<!--", "&lt;!--").replaceAll("-->", "--&gt;");
		String encoding = (String) ResourceUtils.getProperty("app.encoding");
		InputSource is = new InputSource(new StringReader(str));
		is.setEncoding(encoding);
		DOMFragmentParser parser = new DOMFragmentParser();
		parser.setProperty("http://cyberneko.org/html/properties/default-encoding", encoding);
		HTMLDocument document = new HTMLDocumentImpl();
		DocumentFragment fragment = document.createDocumentFragment();
		parser.parse(is, fragment);
		format(fragment, "", false);
	}
	
	public String toString() {
		return sb.toString();
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
	
	private boolean hasChildren(Node node) {
		return node.getFirstChild() != null;
	}
	
	private void format(Node node, String indent, boolean pre) {
		
		if ("".equals(indent)) {
			println("<!DOCTYPE html>");
		}
		
		Node child = node.getFirstChild();
		Node prev = null;
		
		while (child != null) {

			short nodeType = child.getNodeType();
			String nodeName = child.getNodeName().toLowerCase();
			String nodeValue = child.getNodeValue();
			Node parent = child.getParentNode();
			String parentName = parent.getNodeName().toLowerCase();
			
			if (nodeType == 1) {
				
				if (!isSameLine(parentName)) {
					print(indent + "<" + nodeName);
				} else {
					print("<" + nodeName);
				}
				
				NamedNodeMap m = child.getAttributes();
				if (m != null) {
					for (int i = 0; i < m.getLength(); i++) {
					   printattr(m.item(i));
					}
				}
				
				if (!isSameLine(nodeName) && !isSameLine(parentName) && !(isSameLineNoContent(nodeName) && !hasChildren(child))) {
					println(">");
				} else {
					print(">");
				}
				
				if (!isAutoClose(nodeName)) {
					
					format(child, indent + INDENT, isPre(nodeName));
					
					if (!isSameLine(nodeName)) {
						if (!isSameLine(parentName)) {
							if ((isSameLineNoContent(nodeName) && !hasChildren(child))) {
								println("</" + nodeName + ">");
							} else {
								println(indent + "</" + nodeName + ">");
							}
						} else {
							print(indent + "</" + nodeName + ">");
						}
					} else if (!isSameLine(parentName)) {
						println("</" + nodeName + ">");
					} else {
						print("</" + nodeName + ">");							
					}
				}
				
			} else if (nodeType == 3 && StringUtils.hasText(nodeValue)) {
				
				if (isStyleOrScript(parentName)) {
					
					String[] lines = nodeValue.split("\n");
					int l = 1000;
					for (String line : lines) {
						if (StringUtils.hasText(line)) {
							int i = line.length() - StringUtils.trimLeadingWhitespace(line).length();
							if (i < l) l = i;
						}
					}
					nodeValue = Pattern.compile("^\\s{" + l + "}", 
							Pattern.MULTILINE).matcher(nodeValue).replaceAll(indent).trim();
				}
				
				if (child == node.getFirstChild()) {
					nodeValue = StringUtils.trimLeadingWhitespace(nodeValue);
				}
				if (child == node.getLastChild()) {
					nodeValue = StringUtils.trimTrailingWhitespace(nodeValue);						
				}
				
				if (prev != null && isNoSpace(prev.getNodeName().toLowerCase())) {
					nodeValue = StringUtils.trimLeadingWhitespace(nodeValue);
				}
				
				if (!isStyleOrScript(parentName)) {
					//nodeValue = escape(nodeValue);
				}
				
				if (!isPre(parentName) && !nodeValue.startsWith("&lt;!--")) {
					nodeValue = nodeValue.replaceAll("\\n+", "").replaceAll("\\s+", " ").replaceAll("^ <", "<");
				}
				
				/*
				if (prev != null && (isSameLine(prev.getNodeName()) || prev.getNodeType() != 3)) {
					print(nodeValue);
				} else*/ 
				if (isSameLine(parentName)) {
					print(nodeValue);
				} else {
					if (nodeValue.indexOf("&lt;!--[if") != -1 || nodeValue.indexOf("&lt;![endif") != -1) {
						nodeValue = nodeValue.trim().replaceAll("&lt;", "<").replaceAll("&gt;", ">");
					}
					if (nodeValue.indexOf("&lt;!--") != -1 || nodeValue.indexOf("--&gt;") != -1) {
						nodeValue = nodeValue.trim().replaceAll("&lt;\\!--", "<!--").replaceAll("--&gt;", "-->");
					}
					println(indent + nodeValue);
				}
			}
			
			prev = child;
			child = child.getNextSibling();
		}
	}
	
	private String escape(String text) {
		return StringEscapeUtils.escapeHtml(text);
	}
	
	private void printattr(Node node) {
		String nodeName = node.getNodeName();
		String nodeValue = node.getNodeValue();
		if (nodeName.equals(nodeValue) && ("selected".equals(nodeName) || "checked".equals(nodeName))) {
			print(" " + nodeName);
		} else {
			print(" " + nodeName + "=\"" + escape(nodeValue) + "\"");
		}
	}
	
	private void print(String s) {
		sb.append(s);
	}
	
	private void println(String s) {
		print(s + "\n");
	}
  
}