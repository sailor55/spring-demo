/*
 * Copyright (c) 2001-2020 GuaHao.com Corporation Limited. All rights reserved.
 * This software is the confidential and proprietary information of GuaHao Company.
 * ("Confidential Information").
 * You shall not disclose such Confidential Information and shall use it only
 * in accordance with the terms of the license agreement you entered into with GuaHao.com.
 */
package com.myspring.formwork.beans.config;

import lombok.Data;

/**
 * @author linjp
 * @version V1.0
 * @since 2020-03-16 20:09
 */
@Data
public class MyBeanDefinition {

    private boolean lazyInit;

    private String beanClassName;

    String factoryBeanName;
}
