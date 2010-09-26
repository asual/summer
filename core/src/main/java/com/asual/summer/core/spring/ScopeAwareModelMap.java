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
package com.asual.summer.core.spring;

import java.util.Collection;
import java.util.Map;

import org.springframework.core.Conventions;
import org.springframework.ui.ModelMap;

/**
 * @author Rostislav Georgiev
 * 
 */
public class ScopeAwareModelMap extends ModelMap {
    private static final long serialVersionUID = -6880632033847815028L;
    private static final String KEY_SUFFIX = "ScopedMap";

    public enum Scope {
        REQUEST, FLASH, SESSION;
        public String getKey(){
            return getClass()+KEY_SUFFIX;
        }
    }

    public ScopeAwareModelMap() {
    }

    public ScopeAwareModelMap(Object attributeValue) {
        super(attributeValue);
    }

    public ScopeAwareModelMap(Object attributeValue, Scope scope) {
        addAttribute(attributeValue, scope);

    }

    public ScopeAwareModelMap(String attributeName, Object attributeValue) {
        super(attributeName, attributeValue);
    }

    public ScopeAwareModelMap(String attributeName, Object attributeValue, Scope scope) {
        addAttribute(attributeName, attributeValue, scope);
    }

    @SuppressWarnings("rawtypes")
    public ScopeAwareModelMap addAttribute(Object attributeValue, Scope scope) {
        addAttribute(attributeValue);
        if (attributeValue instanceof Collection && ((Collection) attributeValue).isEmpty()) {
            return this;
        }
        return addAttribute(Conventions.getVariableName(attributeValue), attributeValue, scope);

    }

    private ScopeAwareModelMap addAttribute(String attributeName, Object attributeValue, Scope scope) {
        addAttribute(attributeName, attributeValue);
        if (null != scope) {
            ModelMap scopeMap = getScopedMap(scope);
            scopeMap.addAttribute(attributeName, attributeValue);
        }
        return this;
    }

    private ModelMap getScopedMap(Scope scope) {
        String key = scope.getKey();
        ModelMap scopeMap = (ModelMap) get(key);
        if (null == scopeMap) {
            scopeMap = new ModelMap();
            addAttribute(key, scopeMap);
        }
        return scopeMap;
    }

    public ScopeAwareModelMap addAllAttributes(Collection<?> attributeValues, Scope scope) {
        if (attributeValues != null) {
            for (Object attributeValue : attributeValues) {
                addAttribute(attributeValue, scope);
            }
        }
        return this;
    }

    public ScopeAwareModelMap addAllAttributes(Map<String, ?> attributes, Scope scope) {
        if (attributes != null) {
            putAll(attributes);
            if (null != scope) {
                getScopedMap(scope).putAll(attributes);
            }
        }
        return this;
    }
    
    public boolean containsAttribute(String attributeName,Scope scope) {
        if(null==scope){
            return containsAttribute(attributeName);
        }
        ModelMap scopedMap = getScopedMap(scope);
        return scopedMap.containsAttribute(attributeName);
    }
    
    public ModelMap getAttributes(Scope scope) {
        if(null==scope){
            return this;
        }
        return getScopedMap(scope);
    }
}
