package com.asual.summer.sample.convert;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import com.asual.summer.core.util.StringUtils;
import com.asual.summer.sample.domain.License;

@Component
public class StringToLicenseConvertor implements Converter<String, License> {
    
    public License convert(String source) {
    	if (StringUtils.isEmpty(source)) {
            return null;
    	}
        return License.findLicense(Integer.parseInt(source));
    }

}