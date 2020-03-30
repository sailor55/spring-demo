/*
 * Copyright (c) 2001-2020 GuaHao.com Corporation Limited. All rights reserved.
 * This software is the confidential and proprietary information of GuaHao Company.
 * ("Confidential Information").
 * You shall not disclose such Confidential Information and shall use it only
 * in accordance with the terms of the license agreement you entered into with GuaHao.com.
 */
package com.myspring.formwork.webmvc.servlet;

import java.util.Arrays;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author linjp
 * @version V1.0
 * @since 2020-03-19 19:51
 */
public class MyHandlerAdapter {

    public boolean supports(Object handler) {
        return handler instanceof MyHandlerMapping;
    }

    public MyModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object handler)
        throws Exception {
        MyHandlerMapping handlerMapping = (MyHandlerMapping) handler;
        Class<?>[] parameterTypes = handlerMapping.getMethod().getParameterTypes();
        Map<String, String[]> parameterMap = request.getParameterMap();
        Object[] parameterValues = new Object[parameterTypes.length];
        for (Map.Entry<String, String[]> param : parameterMap.entrySet()) {
            String value = Arrays.toString(param.getValue()).replaceAll("\\[|\\]", "").replaceAll("\\s", ",");
            if (!handlerMapping.getParamIndexMap().containsKey(param.getKey())) {
                continue;
            }
            Integer index = handlerMapping.getParamIndexMap().get(param.getKey());
            parameterValues[index] = caseStringValue(parameterTypes[index], value);
        }

        if (handlerMapping.getParamIndexMap().containsKey(HttpServletRequest.class.getName())) {
            int reqIndex = handlerMapping.getParamIndexMap().get(HttpServletRequest.class.getName());
            parameterValues[reqIndex] = request;
        }

        if (handlerMapping.getParamIndexMap().containsKey(HttpServletResponse.class.getName())) {
            int responseIndex = handlerMapping.getParamIndexMap().get(HttpServletResponse.class.getName());
            parameterValues[responseIndex] = response;
        }

        Object result = handlerMapping.getMethod().invoke(handlerMapping.getController(), parameterValues);
        if (result == null || result instanceof Void) {
            return null;
        }
        if (result instanceof MyModelAndView) {
            return (MyModelAndView) result;
        }

        return null;
    }

    private Object caseStringValue(Class<?> parameterType, String value) {
        if (value == null) {
            return value;
        }
        if (parameterType == Integer.class) {
            return Integer.parseInt(value);
        }
        return value.toString();
    }
}
