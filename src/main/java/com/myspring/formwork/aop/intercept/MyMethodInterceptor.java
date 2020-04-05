/*
 * Copyright (c) 2001-2020 GuaHao.com Corporation Limited. All rights reserved.
 * This software is the confidential and proprietary information of GuaHao Company.
 * ("Confidential Information").
 * You shall not disclose such Confidential Information and shall use it only
 * in accordance with the terms of the license agreement you entered into with GuaHao.com.
 */
package com.myspring.formwork.aop.intercept;

/**
 * @author linjp
 * @version V1.0
 * @since 2020-04-05 14:48
 */
public interface MyMethodInterceptor {

    Object invoke(MyMethodInvocation invocation) throws Throwable;
}
