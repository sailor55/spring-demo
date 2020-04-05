/*
 * Copyright (c) 2001-2020 GuaHao.com Corporation Limited. All rights reserved.
 * This software is the confidential and proprietary information of GuaHao Company.
 * ("Confidential Information").
 * You shall not disclose such Confidential Information and shall use it only
 * in accordance with the terms of the license agreement you entered into with GuaHao.com.
 */
package com.myspring.formwork.aop.support;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.myspring.formwork.aop.aspect.MyAfterReturningAdviceInterceptor;
import com.myspring.formwork.aop.aspect.MyAfterThrowingAdviceInterceptor;
import com.myspring.formwork.aop.aspect.MyMethodBeforeAdviceInterceptor;
import com.myspring.formwork.aop.config.MyAopConfig;

/**
 * @author linjp
 * @version V1.0
 * @since 2020-04-04 17:14
 */
public class MyAdvisedSupport {

    private Class<?> targetClass;

    private Object target;

    private MyAopConfig config;

    private Pattern pointCutClassPattern;

    private transient Map<Method, List<Object>> methodCache;

    public MyAdvisedSupport(MyAopConfig config) {
        this.config = config;
    }

    public Class<?> getTargetClass() {
        return this.targetClass;
    }

    public List<Object> getInterceptorsAndDynamicInterceptionAdvice(Method method, Class<?> targetClass)
            throws Exception {
        List<Object> cached = methodCache.get(method);

        if (cached == null) {
            Method m = targetClass.getMethod(method.getName(), method.getParameterTypes());
            cached = methodCache.get(m);
        }

        return cached;
    }

    public Object getTarget() {
        return target;
    }

    public void setTargetClass(Class<?> targetClass) {
        this.targetClass = targetClass;
        parse();
    }

    private void parse() {
        String pointCut = config.getPointCut();

        String pointCutForClassRegex = pointCut.substring(0, pointCut.lastIndexOf("(") - 3);
        pointCutClassPattern = Pattern
                .compile("class " + pointCutForClassRegex.substring(pointCutForClassRegex.lastIndexOf(" ") + 1));
        try {
            methodCache = new HashMap<>();
            Pattern pattern = Pattern.compile(pointCut);

            Class<?> aspectClass = Class.forName(this.config.getAspectClass());

            Map<String, Method> aspectMethods = new HashMap<>();

            for (Method method : aspectClass.getMethods()) {
                aspectMethods.put(method.getName(), method);
            }

            for (Method m : this.targetClass.getMethods()) {
                String methodString = m.toString();
                if (methodString.contains("throws")) {
                    methodString = methodString.substring(0, methodString.lastIndexOf("throws"));
                }
                Matcher matcher = pattern.matcher(methodString);
                if (matcher.matches()) {
                    // 把每一个方法包装成methodInterceptor
                    List<Object> advices = new LinkedList<>();

                    if (!(null == config.getAspectBefore() || "".equals(config.getAspectBefore()))) {
                        advices.add(new MyMethodBeforeAdviceInterceptor(aspectMethods.get(config.getAspectBefore()),
                                aspectClass.newInstance()));
                    }

                    if (!(null == config.getAspectAfter() || "".equals(config.getAspectAfter()))) {
                        advices.add(new MyAfterReturningAdviceInterceptor(aspectMethods.get(config.getAspectAfter()),
                                aspectClass.newInstance()));
                    }

                    if (!(null == config.getAspectAfterThrow() || "".equals(config.getAspectAfterThrow()))) {
                        MyAfterThrowingAdviceInterceptor myAfterThrowingAdviceInterceptor = new MyAfterThrowingAdviceInterceptor(
                                aspectMethods.get(config.getAspectAfterThrow()), aspectClass.newInstance());
                        myAfterThrowingAdviceInterceptor.setThrowName(config.getAspectAfterThrowingName());
                        advices.add(myAfterThrowingAdviceInterceptor);
                    }
                    methodCache.put(m, advices);

                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void setTarget(Object target) {
        this.target = target;
    }

    public boolean pointCutMatch() {
        return pointCutClassPattern.matcher(this.targetClass.toString()).matches();
    }

    public static void main(String[] args) throws Exception {
        Pattern pattern = Pattern.compile("public .* com.myspring.mvc..*Service..*(.*)");
        Matcher matcher = pattern.matcher(
                "public java.lang.Integer com.myspring.mvc.QueryService.add(java.lang.Integer,java.lang.Integer)");
        System.out.println(matcher.matches());
    }
}
