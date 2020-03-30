/*
 * Copyright (c) 2001-2020 GuaHao.com Corporation Limited. All rights reserved.
 * This software is the confidential and proprietary information of GuaHao Company.
 * ("Confidential Information").
 * You shall not disclose such Confidential Information and shall use it only
 * in accordance with the terms of the license agreement you entered into with GuaHao.com.
 */
package com.myspring.test;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

/**
 * @author ljp05
 * @version V1.0
 * @since 2020-03-30 20:18
 */
@Component
public class ClassA {
    @Resource
    private ClassB classB;
}
