/*
 * Copyright (c) 2001-2020 GuaHao.com Corporation Limited. All rights reserved.
 * This software is the confidential and proprietary information of GuaHao Company.
 * ("Confidential Information").
 * You shall not disclose such Confidential Information and shall use it only
 * in accordance with the terms of the license agreement you entered into with GuaHao.com.
 */
package com.myspring.formwork.beans.support;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.myspring.formwork.beans.config.MyBeanDefinition;

/**
 * @author linjp
 * @version V1.0
 * @since 2020-03-16 20:44
 */
public class MyBeanDefinitionReader {

    private Properties config = new Properties();

    private List<String> classNames = new ArrayList<>();

    private final String SCAN_PACKAGE = "scanPackage";

    public MyBeanDefinitionReader(String... locations) {
        doLoadConfiguration(locations[0]);
        doScanner(config.getProperty(SCAN_PACKAGE));
    }

    public List<MyBeanDefinition> loadBeanDefinitions() {
        List<MyBeanDefinition> result = new ArrayList<>();
        for (String className : classNames) {
            MyBeanDefinition myBeanDefinition = doCreateBeanDefinition(className);
            if (myBeanDefinition == null) {
                continue;
            }
            result.add(myBeanDefinition);
        }

        return result;
    }

    private MyBeanDefinition doCreateBeanDefinition(String className) {
        try {
            Class<?> beanClass = Class.forName(className);
            if (beanClass.isInterface()) {
                return null;
            }
            MyBeanDefinition myBeanDefinition = new MyBeanDefinition();
            myBeanDefinition.setBeanClassName(className);
            myBeanDefinition.setFactoryBeanName(beanClass.getSimpleName());
            return myBeanDefinition;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void doScanner(String scanPackage) {
        // 获取到了com.myspring转成/com/myspring
        String bastPath = "/" + scanPackage.replaceAll("\\.", "/");
        URL url = this.getClass().getResource(bastPath);
        File classPath = new File(url.getFile());
        for (File file : classPath.listFiles()) {

            if (file.isDirectory()) {
                doScanner(scanPackage + "." + file.getName());
                continue;
            }

            if (!file.getName().endsWith(".class")) {
                continue;
            }

            String className = scanPackage + "." + file.getName().replace(".class", "");
            classNames.add(className);

        }

    }

    private void doLoadConfiguration(String contextConfigLocation) {
        InputStream fis = this.getClass().getClassLoader().getResourceAsStream(contextConfigLocation);
        try {
            config.load(fis);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }



    public Properties getConfig() {
        return config;
    }
}
