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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Named;

/**
 * 
 * @author Rostislav Hristov
 *
 */
@Named
public class ArrayUtils {
	
    public static <T> T first(T[] obj) {
		if (obj.length != 0) {
			return (T) obj[0];
		}
    	return null;
	}
    
    public static <T> T last(T[] obj) {
		if (obj.length != 0) {
			return (T) obj[obj.length - 1];
		}
    	return null;
	}
    
    public static List<?> asList(Object obj) {
    	if (obj instanceof List) {
    		return (List<?>) obj;
    	} else if (obj instanceof Object[]) {
        	List<Object> list = new ArrayList<Object>();
        	Object[] arr = (Object []) obj;
        	for (Object o : arr) {
        		list.add(o);
        	}
    		return list;
    	} else if (obj instanceof String) {
        	List<Object> list = new ArrayList<Object>();
        	Object[] arr = ((String) obj).split(",");
        	for (Object o : arr) {
        		list.add(o);
        	}
    		return list;
    	} else if (obj instanceof Collection<?>) {
    		return  new ArrayList<Object>(((Collection<?>) obj));
    	}
    	return null;
    }

}
