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

import java.util.LinkedHashSet;
import java.util.Map;

import org.springframework.context.support.ConversionServiceFactoryBean;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterRegistry;
import org.springframework.core.convert.support.ConversionServiceFactory;

import com.asual.summer.core.util.BeanUtils;

public class ConversionDiscoveryFactoryBean extends ConversionServiceFactoryBean {

	@SuppressWarnings("rawtypes")
	private Map<String, Converter> cachedConverters;

	@SuppressWarnings("rawtypes")
	public ConversionService getObject() {
		ConversionService conversionService = super.getObject();
		if (conversionService instanceof ConverterRegistry) {
			ConverterRegistry registry = (ConverterRegistry) conversionService;
			if (cachedConverters == null) {
				cachedConverters = BeanUtils.getBeansOfType(Converter.class);
				ConversionServiceFactory.registerConverters(new LinkedHashSet<Converter>(cachedConverters.values()),
						registry);
			}
		}
		return conversionService;
	}

}