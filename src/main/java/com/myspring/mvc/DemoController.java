package com.myspring.mvc;


import com.myspring.mvcframwork.annotaion.MyController;
import com.myspring.mvcframwork.annotaion.MyRequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author linjp
 * @version V1.0
 * @since 2020/3/1 10:25 下午
 */
@MyRequestMapping("/demo")
@MyController
public class DemoController {

    @MyRequestMapping("/get")
    public void get(HttpServletRequest req, HttpServletResponse resp, String name) throws Exception {
        resp.getWriter().write("my Name is " + name);
    }

    @MyRequestMapping("/list")
    public void get(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        resp.getWriter().write("list");
    }
}
