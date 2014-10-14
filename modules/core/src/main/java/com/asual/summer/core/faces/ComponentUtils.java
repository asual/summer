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

import java.beans.BeanInfo;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.el.ValueExpression;
import javax.faces.application.Resource;
import javax.faces.component.UIComponent;
import javax.faces.component.UINamingContainer;
import javax.faces.component.UIViewRoot;
import javax.faces.component.behavior.ClientBehaviorHolder;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import org.jboss.el.lang.EvaluationContext;
import org.jboss.el.lang.ExpressionBuilder;

import com.asual.summer.core.ErrorResolver;
import com.asual.summer.core.util.BeanUtils;
import com.asual.summer.core.util.RequestUtils;
import com.asual.summer.core.util.StringUtils;
import com.sun.faces.facelets.el.TagValueExpression;
import com.sun.faces.facelets.tag.jsf.ComponentSupport;

/**
 * 
 * @author Rostislav Hristov
 *
 */
public class ComponentUtils {
	
	final static String ATTRIBUTES = "^-?\\d.*$|idx|varAttr|com.sun.faces.facelets.APPLIED|javax.faces.retargetablehandlers";
	
	static Object getConfig(Component component, String name) {
		BeanInfo info = (BeanInfo) component.getAttributes().get(UIComponent.BEANINFO_KEY);
		if (info != null) {
			TagValueExpression ve = (TagValueExpression) info.getBeanDescriptor().getValue(name);
			if (ve != null) {
				return ve.getValue(FacesContext.getCurrentInstance().getELContext());
			}
		}
		return null;
	}
	
	static String getChildrenText(Component component) {
		List<String> strings = new ArrayList<String>();
		Iterator<UIComponent> fac = component.getFacetsAndChildren();
		while (fac.hasNext()) {
			UIComponent value = fac.next().findComponent("value");
			for (UIComponent child : value.getChildren()) {
				strings.add(child.toString());
			}
		}
		return StringUtils.join(strings, "");
	}

	static String getValueId(Component component) {
		try {
			ValueExpression value = component.getBindings().get("value");
			ValueExpression dataValue = component.getBindings().get("dataValue");
			try {
				FacesContext context = FacesContext.getCurrentInstance();
				RepeatComponent repeatComponent = getRepeatComponent(component);
				String componentTag = (String) repeatComponent.getAttributes().get(FacesDecorator.QNAME);
				if (componentTag != null) {
					return getExprId(repeatComponent.getBindings().get("dataValue").getExpressionString()) + 
						UINamingContainer.getSeparatorChar(context) + value.getValue(context.getELContext());
				}
			} catch (Exception e) {}
			if (dataValue != null) {
				return getExprId(dataValue.getExpressionString());
			}
			if (value == null) {
				return getExprId(getChildrenText(component));
			}
			return getExprId(value.getExpressionString());
		} catch (Exception e) {}		
		return null;
	}
	
	static RepeatComponent getRepeatComponent(Component component) {
		UIComponent parent = component.getParent();
		if (parent instanceof RepeatComponent) {
			return (RepeatComponent) parent;
		} else {
			return getRepeatComponent((Component) parent);
		}
	}
	
	static String getClientId(Component component) {
		ValueExpression ve = component.getBindings().get("idx");
		if (ve != null) {
			return (String) ve.getValue(FacesContext.getCurrentInstance().getELContext());
		}
		String id = component.getId();
		if (!id.startsWith(UIViewRoot.UNIQUE_ID_PREFIX)) {
			return id;
		}
		String name = getAttrValue(component, "name");
		if (StringUtils.isEmpty(name)) {
			String valueId = getValueId(component);
			if (valueId != null) {
				return valueId;
			}
		}
		return id;
	}
	
	static String getComponentClass(Component component) {
		return (String) getConfig(component, "componentClass");
	}
	
	static List<String> getComponentClasses(Component component) {
		List<String> classes = new ArrayList<String>();
		String componentClass = getComponentClass(component);
		if (!StringUtils.isEmpty(componentClass)) {
			classes.add(componentClass);
		}
		String styleClass = (String) component.getAttributes().get("styleClass");
		if (!StringUtils.isEmpty(styleClass)) {
			classes.add(styleClass);
		}
		return classes;
	}
	
	static String getValue(UIComponent component) {
		return (String) ExpressionBuilder.createNode(component.getChildren().get(0).toString())
			.getValue(new EvaluationContext(FacesContext.getCurrentInstance().getELContext(), null, null));
	}
	
	static String getAttrValue(Component component, String key) {
		Object value = component.getAttributes().get(key);
		if (value == null) {
			ValueExpression expr = component.getBindings().get(key);
			if (expr != null) {
				value = expr.getValue(new EvaluationContext(FacesContext.getCurrentInstance().getELContext(), null, null));
			}
		}
		if (value != null) {
			return value.toString();
		}
		return null;
	}
	
	static String getFormId(Component component) {
		String id = component.getClientId();
		String name = ComponentUtils.getAttrValue(component, "name");
		if ((StringUtils.isEmpty(id) || id.startsWith(UIViewRoot.UNIQUE_ID_PREFIX)) && StringUtils.isEmpty(name)) {
			id = ComponentUtils.getValueId(component);
		}
		if (!StringUtils.isEmpty(id) && !id.startsWith(UIViewRoot.UNIQUE_ID_PREFIX)) {
			return id;
		}
		return null;
	}
		
	static String getFormName(Component component) {
		String name = ComponentUtils.getAttrValue(component, "name");
		if (!StringUtils.isEmpty(name)) {
			return name;
		}
		try {
			return ComponentUtils.getExprId(ComponentUtils.getRepeatComponent(component).getBindings().get("dataValue").getExpressionString());
		} catch (Exception e) {
			return getFormId(component);
		}
	}
	
	static boolean shouldWriteIdAttribute(Component component, String id) {
		String componentTag = getComponentTag((UIComponent) component);
		return (id != null && (isComponentWrapper((UIComponent) component) || componentTag != null) && !"option".equals(componentTag) &&
					(!id.startsWith(UIViewRoot.UNIQUE_ID_PREFIX) ||
						((component instanceof ClientBehaviorHolder) &&
						  !((ClientBehaviorHolder) component).getClientBehaviors().isEmpty())));
	}
	
	static String getComponentTag(UIComponent component) {
		if (component instanceof RepeatComponent) {
			return (String) component.getAttributes().get(FacesDecorator.QNAME);
		} else {
			return (String) ComponentUtils.getConfig((Component) component, "componentTag");
		}
	}
	
	static boolean isComponentWrapper(UIComponent component) {
		Object config = ComponentUtils.getConfig((Component) component, "componentWrapper");
		if (config != null) {
			return config instanceof String ? Boolean.valueOf((String) config) : Boolean.valueOf((Boolean) config);
		}
		return false;
	}
	
	static Map<String, Object> getAttributes(Component component, String name) {
		
		Map<String, Object> attrs = new HashMap<String, Object>(component.getAttributes());
		
		String id = component.getClientId();
		if (shouldWriteIdAttribute(component, id)) {
			attrs.put("id", id);
		}
		
		Map<String, ValueExpression> bindings = component.getBindings();
		for (String key : bindings.keySet()) {
			if (ComponentUtils.shouldWriteAttribute(component, key)) {
				attrs.put(key, bindings.get(key).getValue(FacesContext.getCurrentInstance().getELContext()));
			}
		}
		
		if ("form".equals(name)) {

			String action = ComponentUtils.getAttrValue(component, "action");
			attrs.put("action", StringUtils.isEmpty(action) ? RequestUtils.getRequestUri() : RequestUtils.contextRelative(action, true));
			
			String method = ComponentUtils.getAttrValue(component, "method");
			attrs.put("method", StringUtils.isEmpty(method) ? "get" : (RequestUtils.isMethodBrowserSupported(method) ? method : "post"));
						
		} else if ("input".equals(name)) {
			
			attrs.put("id", getFormId(component));
			attrs.put("name", getFormName(component));
			
			String type = (String) attrs.get("type");
			
			if ("checkbox".equals(type) || "radio".equals(type)) {
				if (isMatch(component) && ComponentUtils.getAttrValue(component, "checked") == null) {
					attrs.put("checked", true);
				}
			}
			
			Map<String, Map<String, Object>> errors = BeanUtils.getBeanOfType(ErrorResolver.class).getErrors();
			attrs.put("value", errors != null && errors.get(getFormId(component)) != null ? 
					errors.get(getFormId(component)).get("value") : ComponentUtils.getAttrValue(component, "value"));
			
		} else if ("select".equals(name) || "textarea".equals(name)) {
			
			attrs.put("id", getFormId(component));
			attrs.put("name", getFormName(component));
			
		} else if ("option".equals(name)) {
			
			if (isMatch(component) && ComponentUtils.getAttrValue(component, "selected") == null) {
				attrs.put("selected", true);
			}
		}
		
		return attrs;
	}
	
	static boolean shouldWriteAttribute(Component component, String key) {
		return !Pattern.compile(ATTRIBUTES + "|" + 
			FacesDecorator.ATTRIBUTES + "|" + 
			FacesDecorator.QNAME + "|" + 
			ComponentSupport.MARK_CREATED + "|" + 
			Resource.COMPONENT_RESOURCE_KEY + "|" + 
			UIComponent.BEANINFO_KEY + "|" + 
			UIComponent.FACETS_KEY + "|" + 
			UIComponent.VIEW_LOCATION_KEY, Pattern.CASE_INSENSITIVE).matcher(key).matches();
	}
	
	static Object contextAttribute(String name, Object value) {
		if (value != null && "data-ajax-url".equalsIgnoreCase(name) || 
				"href".equalsIgnoreCase(name) || "src".equalsIgnoreCase(name)) {
			return RequestUtils.contextRelative(value.toString(), true);
		}
		return value;
	}
	
	static void writeAttribute(ResponseWriter writer, String name, Object value) throws IOException {
		if (value != null && !"rendered".equalsIgnoreCase(name) && !"styleClass".equalsIgnoreCase(name)) {
			writer.writeAttribute(name, contextAttribute(name, value), null);
		}
	}
	
	static String getExprId(String expr) {
		List<String> values = Arrays.asList(expr.trim().replaceAll("^(\\$|#)\\{|\\}$", "").split("\\."));
		if (values.size() > 1) {
			return StringUtils.join(values.subList(1, values.size()), ".");
		} else if (values.size() > 0) {
			return values.get(0);
		}
		return null;
	}
	
	static boolean isMatch(Component component) {
		try {
			RepeatComponent repeatComponent = getRepeatComponent(component);
			Map<String, ValueExpression> bindings = repeatComponent.getBindings();
			FacesContext context = FacesContext.getCurrentInstance();
			Object dataValue = bindings.get("dataValue").getValue(context.getELContext());
			Map<String, Map<String, Object>> errors = BeanUtils.getBeanOfType(ErrorResolver.class).getErrors();
			if (errors != null && errors.get(getFormName(component)) != null) {
				dataValue = errors.get(getFormName(component)).get("value");
			}
			Object valueAttr = RequestUtils.getAttribute(repeatComponent.getVar());
			if (dataValue instanceof List<?>) {
			 	return ((List<?>) dataValue).contains(valueAttr);
			} else if (dataValue instanceof Boolean) {
				return dataValue.equals(Boolean.valueOf(valueAttr.toString()));
			} else {
				return dataValue.equals(valueAttr);
			}
		} catch (Exception e) {
			return false;
		}
	}
	
	static boolean isStyleOrScript(String nodeName) {
		return "style".equals(nodeName) || "script".equals(nodeName);
	}
	
}