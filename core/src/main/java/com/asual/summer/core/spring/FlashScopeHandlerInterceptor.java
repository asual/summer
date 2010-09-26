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

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.asual.summer.core.spring.ScopeAwareModelMap.Scope;

/**
 * @author Rostislav Georgiev
 * 
 */
public class FlashScopeHandlerInterceptor extends HandlerInterceptorAdapter {

    private static final String FLASH_SCOPE_KEY = Scope.FLASH.getKey();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession(false);
        if (session != null) {
            ModelMap flashMap = (ModelMap) session.getAttribute(FLASH_SCOPE_KEY);
            if (null != flashMap) {
                for (Map.Entry<String, ?> entry : flashMap.entrySet()) {
                    if (null == request.getAttribute(entry.getKey())) {
                        request.setAttribute(entry.getKey(), entry.getValue());
                    }
                }
                request.setAttribute(FLASH_SCOPE_KEY, flashMap);
                if (flashMap.isEmpty()) {
                    session.removeAttribute(FLASH_SCOPE_KEY);
                }
            }
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
            ModelAndView modelAndView) throws Exception {

        if (modelAndView != null) {
            ModelMap flashScopeMap = null;
            if (modelAndView.getModelMap() instanceof ScopeAwareModelMap) {
                ScopeAwareModelMap modelMap = (ScopeAwareModelMap) modelAndView.getModelMap();
                flashScopeMap = modelMap.getAttributes(Scope.FLASH);
            } else {
                flashScopeMap = (ModelMap) modelAndView.getModelMap().get(FLASH_SCOPE_KEY);
            }
            if (null != flashScopeMap) {
                request.setAttribute(FLASH_SCOPE_KEY, flashScopeMap);
            } else {
                request.removeAttribute(FLASH_SCOPE_KEY);
            }

        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
            throws Exception {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.removeAttribute(FLASH_SCOPE_KEY);
        }
        ModelMap attributes = (ModelMap) request.getAttribute(FLASH_SCOPE_KEY);
        if (null != attributes) {
            if (!attributes.isEmpty()) {
                request.getSession(true).setAttribute(FLASH_SCOPE_KEY, attributes);
            }
        }
    }

}
