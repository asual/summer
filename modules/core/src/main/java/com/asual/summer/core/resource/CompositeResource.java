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

package com.asual.summer.core.resource;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import org.springframework.core.OrderComparator;

import com.asual.summer.core.util.BeanUtils;

/**
 * 
 * @author Rostislav Hristov
 *
 */
public class CompositeResource extends AbstractResource {

	private CompositeResource parent;
	private Map<Object, Object> properties;
	private List<CompositeResource> children;

	public CompositeResource getParent() {
		return parent;
	}

	public void setParent(CompositeResource parent) {
		this.parent = parent;
	}

	public void setProperties(Map<Object, Object> properties) {
		this.properties = properties;
	}

	public Map<Object, Object> getProperties() {
		return properties;
	}

	public List<CompositeResource> getChildren() {
		if (children == null) {
			LinkedHashSet<CompositeResource> set = new LinkedHashSet<CompositeResource>();
			Map<String, CompositeResource> beans = BeanUtils.getBeansOfType(CompositeResource.class);
			for (CompositeResource resource : beans.values()) {
				if (this.equals(resource.getParent())) {
					set.add(resource);
				}
			}
			children = new ArrayList<CompositeResource>(set);
			OrderComparator.sort(children);
		}
		return children;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((parent == null) ? 0 : parent.hashCode());
		result = prime * result
				+ ((properties == null) ? 0 : properties.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		CompositeResource other = (CompositeResource) obj;
		if (parent == null) {
			if (other.parent != null)
				return false;
		} else if (!parent.equals(other.parent))
			return false;
		if (properties == null) {
			if (other.properties != null)
				return false;
		} else if (!properties.equals(other.properties))
			return false;
		return true;
	}
	
}
