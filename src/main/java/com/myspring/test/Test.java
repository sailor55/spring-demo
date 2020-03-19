/*
 * Copyright (c) 2001-2020 GuaHao.com Corporation Limited. All rights reserved.
 * This software is the confidential and proprietary information of GuaHao Company.
 * ("Confidential Information").
 * You shall not disclose such Confidential Information and shall use it only
 * in accordance with the terms of the license agreement you entered into with GuaHao.com.
 */
package com.myspring.test;

import com.myspring.formwork.context.support.MyApplicationContext;

/**
 * @author ljp05
 * @version V1.0
 * @since 2020-03-17 21:26
 */
public class Test {
    public static void main(String[] args) {
        MyApplicationContext applicationContext = new MyApplicationContext("application.properties");
        System.out.println(applicationContext);
    }
}
