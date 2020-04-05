/*
 * Copyright (c) 2001-2020 GuaHao.com Corporation Limited. All rights reserved.
 * This software is the confidential and proprietary information of GuaHao Company.
 * ("Confidential Information").
 * You shall not disclose such Confidential Information and shall use it only
 * in accordance with the terms of the license agreement you entered into with GuaHao.com.
 */
package com.myspring.formwork.aop.aspect;

import java.lang.reflect.Method;

/**
 * @author linjp
 * @version V1.0
 * @since 2020-04-05 17:27
 */
public abstract class MyAbstractAdvice {
    private Method aspectMethod;
    private Object aspectTarget;

    public MyAbstractAdvice(Method aspectMethod, Object aspectTarget) {
        this.aspectMethod = aspectMethod;
        this.aspectTarget = aspectTarget;
    }

    public void invokeAdviceMethod(MyJointPoint jointPoint, Object returnVal, Throwable tx) throws Throwable {
        Class<?>[] paramTypes = this.aspectMethod.getParameterTypes();
        if (paramTypes == null || paramTypes.length == 0) {
            this.aspectMethod.invoke(aspectTarget);
        } else {
            Object[] args = new Object[paramTypes.length];
            for (int i = 0; i < paramTypes.length; i++) {
                if (paramTypes[i] == MyJointPoint.class) {
                    args[i] = jointPoint;
                } else if (paramTypes[i] == Throwable.class) {
                    args[i] = tx;
                } else if (paramTypes[i] == Object.class) {
                    args[i] = returnVal;
                }
            }

            this.aspectMethod.invoke(aspectTarget, args);
        }
    }
}
