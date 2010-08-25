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

package org.jboss.el.util;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.el.ELException;
import javax.el.MethodInfo;
import javax.el.MethodNotFoundException;

import org.jboss.el.lang.ELSupport;


/**
 * Utilities for Managing Serialization and Reflection
 *
 * @author Jacob Hookom [jacob@hookom.net]
 * @version $Change: 181177 $$DateTime: 2001/06/26 08:45:09 $$Author: markt $
 */
@SuppressWarnings({"rawtypes", "unchecked", "unused"})
public final class ReflectionUtil {
    
    protected static final String[] EMPTY_STRING = new String[0];
    
    protected static final String[] PRIMITIVE_NAMES = new String[] { "boolean",
    "byte", "char", "double", "float", "int", "long", "short", "void" };
    
    protected static final Class[] PRIMITIVES = new Class[] { boolean.class,
    byte.class, char.class, double.class, float.class, int.class,
    long.class, short.class, Void.TYPE };
    
    /**
     *
     */
    private ReflectionUtil() {
        super();
    }
    
    public static Class forName(String name) throws ClassNotFoundException {
        if (null == name || "".equals(name)) {
            return null;
        }
        Class c = forNamePrimitive(name);
        if (c == null) {
            if (name.endsWith("[]")) {
                String nc = name.substring(0, name.length() - 2);
                c = Class.forName(nc, true, Thread.currentThread()
                .getContextClassLoader());
                c = Array.newInstance(c, 0).getClass();
            } else {
                c = Class.forName(name, true, Thread.currentThread()
                .getContextClassLoader());
            }
        }
        return c;
    }
    
    protected static Class forNamePrimitive(String name) {
        if (name.length() <= 8) {
            int p = Arrays.binarySearch(PRIMITIVE_NAMES, name);
            if (p >= 0) {
                return PRIMITIVES[p];
            }
        }
        return null;
    }
    
    /**
     * Converts an array of Class names to Class types
     *
     * @param s
     * @return
     * @throws ClassNotFoundException
     */
    public static Class[] toTypeArray(String[] s) throws ClassNotFoundException {
        if (s == null)
            return null;
        Class[] c = new Class[s.length];
        for (int i = 0; i < s.length; i++) {
            c[i] = forName(s[i]);
        }
        return c;
    }
    
    /**
     * Converts an array of Class types to Class names
     *
     * @param c
     * @return
     */
    public static String[] toTypeNameArray(Class[] c) {
        if (c == null)
            return null;
        String[] s = new String[c.length];
        for (int i = 0; i < c.length; i++) {
            s[i] = c[i].getName();
        }
        return s;
    }
    
    private static Method pickBest(Class[] paramTypes, Method a, Method b) {
        int r = 0;
        for (int i = 0; i < paramTypes.length; i++) {
            if (paramTypes[i] != null) {
                r += matches(paramTypes[i], a.getParameterTypes()[i]);
                r -= matches(paramTypes[i], b.getParameterTypes()[i]);
            }
        }
        return (r >= 0) ? a : b;
    }
    
    private static int matches(Class t, Class p) {
        if (t == p || t.equals(p)) {
            return 2;
        }
        if (p.isAssignableFrom(t)) {
            return 1;
        }
        return 0;
    }
    
    private static ReferenceCache<Class, MethodCache> methodCache = new ReferenceCache<Class, MethodCache>(ReferenceCache.Type.Weak, ReferenceCache.Type.Soft) {
    	public MethodCache create(Class key) {
    		return new MethodCache(key);
    	}
    };
    
    private static final class MethodCache {
        private final Method[] methods;
        private final Class type;
        private final Map<String,Object> cache;
        public MethodCache(Class type) {
            boolean isAnonymous = type.isAnonymousClass();
            boolean isPrivate = !Modifier.isPublic(type.getModifiers());

            this.type = type;
            this.methods = type.getMethods();
            this.cache = new HashMap<String,Object>();
            Object c;
            for (Method m : this.methods) {
                if ((isPrivate || isAnonymous) && Modifier.isPublic(m.getModifiers())) {
                    m.setAccessible(true);                    
                }
                c = this.cache.get(m.getName());
                if (c == null) {
                    this.cache.put(m.getName(), m);
                } else if (c instanceof Method) {
                    List l = new ArrayList(5);
                    l.add(m);
                    l.add(c);
                    this.cache.put(m.getName(), l);
                } else {
                    ((List) c).add(m);
                }
            }
        }
        
        public Class getType() {
            return this.type;
        }
        
        public Method findMethod(String name, Object[] in) {
            Object o = this.cache.get(name);
            if (o == null) return null;
            if (o instanceof Method) return (Method) o;
            Method r = null;
            Class[] types = paramTypes(in);
            for (Method m : (List<Method>) o) {
                if (m.getParameterTypes().length == types.length) {
                    if (r == null) {
                        r = m;
                    } else {
                        r = pickBest(types, r, m);
                    }
                }
            }
            return r;
        }
    }
    
    public static Method findMethod(Object base, Object name, Object[] params) {
        Method r = null;
        if (base != null && name != null) {
            Class type = base.getClass();
            String methodName = ELSupport.coerceToString(name);
            MethodCache m = methodCache.get(type);
//            if (m == null || type != m.getType()) {
//                m = new MethodCache(type);
//                methodCache.set(type, m);
//            }
            r = m.findMethod(methodName, params);
            if (r == null) {
                throw new MethodNotFoundException(MessageFactory.get(
                        "error.method.notfound", base, name,
                        paramString(paramTypes(params))));
            }
        } else {
            throw new MethodNotFoundException();
        }
        return r;
    }
    
    /**
     * Returns a method based on the criteria
     *
     * @param base
     *            the object that owns the method
     * @param property
     *            the name of the method
     * @param paramTypes
     *            the parameter types to use
     * @return the method specified
     * @throws MethodNotFoundException
     */
    public static Method getMethod(Object base, Object property,
            Class[] paramTypes) throws MethodNotFoundException {
        if (base == null || property == null) {
            throw new MethodNotFoundException(MessageFactory.get(
                    "error.method.notfound", base, property,
                    paramString(paramTypes)));
        }
        
        String methodName = (property instanceof String) ? (String) property
                : property.toString();
        
        Method method = null;
        try {
            method = base.getClass().getMethod(methodName, paramTypes);
        } catch (NoSuchMethodException nsme) {
            throw new MethodNotFoundException(MessageFactory.get(
                    "error.method.notfound", base, property,
                    paramString(paramTypes)));
        }
        return method;
    }
    
    public static MethodInfo getMethodInfo(Object base, Object property,
            Class[] paramTypes) throws MethodNotFoundException {
        Method m = ReflectionUtil.getMethod(base, property, paramTypes);
        return new MethodInfo(m.getName(), m.getReturnType(), m
                .getParameterTypes());
    }
    
    public static MethodInfo getMethodInfo(Object base, Object property, Object[] paramValues) throws MethodNotFoundException {
        Method m = ReflectionUtil.findMethod(base, property, paramValues);
        return new MethodInfo(m.getName(), m.getReturnType(), m
                .getParameterTypes());
    }
    
    public static Object invokeMethod(Object base, Object property, Object[] paramValues) throws ELException {
        Method m = ReflectionUtil.findMethod(base, property, paramValues);
        return invokeMethod(base, m, paramValues);
    }
    
    private static final Object[] EMPTY_PARAMS = new Object[0];
    
    public static Object invokeMethod(Object base, Method m, Object[] paramValues) throws ELException {
        if (m == null) throw new MethodNotFoundException();
        
        Class[] paramTypes = m.getParameterTypes();
        Object[] params = null;
        
        if (paramTypes.length == 0) {
            // leave params null
        } else if (paramValues == null) {
            throw new MethodNotFoundException(m.getDeclaringClass() + "." + m.getName() + " has " + paramTypes.length + " params");
        } else if (m.isVarArgs()) {
            // add values
            params = new Object[paramTypes.length];
            
            int i = 0;
            for (; i < paramTypes.length - 1; i++) {
                params[i] = ELSupport.coerceToType(paramValues[i], paramTypes[i]);
            }
            
            Class argType = paramTypes[i].getComponentType();
            if (paramTypes.length == paramValues.length) {
                if (paramValues[i] == null) {
                    params[i] = Array.newInstance(argType, 0);
                } else if (paramValues[i].getClass().isArray()) {
                    params[i] = paramValues[i];
                } else {
                    params[i] = Array.newInstance(argType, 1);
                    Array.set(params[i], 0, ELSupport.coerceToType(paramValues[i], argType));
                }
            } else {
                int len = paramValues.length - paramTypes.length + 1;
                Object ar = Array.newInstance(argType, len);
                for (int j = 0; j < len; j++) {
                    Array.set(ar, j, ELSupport.coerceToType(paramValues[paramTypes.length - 1 + j], argType));
                }
                params[i] = ar;
            }
        } else if (paramValues.length == paramTypes.length) {
            // add values
            params = new Object[paramTypes.length];
            
            // assign first set
            for (int i = 0; i < paramTypes.length; i++) {
                params[i] = ELSupport.coerceToType(paramValues[i], paramTypes[i]);
            }
        } else {
            throw new MethodNotFoundException(m.getDeclaringClass().getName() + "." + m.getName() + " has " + paramTypes.length + ", only passed " + paramValues.length + " parameters");
        }
        
        try {
            return m.invoke(base, params);
        } catch (IllegalAccessException iae) {
            throw new ELException(iae);
        } catch (InvocationTargetException ite) {
            throw new ELException(ite.getCause());
        }
        
    }
    
    public static Object invokeMethod(Object base, Object property,
            Class[] paramTypes, Object[] paramValues) throws ELException,
            MethodNotFoundException {
        Method m = getMethod(base, property, paramTypes);
        return invokeMethod(base, m, paramValues);
    }
    
    protected static final String paramString(Class[] types) {
        if (types != null) {
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < types.length; i++) {
                if (types[i] != null) {
                    sb.append(types[i].getName()).append(", ");
                } else {
                    sb.append("null, ");
                }
            }
            if (sb.length() > 2) {
                sb.setLength(sb.length() - 2);
            }
            return sb.toString();
        }
        return null;
    }
    
    private static Class[] NO_TYPES = new Class[0];
    
    protected static final Class[] paramTypes(Object[] ar) {
        if (ar != null) {
            Class[] p = new Class[ar.length];
            for (int i = 0; i < ar.length; i++) {
                if (ar[i] != null) {
                    p[i] = ar[i].getClass();
                }
            }
            return p;
        }
        return NO_TYPES;
    }
}
