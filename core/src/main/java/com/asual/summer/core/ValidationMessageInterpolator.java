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

package com.asual.summer.core;

import java.util.Locale;

import javax.validation.MessageInterpolator;

import org.springframework.context.i18n.LocaleContextHolder;

import com.asual.summer.core.util.ResourceUtils;

/**
 * 
 * @author Rostislav Hristov
 *
 */
public class ValidationMessageInterpolator implements MessageInterpolator {

    public String interpolate(String message, Context context) {
        return interpolate(message, context, LocaleContextHolder.getLocale());
    }

    public String interpolate(String message, Context context, Locale locale) {
    	return ResourceUtils.getMessage(message, locale);
    }
    
}