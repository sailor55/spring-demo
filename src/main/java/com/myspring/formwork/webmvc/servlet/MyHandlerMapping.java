/*
 * Copyright (c) 2001-2020 GuaHao.com Corporation Limited. All rights reserved.
 * This software is the confidential and proprietary information of GuaHao Company.
 * ("Confidential Information").
 * You shall not disclose such Confidential Information and shall use it only
 * in accordance with the terms of the license agreement you entered into with GuaHao.com.
 */
package com.myspring.formwork.webmvc.servlet;

import com.myspring.formwork.annotation.MyRequestParam;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author linjp
 * @version V1.0
 * @since 2020-03-19 19:40
 */
public class MyHandlerMapping {
    private Pattern pattern;
    private Method method;
    private Object controller;
    private Map<String, Integer> paramIndexMap;

    public MyHandlerMapping(Pattern pattern, Method method, Object controller) {
        this.pattern = pattern;
        this.method = method;
        this.controller = controller;
        paramIndexMap = new HashMap<>();
        putParam(method);
    }

    public Pattern getPattern() {
        return pattern;
    }

    public Method getMethod() {
        return method;
    }

    public Object getController() {
        return controller;
    }

    public Map<String, Integer> getParamIndexMap() {
        return paramIndexMap;
    }

    private void putParam(Method method) {
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        for (int i = 0; i < parameterAnnotations.length; i++) {
            for (Annotation annotation : parameterAnnotations[i]) {
                if (annotation instanceof MyRequestParam) {
                    String paramName = ((MyRequestParam) annotation).name();
                    paramIndexMap.put(paramName, i);
                }
            }

        }

        Class<?>[] parameterTypes = method.getParameterTypes();
        for (int i = 0; i < parameterTypes.length; i++) {
            Class<?> type = parameterTypes[i];
            if (type == HttpServletRequest.class || type == HttpServletResponse.class) {
                paramIndexMap.put(type.getName(), i);
            }
        }

    }
}
