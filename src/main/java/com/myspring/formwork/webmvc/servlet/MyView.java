/*
 * Copyright (c) 2001-2020 GuaHao.com Corporation Limited. All rights reserved.
 * This software is the confidential and proprietary information of GuaHao Company.
 * ("Confidential Information").
 * You shall not disclose such Confidential Information and shall use it only
 * in accordance with the terms of the license agreement you entered into with GuaHao.com.
 */
package com.myspring.formwork.webmvc.servlet;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author linjp
 * @version V1.0
 * @since 2020-03-19 21:17
 */
public class MyView {

    public final String DEFAULT_CONTENT_TYPE = "test/html;charset=utf-8";

    String varPattern = "￥\\{[^\\}]+\\}";

    private File viewFile;

    public MyView(File viewFile) {
        this.viewFile = viewFile;
    }

    void render(Map<String, ?> model, HttpServletRequest request, HttpServletResponse response) throws Exception {
        StringBuffer sb = new StringBuffer();
        RandomAccessFile randomAccessFile = new RandomAccessFile(this.viewFile, "r");
        String line = null;
        while ((line = randomAccessFile.readLine()) != null) {
            line = new String(line.getBytes("ISO-8859-1"), "UTF-8");
            Pattern pattern = Pattern.compile(varPattern, Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(line);
            while (matcher.find()) {
                String paramName = matcher.group();
                paramName = paramName.replaceAll("￥\\{|\\}", "");
                Object paramValue = model.get(paramName);
                if (null == paramValue) {
                    continue;
                }
                line = matcher.replaceFirst(paramValue.toString());
                matcher = pattern.matcher(line);
            }
            sb.append(line);
        }
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(sb.toString());
    }
}
