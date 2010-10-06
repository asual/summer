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
import java.util.List;
import java.util.Map;

import javax.inject.Named;

/**
 * 
 * @author Rostislav Hristov
 *
 */
@Named
public class MapUtils {

	public static List<Object> getKeysFromValue(Map<?, ?> m, Object value) {
		List<Object> list = new ArrayList<Object>();
		for (Object o : m.keySet()){
			if(m.get(o).equals(value)) {
				list.add(o);
			}
		}
		return list;
	}

    public static void put(Map<Object, Object> map, Object key, Object value) {
		map.put(key, value);
	}
    
}
