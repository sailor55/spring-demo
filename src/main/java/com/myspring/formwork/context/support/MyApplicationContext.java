/*
 * Copyright (c) 2001-2020 GuaHao.com Corporation Limited. All rights reserved.
 * This software is the confidential and proprietary information of GuaHao Company.
 * ("Confidential Information").
 * You shall not disclose such Confidential Information and shall use it only
 * in accordance with the terms of the license agreement you entered into with GuaHao.com.
 */
package com.myspring.formwork.context.support;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.myspring.formwork.beans.config.MyBeanDefinition;
import com.myspring.formwork.beans.support.MyBeanDefinitionReader;
import com.myspring.formwork.beans.support.MyDefaultListableBeanFactory;

/**
 * @author linjp
 * @version V1.0
 * @since 2020-03-16 20:10
 */
public class MyApplicationContext extends MyDefaultListableBeanFactory {

    public MyApplicationContext(String... configLocations) {
        this.configLocations = configLocations;
        refresh();
    }

    @Override
    public void refresh() {
        //1:定位
        reader = new MyBeanDefinitionReader(this.configLocations);
        //2:加载
        List<MyBeanDefinition> beanDefinitions = reader.loadBeanDefinitions();
        //3:注册
        doRegisterBeanDifination(beanDefinitions);
        //4:注入
        doAutoWired();
    }

    private void doAutoWired() {
        for (Map.Entry<String, MyBeanDefinition> beanDefinitionEntry : super.beanDefinitionMap.entrySet()) {
            if (!beanDefinitionEntry.getValue().isLazyInit()) {
                getBean(beanDefinitionEntry.getKey());
            }

        }
    }

    private void doRegisterBeanDifination(List<MyBeanDefinition> beanDefinitions) {
        for (MyBeanDefinition beanDefinition : beanDefinitions) {
            super.beanDefinitionMap.put(toLowerFirstCase(beanDefinition.getFactoryBeanName()), beanDefinition);
        }

    }

    public Properties getConfig() {
        return reader.getConfig();
    }

    private String toLowerFirstCase(String beanName) {
        char[] chars = beanName.toCharArray();
        chars[0] += 32;
        return String.valueOf(chars);
    }
}
