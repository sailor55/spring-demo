/*
 * Copyright (c) 2001-2020 GuaHao.com Corporation Limited. All rights reserved.
 * This software is the confidential and proprietary information of GuaHao Company.
 * ("Confidential Information").
 * You shall not disclose such Confidential Information and shall use it only
 * in accordance with the terms of the license agreement you entered into with GuaHao.com.
 */
package com.myspring.formwork.webmvc.servlet;

import java.io.File;
import java.util.Locale;

/**
 * @author linjp
 * @version V1.0
 * @since 2020-03-19 21:16
 */
public class MyViewResolver {

    private File templateRootDir;

    private static final String DEFAULT_TEMPLATE_SUFFER = ".html";

    public MyViewResolver(String templateRoot) {
        templateRootDir = new File(templateRoot);
    }

    MyView resolveViewName(String viewName, Locale locale) throws Exception {

        if (viewName == null || viewName.trim().equals("")) {
            return null;
        }

        if (!viewName.endsWith(DEFAULT_TEMPLATE_SUFFER)) {
            viewName = viewName + DEFAULT_TEMPLATE_SUFFER;
        }

        File templateFile = new File((templateRootDir.getPath() + "/" + viewName).replaceAll("/+", "/"));

        return new MyView(templateFile);
    }
}
