/*
 * Copyright (c) 2001-2020 GuaHao.com Corporation Limited. All rights reserved.
 * This software is the confidential and proprietary information of GuaHao Company.
 * ("Confidential Information").
 * You shall not disclose such Confidential Information and shall use it only
 * in accordance with the terms of the license agreement you entered into with GuaHao.com.
 */
package com.myspring.formwork.beans.support;

import com.myspring.formwork.annotation.MyAutoWired;
import com.myspring.formwork.annotation.MyController;
import com.myspring.formwork.annotation.MyService;
import com.myspring.formwork.beans.MyBeanFactory;
import com.myspring.formwork.beans.MyBeanWrapper;
import com.myspring.formwork.beans.config.MyBeanDefinition;
import com.myspring.formwork.context.support.MyAbstractApplicationContext;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author linjp
 * @version V1.0
 * @since 2020-03-16 20:12
 */
public class MyDefaultListableBeanFactory extends MyAbstractApplicationContext implements MyBeanFactory {

    public Map<String, MyBeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>(256);

    private final Map<String, MyBeanWrapper> factoryBeanInstanceCache = new ConcurrentHashMap<>(16);

    private final Map<String, Object> singletonObjects = new ConcurrentHashMap<>(16);

    @Override
    public Object getBean(String beanName) {
        MyBeanDefinition myBeanDefinition = beanDefinitionMap.get(beanName);
        //1.初始化
        MyBeanWrapper myBeanWrapper = instantiateBean(beanName, myBeanDefinition);
        //2.将BeanWrapper放置到ioc容器中
        factoryBeanInstanceCache.put(beanName, myBeanWrapper);
        //3.注入
        populateBean(beanName, myBeanDefinition, myBeanWrapper);
        return myBeanWrapper.getWrappedInstance();
    }

    private MyBeanWrapper instantiateBean(String beanName, MyBeanDefinition myBeanDefinition) {
        String className = myBeanDefinition.getBeanClassName();
        Object instance = null;
        if (singletonObjects.containsKey(className)) {
            instance = singletonObjects.get(className);
        } else {
            try {
                instance = Class.forName(className).newInstance();
                singletonObjects.put(className, instance);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return new MyBeanWrapper(instance);
    }

    private void populateBean(String beanName, MyBeanDefinition myBeanDefinition, MyBeanWrapper myBeanWrapper) {
        Class<?> wrappedClass = myBeanWrapper.getWrappedClass();
        if (!wrappedClass.isAnnotationPresent(MyController.class) && !wrappedClass
            .isAnnotationPresent(MyService.class)) {
            return;
        }

        Field[] fields = wrappedClass.getDeclaredFields();

        for (Field field : fields) {
            if (!field.isAnnotationPresent(MyAutoWired.class)) {
                continue;
            }

            MyAutoWired annotation = field.getAnnotation(MyAutoWired.class);
            String autoWiredName = annotation.value().trim();
            if ("".equals(autoWiredName)) {
                autoWiredName = field.getType().getName();
            }
            field.setAccessible(true);

            try {
                field.set(myBeanWrapper.getWrappedInstance(),
                    factoryBeanInstanceCache.get(autoWiredName).getWrappedInstance());
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }

        }

    }

}
