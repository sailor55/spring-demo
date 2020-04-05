/*
 * Copyright (c) 2001-2020 GuaHao.com Corporation Limited. All rights reserved.
 * This software is the confidential and proprietary information of GuaHao Company.
 * ("Confidential Information").
 * You shall not disclose such Confidential Information and shall use it only
 * in accordance with the terms of the license agreement you entered into with GuaHao.com.
 */
package com.myspring.formwork.aop.intercept;

import java.lang.reflect.Method;
import java.util.List;

import com.myspring.formwork.aop.aspect.MyJointPoint;

/**
 * @author linjp
 * @version V1.0
 * @since 2020-04-05 14:36
 */
public class MyMethodInvocation implements MyJointPoint {

    private Object proxy;
    private Object target;
    private Method method;
    private Object[] arguments;
    private Class<?> targetClass;
    private List<Object> interceptorsAndDynamicMethodMatchers;

    private int currentInterceptorIndex = -1;

    public MyMethodInvocation(Object proxy, Object target, Method method, Object[] arguments, Class<?> targetClass,
            List<Object> interceptorsAndDynamicMethodMatchers) {

        this.proxy = proxy;
        this.target = target;
        this.targetClass = targetClass;
        this.method = method;
        this.arguments = arguments;
        this.interceptorsAndDynamicMethodMatchers = interceptorsAndDynamicMethodMatchers;
    }

    public Object proceed() throws Throwable {
        if (this.currentInterceptorIndex == this.interceptorsAndDynamicMethodMatchers.size() - 1) {
            return this.method.invoke(this.target, this.arguments);
        }

        Object interceptorOrInterceptionAdvice = this.interceptorsAndDynamicMethodMatchers
                .get(++this.currentInterceptorIndex);
        if (interceptorOrInterceptionAdvice instanceof MyMethodInterceptor) {
            MyMethodInterceptor dm = (MyMethodInterceptor) interceptorOrInterceptionAdvice;
            return dm.invoke(this);
        } else {
            return proceed();
        }

    }

    @Override
    public Method getMethod() {
        return this.method;
    }

    @Override
    public Object[] getArgs() {
        return this.arguments;
    }

    @Override
    public Object getTarget() {
        return this.target;
    }
}
