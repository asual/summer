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

package com.asual.summer.core.util;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Named;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.WordUtils;

/**
 * 
 * @author Rostislav Hristov
 *
 */
@Named
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
	
	public static String lines(String str, int length) {
		List<String> values = Arrays.asList(str.split("\n\r|\r\n|\n|\r"));
		if (length < values.size()) {
			return join(values.subList(0, length).toArray(new String[] {}), "\n") + "...";
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
			return URLEncoder.encode(value, getEncoding());
		} catch (Exception e) {
			return value;
		}
	}

	public static String decode(String value) {
		try {
			return URLDecoder.decode(value, getEncoding());
		} catch (Exception e) {
			return value;
		}
	}

	public static String escape(String value) {
		try {
			return StringEscapeUtils.escapeHtml(value);
		} catch (Exception e) {
			return value;
		}
	}

	public static String unescape(String value) {
		try {
			return StringEscapeUtils.unescapeHtml(value);
		} catch (Exception e) {
			return value;
		}
	}
	
	public static String decorate(String str, Map<String, String> values) {
		Matcher m = Pattern.compile("\\$\\{[^}]*\\}").matcher(str);
		while (m.find()) {
			String key = m.group().replaceAll("^\\$\\{|\\}$", "");
			String value = values.get(key);
			if (value == null) {
				value = "";
			}
			str = str.replaceAll("\\$\\{" + key + "\\}", 
					value.replaceAll("\\\\", "\\\\\\\\").replaceAll("\"", "\\\\\""));
		}
		return str;
	}	
	
	public static String getEncoding() {
		return ENCODING;
	}
}