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

package com.asual.summer.core.faces;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.faces.view.Location;
import javax.faces.view.facelets.Tag;
import javax.faces.view.facelets.TagAttribute;
import javax.faces.view.facelets.TagDecorator;

import com.asual.summer.core.util.StringUtils;
import com.sun.faces.facelets.tag.TagAttributeImpl;
import com.sun.faces.facelets.tag.TagAttributesImpl;
import com.sun.faces.facelets.tag.jsf.html.HtmlDecorator;
import com.sun.faces.facelets.tag.jsf.html.HtmlLibrary;

/**
 * 
 * @author Rostislav Hristov
 *
 */
public final class FacesDecorator implements TagDecorator {

    public final static String SUMMER = "http://java.sun.com/jsf/composite/cc";
    public final static String QNAME = "qName";
    public final static String ATTRIBUTES = "dataEmpty|dataEmptyOption|dataRepeat|dataRepeatBegin|dataRepeatEnd|" + 
    	"dataTemplate|dataValue|dataVar|dataVarStatus|dataPack|dataEscape|dataError|dataLabel";

    private List<String> reservedTags = Arrays.asList(new String[] {
        "repeat", 
	    "tag", 
	    "template" 
	});
    
    private List<String> headTags = Arrays.asList(new String[] {
        "base", 
	    "link", 
	    "meta", 
	    "script",
	    "style"
	    //title
	});
    
    private List<String> bodyTags = Arrays.asList(new String[] {
	    "a", 
	    "abbr", 
	    "address", 
	    "area", 
	    "article", 
	    "aside", 
	    "audio", 
	    "b", 
	    "bdo", 
	    "blockquote", 
	    "body", 
	    "br", 
	    "button", 
	    "canvas", 
	    "caption", 
	    "cite", 
	    "code", 
	    "col", 
	    "colgroup", 
	    "command", 
	    "datalist", 
	    "dd", 
	    "del", 
	    "details", 
	    "dfn", 
	    "div", 
	    "dl", 
	    "dt", 
	    "em", 
	    "embed", 
	    //"fieldset", 
	    "figcaption", 
	    "figure", 
	    "footer", 
	    //"form", 
	    "h1", 
	    "h2", 
	    "h3", 
	    "h4", 
	    "h5", 
	    "h6", 
	    //"head", 
	    "header", 
	    "hgroup", 
	    "hr", 
	    //"html", 
	    "i", 
	    "iframe", 
	    "img", 
	    "input", 
	    "ins", 
	    "keygen", 
	    "kbd", 
	    "label", 
	    "legend", 
	    "li", 
	    "map", 
	    "mark", 
	    "menu", 
	    "meter", 
	    "nav", 
	    "noscript", 
	    "object", 
	    "ol", 
	    "optgroup", 
	    "option", 
	    "output", 
	    "p", 
	    "param", 
	    "pre", 
	    "progress", 
	    "q", 
	    "rp", 
	    "rt", 
	    "ruby", 
	    "samp", 
	    "section", 
	    "select", 
	    "small", 
	    "source", 
	    "span", 
	    "strong", 
	    "sub", 
	    "summary", 
	    "sup", 
	    "table", 
	    "tbody", 
	    "td", 
	    "textarea", 
	    "tfoot", 
	    "th", 
	    "thead", 
	    "time", 
	    "tr", 
	    "ul", 
	    "var", 
	    "video"
    });

    public FacesDecorator() {
        super();
    }

    public Tag decorate(Tag tag) {
    	Location location = tag.getLocation();
    	String namespace = tag.getNamespace();
    	String qName = tag.getQName();
    	TagAttributeImpl tagNameAttr = null;
    	TagAttributeImpl tagTargetAttr = null;
		List<TagAttribute> attrs = new ArrayList<TagAttribute>(Arrays.asList(tag.getAttributes().getAll()));
    	if (StringUtils.isEmpty(namespace) && !HtmlDecorator.XhtmlNamespace.equals(namespace) && !reservedTags.contains(qName)) {
        	String name = qName;
        	if (headTags.contains(qName)) {
				name = "outputStylesheet";
				tagNameAttr = new TagAttributeImpl(location, namespace, QNAME, QNAME, qName);
				tagTargetAttr = new TagAttributeImpl(location, namespace, "target", "target", "head");
			} else if (bodyTags.contains(qName)) {
				name = "tag";
				tagNameAttr = new TagAttributeImpl(location, namespace, QNAME, QNAME, qName);
			}
    		for (TagAttribute attr : attrs) {
    			if ("target".equals(attr.getQName())) {
    				tagTargetAttr = null;
    			} else if ("data-template".equals(attr.getQName())) {
    				tagNameAttr = new TagAttributeImpl(location, namespace, QNAME, QNAME, qName);
    				name = "html".equals(qName) ? "html" : "template";
    			} else if ("data-repeat".equals(attr.getQName())) {
    				tagNameAttr = new TagAttributeImpl(location, namespace, QNAME, QNAME, qName);
    				name = "repeat";
    			} else if ("data-error".equals(attr.getQName())) {
    				tagNameAttr = new TagAttributeImpl(location, namespace, QNAME, QNAME, qName);
    				name =  Boolean.valueOf(attr.getValue()) ? qName : "tag";
    			} else if ("data-label".equals(attr.getQName()) && !StringUtils.isEmpty(attr.getValue())) {
    				tagNameAttr = new TagAttributeImpl(location, namespace, QNAME, QNAME, qName);
    				name = qName;
    			} else if ("input".equals(qName) && "type".equals(attr.getQName()) && attr.getValue().matches("hidden|reset|submit")) {
    				name = qName;
    			}
    	    	if (attr.getQName().startsWith("data-") && !attr.getQName().equals("data-rendered")) {
    	    		String replace = convertName(attr.getQName());
    	    		if (replace.matches(ATTRIBUTES)) {
    	    			attrs.set(attrs.indexOf(attr), 
    	    					new TagAttributeImpl(location, attr.getNamespace(), replace, replace, attr.getValue()));
    	    		}
    			}
    			replaceAttr(attrs, attr, "class", "styleClass");
    			replaceAttr(attrs, attr, "data-rendered", "rendered");
    		}
    		if ("select".equals(qName)) {
    			if ("select".equals(name)) {
        			attrs.add(new TagAttributeImpl(location, namespace, "dataRepeat", "dataRepeat", "${''.split('')}"));
    			} else {
    				name = "select";
    			}
    		}
    		if (tagNameAttr != null) {
    			attrs.add(tagNameAttr);
    		}
    		if (tagTargetAttr != null) {
    			attrs.add(tagTargetAttr);
    		}
    		return new Tag(location, headTags.contains(qName) ? HtmlLibrary.Namespace : SUMMER, name, name, 
            		new TagAttributesImpl(attrs.toArray(new TagAttribute[] {})));
    	}
    	return tag;
    }

    private String convertName(String name) {
    	String[] parts = name.split("-");
    	int l = parts.length;
    	for (int i = 1; i < l; i++) {
    		parts[i] = StringUtils.toTitleCase(parts[i]);
    	}
    	return StringUtils.join(parts, "");
    }
    
    private void replaceAttr(List<TagAttribute> attrs, TagAttribute attr, String search, String replace) {
    	if (search.equals(attr.getQName())) {
			attrs.set(attrs.indexOf(attr), new TagAttributeImpl(attr.getLocation(), attr.getNamespace(), replace,
					replace, attr.getValue()));
		}
    }    
}