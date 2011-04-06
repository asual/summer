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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;

import javax.faces.component.UIComponent;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.render.Renderer;

import com.sun.faces.renderkit.Attribute;
import com.sun.faces.renderkit.AttributeManager;
import com.sun.faces.renderkit.RenderKitUtils;

/**
 * 
 * @author Rostislav Hristov
 *
 */
public class HeadRenderer extends Renderer {
	
	private static final Attribute[] HEAD_ATTRIBUTES = 
		AttributeManager.getAttributes(AttributeManager.Key.OUTPUTHEAD);

	public void decode(FacesContext context, UIComponent component) {
	}

	public void encodeChildren(FacesContext context, UIComponent component)
		throws IOException {
	}
	
	public void encodeBegin(FacesContext context, UIComponent component)
		  throws IOException {
		ResponseWriter writer = context.getResponseWriter();
		writer.startElement("head", component);
		RenderKitUtils.renderPassThruAttributes(context,
												writer,
												component,
												HEAD_ATTRIBUTES);
		encodeHeadResources(context, component);
	}
	
	public void encodeEnd(FacesContext context, UIComponent component)
		throws IOException {
		ResponseWriter writer = context.getResponseWriter();
		writer.endElement("head");
		writer.write("\n");
	}

	protected void encodeHeadResources(FacesContext context, UIComponent component)
		throws IOException {
		
		UIViewRoot viewRoot = context.getViewRoot();
		ListIterator<?> iter = (viewRoot.getComponentResources(context, "head")).listIterator();
		List<UIComponent> stylesheetComponents = new ArrayList<UIComponent>();
		List<UIComponent> scriptComponents = new ArrayList<UIComponent>();
		List<UIComponent> tagComponents = new ArrayList<UIComponent>();
		
		while (iter.hasNext()) {
			UIComponent resource = (UIComponent) iter.next();
			if (isStylesheet(resource)) {
				if (isUnique(stylesheetComponents, resource)) {
					stylesheetComponents.add(resource);
				}
			} else if (isScript(resource)) {
				if (isUnique(scriptComponents, resource)) {
					scriptComponents.add(resource);
				}
			} else {
				tagComponents.add(resource);
			}
		}

		for (UIComponent c : tagComponents) {
			c.encodeAll(context);
		}
		
		sortByPack(stylesheetComponents);
		for (UIComponent c : stylesheetComponents) {
			c.encodeAll(context);
		}

		sortByPack(scriptComponents);
		for (UIComponent c : scriptComponents) {
			c.encodeAll(context);
		}
	}
	
	protected void sortByPack(List<UIComponent> components) {
		Collections.sort(components, new PackComparator<UIComponent>());
	}
	
	protected boolean isStylesheet(UIComponent component) {
		String qName = (String) component.getAttributes().get(FacesDecorator.QNAME);
		String type = (String) component.getAttributes().get("type");
		return ("link".equals(qName) || "style".equals(qName)) && (type == null || "text/css".equals(type));
	}
	
	protected boolean isScript(UIComponent component) {
		String qName = (String) component.getAttributes().get(FacesDecorator.QNAME);
		String type = (String) component.getAttributes().get("type");
		return "script".equals(qName) && (type == null || "text/javascript".equals(type));
	}
	
	protected boolean isUnique(List<UIComponent> components, UIComponent component) {
		for (UIComponent c : components) {
			try {
				String attr = null;
				if (isStylesheet(component)) {
					attr = "href";
				} else if (isScript(component)) {
					attr = "src";					
				}
				if (attr != null && c.getAttributes().get(attr).equals(component.getAttributes().get(attr))) {
					return false;
				}				
			} catch (Exception e) {
			}
		}
		return true;
	}	
}

class PackComparator<T> implements Comparator<UIComponent> {

	public int compare(UIComponent c1, UIComponent c2) {
		String pkg1 = (String) c1.getAttributes().get("dataPack");
		String pkg2 = (String) c2.getAttributes().get("dataPack");
		if ("summer".equals(pkg1)) {
			return -1;
		} else if ("summer".equals(pkg2)) {
			return 1;
		} else if (pkg1 != null && pkg2 != null) {
			return pkg1.compareToIgnoreCase(pkg2);
		} else if (pkg1 != null && pkg2 == null) {
			return -1;
		} else if (pkg1 == null && pkg2 != null) {
			return 1;
		}
		return 0;
	}
	
}