/*
 * Copyright (c) 2001-2020 GuaHao.com Corporation Limited. All rights reserved.
 * This software is the confidential and proprietary information of GuaHao Company.
 * ("Confidential Information").
 * You shall not disclose such Confidential Information and shall use it only
 * in accordance with the terms of the license agreement you entered into with GuaHao.com.
 */
package com.myspring.formwork.beans.support;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.myspring.formwork.annotation.MyAutoWired;
import com.myspring.formwork.annotation.MyController;
import com.myspring.formwork.annotation.MyService;
import com.myspring.formwork.aop.MyAopProxy;
import com.myspring.formwork.aop.MyCglibAopProxy;
import com.myspring.formwork.aop.MyJdkDynamicAopProxy;
import com.myspring.formwork.aop.config.MyAopConfig;
import com.myspring.formwork.aop.support.MyAdvisedSupport;
import com.myspring.formwork.beans.MyBeanFactory;
import com.myspring.formwork.beans.MyBeanWrapper;
import com.myspring.formwork.beans.config.MyBeanDefinition;
import com.myspring.formwork.context.support.MyAbstractApplicationContext;

/**
 * @author linjp
 * @version V1.0
 * @since 2020-03-16 20:12
 */
public class MyDefaultListableBeanFactory extends MyAbstractApplicationContext implements MyBeanFactory {

    public Map<String, MyBeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>(256);

    private final Map<String, MyBeanWrapper> factoryBeanInstanceCache = new ConcurrentHashMap<>(16);

    private final Map<String, Object> singletonObjects = new ConcurrentHashMap<>(16);

    private final Map<String, Object> singletonFactories = new ConcurrentHashMap<>(16);

    protected MyBeanDefinitionReader reader;

    protected String[] configLocations;

    @Override
    public Object getBean(String beanName) {
        if (singletonObjects.containsKey(beanName)) {
            return singletonObjects.get(beanName);
        }
        return doCreateBean(beanName);
    }

    private Object doCreateBean(String beanName) {
        MyBeanDefinition myBeanDefinition = beanDefinitionMap.get(beanName);
        // 1.初始化
        Object instance = instantiateBean(beanName, myBeanDefinition);
        // 把对象封装到beanWrapper
        MyBeanWrapper myBeanWrapper = new MyBeanWrapper(instance);
        // 2.将BeanWrapper放置到ioc容器中
        factoryBeanInstanceCache.put(beanName, myBeanWrapper);
        // 3.注入
        populateBean(beanName, myBeanDefinition, myBeanWrapper);
        singletonObjects.put(beanName, myBeanWrapper.getWrappedInstance());
        return myBeanWrapper.getWrappedInstance();
    }

    private Object instantiateBean(String beanName, MyBeanDefinition myBeanDefinition) {
        String className = myBeanDefinition.getBeanClassName();
        Object instance = null;
        if (singletonObjects.containsKey(beanName)) {
            instance = singletonObjects.get(beanName);
        } else {
            try {
                Class<?> targetClass = Class.forName(className);
                instance = targetClass.newInstance();
                MyAdvisedSupport config = instanceAopConfig();
                config.setTarget(instance);
                config.setTargetClass(targetClass);
                // 符合切面规则的类则创建代理对象
                if (config.pointCutMatch()) {
                    instance = createProxy(config).getProxy();
                }
                singletonFactories.put(beanName, instance);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return instance;
    }

    private MyAopProxy createProxy(MyAdvisedSupport config) {
        Class<?> targetClass = config.getTargetClass();
        if (targetClass.getInterfaces().length > 0) {
            return new MyJdkDynamicAopProxy(config);
        }
        return new MyCglibAopProxy(config);
    }

    private MyAdvisedSupport instanceAopConfig() {
        MyAopConfig config = new MyAopConfig();
        config.setPointCut(this.reader.getConfig().getProperty("pointCut"));
        config.setAspectClass(this.reader.getConfig().getProperty("aspectClass"));
        config.setAspectBefore(this.reader.getConfig().getProperty("aspectBefore"));
        config.setAspectAfter(this.reader.getConfig().getProperty("aspectAfter"));
        config.setAspectAfterThrow(this.reader.getConfig().getProperty("aspectAfterThrow"));
        config.setAspectAfterThrowingName(this.reader.getConfig().getProperty("aspectAfterThrowingName"));
        return new MyAdvisedSupport(config);
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
                Object myAutoWiredBean = null;
                if (singletonFactories.containsKey(autoWiredName)) {
                    myAutoWiredBean = singletonFactories.get(autoWiredName);
                } else {
                    myAutoWiredBean = getBean(autoWiredName);
                }

                field.set(myBeanWrapper.getWrappedInstance(), myAutoWiredBean);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }

        }

    }

    public int getBeanDefinitionCount() {
        return beanDefinitionMap.size();
    }

    public String[] getBeanDefinitionNames() {
        return beanDefinitionMap.keySet().toArray(new String[beanDefinitionMap.keySet().size()]);
    }
}
