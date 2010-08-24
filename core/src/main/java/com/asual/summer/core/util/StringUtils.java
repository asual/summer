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

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.WordUtils;
import org.springframework.stereotype.Component;

@Component
public class StringUtils {

	private static String ENCODING = "UTF-8";
	
    public static String ellipsis(String str, int length) {
    	String space = " ";
    	String ellipsis = "...";
    	length = length - ellipsis.length();
    	if (str.length() > length) {
    		String[] words = str.substring(0, length).split(space);
			if (words.length > 1) {
    			str = join(ArrayUtils.remove(words, words.length - 1), space);
			} else {
				str = words[0];
			}
			String end = str.substring(str.length() - 1, str.length());
			if (end.matches("\\.|\\?|\\!|,")) {
				str = str.substring(0, str.length() - 1);
			}
			return str + ellipsis;
    	}
    	return str;
    }

    public static String wrap(String left, String center, String right) {
    	return left.concat(center).concat(right);
    }

    public static String toCamelCase(String str) {
    	return WordUtils.uncapitalize(str);
    }

    public static String toTitleCase(String str) {
    	return WordUtils.capitalize(str);
    }

    public static String toURIPath(String str) {
		return StringUtils.join(str.replaceAll("'|\\\"|\\.|,|;", "").split(" "), "-").toLowerCase();
    }

    public static boolean isEmpty(String str) {
        return org.apache.commons.lang.StringUtils.isEmpty(str);
    }
    
    public static String join(Object[] array, String str) {
    	return org.apache.commons.lang.StringUtils.join(array, str);
    }
    
    public static String join(List<String> list, String str) {
    	return org.apache.commons.lang.StringUtils.join(list.toArray(), str);
    }
    
    public static boolean containsIgnoreCase(String name, String searchString) {
        if (name == null) {
            name = "";
        }
        if (searchString == null) {
            searchString = "";
        }
        return name.toLowerCase().contains(searchString.toLowerCase());
    }

	public static String encode(String value) {
	    try {
	        return URLEncoder.encode(value, ENCODING);
	    } catch (Exception e) {
	    	return value;
	    }
	}

	public static String decode(String value) {
	    try {
	        return URLDecoder.decode(value, ENCODING);
	    } catch (Exception e) {
	    	return value;
	    }
	}     
}