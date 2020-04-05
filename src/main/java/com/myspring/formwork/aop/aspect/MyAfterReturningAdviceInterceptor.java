/*
 * Copyright (c) 2001-2020 GuaHao.com Corporation Limited. All rights reserved.
 * This software is the confidential and proprietary information of GuaHao Company.
 * ("Confidential Information").
 * You shall not disclose such Confidential Information and shall use it only
 * in accordance with the terms of the license agreement you entered into with GuaHao.com.
 */
package com.myspring.formwork.aop.aspect;

import java.lang.reflect.Method;

import com.myspring.formwork.aop.intercept.MyMethodInterceptor;
import com.myspring.formwork.aop.intercept.MyMethodInvocation;

/**
 * @author linjp
 * @version V1.0
 * @since 2020-04-05 17:13
 */
public class MyAfterReturningAdviceInterceptor extends MyAbstractAdvice implements MyMethodInterceptor {

    private MyJointPoint myJointPoint;

    public MyAfterReturningAdviceInterceptor(Method aspectMethod, Object aspectTarget) {
        super(aspectMethod, aspectTarget);
    }

    @Override
    public Object invoke(MyMethodInvocation invocation) throws Throwable {
        this.myJointPoint = invocation;
        Object returnVal = invocation.proceed();
        super.invokeAdviceMethod(myJointPoint, returnVal, null);
        return returnVal;
    }
}
