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

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.mvc.annotation.AnnotationMethodHandlerAdapter;

/**
 * @author Rostislav Georgiev
 * 
 */
public class CustomAnnotationMethodHandlerAdapter extends AnnotationMethodHandlerAdapter {

    /**
     * 
     */
    public CustomAnnotationMethodHandlerAdapter() {
        super();
    }

    @Override
    protected ServletRequestDataBinder createBinder(HttpServletRequest request, Object target, String objectName)
            throws Exception {
        Object value = request.getAttribute(objectName);
        Object targetObject = target;
        if (null != value) {
            if (null != target && target.getClass().isInstance(value)) {
                targetObject = value;
            } else if (null == target) {
                targetObject = value;
            }
        }
        return new ServletRequestDataBinder(targetObject, objectName);
    }

}
