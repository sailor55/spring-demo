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
 * @since 2020-04-05 17:15
 */
public class MyAfterThrowingAdviceInterceptor extends MyAbstractAdvice implements MyMethodInterceptor {

    private String throwName;

    private MyJointPoint myJointPoint;

    public MyAfterThrowingAdviceInterceptor(Method aspectMethod, Object aspectTarget) {
        super(aspectMethod, aspectTarget);
    }

    public String getThrowName() {
        return throwName;
    }

    public void setThrowName(String throwName) {
        this.throwName = throwName;
    }

    @Override
    public Object invoke(MyMethodInvocation invocation) throws Throwable {
        this.myJointPoint = invocation;
        Object returnVal = null;
        try {
            returnVal = invocation.proceed();
        } catch (Throwable e) {
            super.invokeAdviceMethod(myJointPoint, returnVal, e.getCause());
            throw e;
        }
        return returnVal;
    }
}
