package com.asual.summer.sample.convert;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import com.asual.summer.sample.domain.License;

@Component
public class StringToLicenseConvertor implements Converter<String, License> {
    
    public License convert(String source) {
		try {
	    	return License.find(source);
		} catch (Exception e) {
			return null;
		}
    }

}