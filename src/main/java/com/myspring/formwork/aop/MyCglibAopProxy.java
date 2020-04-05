/*
 * Copyright (c) 2001-2020 GuaHao.com Corporation Limited. All rights reserved.
 * This software is the confidential and proprietary information of GuaHao Company.
 * ("Confidential Information").
 * You shall not disclose such Confidential Information and shall use it only
 * in accordance with the terms of the license agreement you entered into with GuaHao.com.
 */
package com.myspring.formwork.aop;

import java.lang.reflect.Method;
import java.util.List;

import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;

import com.myspring.formwork.aop.intercept.MyMethodInvocation;
import com.myspring.formwork.aop.support.MyAdvisedSupport;

/**
 * @author linjp
 * @version V1.0
 * @since 2020-04-04 17:05
 */
public class MyCglibAopProxy implements MyAopProxy, MethodInterceptor {

    private MyAdvisedSupport advised;

    public MyCglibAopProxy(MyAdvisedSupport config) {
        this.advised = config;
    }

    @Override
    public Object getProxy() {
        return getProxy(null);

    }

    @Override
    public Object getProxy(ClassLoader classLoader) {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(this.advised.getTargetClass());
        enhancer.setCallback(this);
        return enhancer.create();
    }

    @Override
    public Object intercept(Object proxy, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
        List<Object> chain = this.advised.getInterceptorsAndDynamicInterceptionAdvice(method,
                this.advised.getTargetClass());

        MyMethodInvocation invocation = new MyMethodInvocation(proxy, this.advised.getTarget(), method, args,
                this.advised.getTargetClass(), chain);
        return invocation.proceed();
    }
}
