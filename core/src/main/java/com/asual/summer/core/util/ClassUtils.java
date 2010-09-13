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

package com.asual.summer.core.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

/**
 * 
 * @author Rostislav Hristov
 *
 */
@Component
public class ClassUtils {
    
	public static boolean instanceOf(Object obj, String className) {
    	try {
			return Class.forName(className).isInstance(obj);
		} catch (ClassNotFoundException e) {
			return false;
		}
    }

    public static Object instance(String className, Object[] parameters) throws SecurityException, ClassNotFoundException {
    	Class<?>[] classParameters = new Class[parameters == null ? 0 : parameters.length];
    	for (int i = 0; i < classParameters.length; i++) {
    		classParameters[i] = parameters[i].getClass();
    	}
    	Object instance = null;
    	try {
    		instance = Class.forName(className).getConstructor(classParameters).newInstance(parameters == null ? new Object[] {} : parameters.length);
    	} catch (Exception e) {
    		Constructor<?>[] constructors = Class.forName(className).getConstructors();
    		for (Constructor<?> constructor : constructors) {
    			Class<?>[] types = constructor.getParameterTypes();
    			if (types.length == parameters.length) {
    				Object[] params = new Object[parameters.length];
    				for (int i = 0; i < parameters.length; i++) {
    					if (types[i] == boolean.class || types[i] == Boolean.class) {
    						params[i] = Boolean.valueOf((String) parameters[i]);
    					} else if (types[i] == int.class || types[i] == Integer.class) {
    						params[i] = Integer.valueOf((String) parameters[i]);
    					} else {
    						params[i] = types[i].cast(parameters[i]);
    					}
    				}
    				try {
    					instance = constructor.newInstance(params);
    					break;
    				} catch (Exception ex) {
    					continue;
    				}
    			}
    		}
    	}
    	return instance;
    }

    public static Object invoke(Object target, final String methodName, final Object[] parameters) {
        if (target != null) {
            final List<Method> matches = new ArrayList<Method>();
            ReflectionUtils.doWithMethods(target.getClass(), new ReflectionUtils.MethodCallback() {
                public void doWith(Method method) throws IllegalArgumentException,
                        IllegalAccessException {
                    matches.add(method);
                }
            }, 
            new ReflectionUtils.MethodFilter() {
                public boolean matches(Method method) {
                    if (method.getName().equals(methodName)) {
                        Class<?>[] types = method.getParameterTypes();
                        if (parameters == null && types.length == 0) {
                            return true;
                        }
                        if (types.length != parameters.length) {
                            return false;
                        }
                        for (int i = 0; i < types.length; i++) {
                            if (!types[i].isInstance(parameters[i])) {
                                return false;
                            }
                        }
                        return true;
                    }
                    return false;
                }
            });
            if (matches.size() > 0) {
                if (parameters == null) {
                    return ReflectionUtils.invokeMethod(matches.get(0), target);
                } else {
                    return ReflectionUtils.invokeMethod(matches.get(0), target, parameters);                
                }
            }
        }
        return null;
    }
}
