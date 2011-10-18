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

package com.asual.summer.lesspack;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import javax.faces.application.ProjectStage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import com.asual.lesscss.ResourcePackage;
import com.asual.summer.core.faces.HeadRenderer;
import com.asual.summer.core.util.RequestUtils;
import com.asual.summer.core.util.ResourceUtils;

/**
 * 
 * @author Rostislav Hristov
 *
 */
public class HeadPackRenderer extends HeadRenderer {
   
	protected void encodeHeadResources(FacesContext context, UIComponent component) throws IOException {
		
		if (context.isProjectStage(ProjectStage.Production)) {
			
			ResponseWriter writer = context.getResponseWriter();
			UIViewRoot viewRoot = context.getViewRoot();
			ListIterator<?> iter = (viewRoot.getComponentResources(context, "head")).listIterator();
			List<UIComponent> styleComponents = new ArrayList<UIComponent>();
			List<UIComponent> scriptComponents = new ArrayList<UIComponent>();
			List<UIComponent> tagComponents = new ArrayList<UIComponent>();
						
			while (iter.hasNext()) {
				UIComponent resource = (UIComponent) iter.next();
				if (isStyle(resource)) {
					if (isUnique(styleComponents, resource)) {
						styleComponents.add(resource);
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
			
			String version = (String) ResourceUtils.getProperty("app.version");
			String dataPack = null;

			List<String> styles = new ArrayList<String>();
			List<String> scripts = new ArrayList<String>();
			
			sortByPack(styleComponents);
			for (UIComponent c : styleComponents) {
				
				if (c.isRendered()) {
					Map<String, Object> attrs = c.getAttributes();
					String href = (String) attrs.get("href");
					String pkg = (String) attrs.get("dataPack");
					
					if (href != null) {
						preEncodeAll(context, c);
						if (dataPack != null && !dataPack.equals(pkg)) {
							combineStyles(writer, component, styles, dataPack, version);
						}
						dataPack = pkg;
						styles.add(href);
					} else {
						combineStyles(writer, component, styles, dataPack, version);
						preEncodeAll(context, c);
						postEncodeAll(context, c);
					}
				}
			}
			combineStyles(writer, component, styles, dataPack, version);
			
			dataPack = null;
			
			sortByPack(scriptComponents);
			for (UIComponent c : scriptComponents) {
				
				if (c.isRendered()) {
					Map<String, Object> attrs = c.getAttributes();
					String src = (String) attrs.get("src");
					String pkg = (String) attrs.get("dataPack");
					
					if (src != null) {
						preEncodeAll(context, c);
						if (dataPack != null && !dataPack.equals(pkg)) {
							combineScripts(writer, component, scripts, dataPack, version);
						}
						dataPack = pkg;
						scripts.add(src);
					} else {
						combineScripts(writer, component, scripts, dataPack, version);
						preEncodeAll(context, c);
						postEncodeAll(context, c);
					}
				}
			}
			combineScripts(writer, component, scripts, dataPack, version);
			
		} else {
			
			super.encodeHeadResources(context, component);
		}
	}
	
	private void preEncodeAll(FacesContext context, UIComponent component) throws IOException {
		if (context == null) {
			throw new NullPointerException();
		}
		if (!component.isRendered()) {
			return;
		}
		Map<String, Object> attrs = component.getAttributes();
		if ((isStyle(component) && attrs.get("href") == null) || 
				(isScript(component) && attrs.get("src") == null)) {
			component.encodeBegin(context);
		}
	}
	
	private void postEncodeAll(FacesContext context, UIComponent component) throws IOException {
		if (component.getRendersChildren()) {
			component.encodeChildren(context);
		} else if (component.getChildCount() > 0) {
			for (UIComponent kid : component.getChildren()) {
				kid.encodeAll(context);
			}
		}
		Map<String, Object> attrs = component.getAttributes();
		if ((isStyle(component) && attrs.get("href") == null) || 
				(isScript(component) && attrs.get("src") == null)) {
			component.encodeEnd(context);
		}
	}
	
	private void writeStyle(ResponseWriter writer, UIComponent component, String href) throws IOException {
		writer.startElement("link", component);
		writer.writeAttribute("type", "text/css", "type");
		writer.writeAttribute("rel", "stylesheet", "rel");
		writer.writeAttribute("href", href, "href");
		writer.endElement("link");
	}
	
	private void writeScript(ResponseWriter writer, UIComponent component, String src) throws IOException {
		writer.startElement("script", component);
		writer.writeAttribute("type", "text/javascript", "type");
		writer.writeAttribute("src", src, "src");
		writer.endElement("script");
	}
	
	private void combineStyles(ResponseWriter writer, UIComponent component, List<String> styles, String name, String version) throws IOException {
		String type = "css";
		List<String> pack = new ArrayList<String>();
		for (String href : styles) {
			if (href.startsWith("http") || !href.startsWith("/" + type)) {
				writeStyle(writer, component, href);
			} else {
				pack.add(href);
			}
		}
		if (pack.size() != 0) {
			ResourcePackage rp = new ResourcePackage(pack.toArray(new String[]{}));
			rp.setName(name);
			rp.setVersion(version);
			rp.setExtension(type);
			writeStyle(writer, component, RequestUtils.contextRelative("/" + type + rp, true));
		}
		styles.clear();
	}
	
	private void combineScripts(ResponseWriter writer, UIComponent component, List<String> scripts, String name, String version) throws IOException {
		String type = "js";
		List<String> pack = new ArrayList<String>();
		for (String src : scripts) {
			if (src.startsWith("http") || !src.startsWith("/" + type)) {
				writeScript(writer, component, src);
			} else {
				pack.add(src);
			}
		}
		if (pack.size() != 0) {
			ResourcePackage rp = new ResourcePackage(pack.toArray(new String[]{}));
			rp.setName(name);
			rp.setVersion(version);
			rp.setExtension(type);
			writeScript(writer, component, RequestUtils.contextRelative("/" + type + rp, true));
		}
		scripts.clear();
	}

}