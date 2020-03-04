package com.myspring.mvc;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.myspring.mvcframwork.annotaion.MyController;
import com.myspring.mvcframwork.annotaion.MyRequestMapping;
import com.myspring.mvcframwork.annotaion.MyRequestParam;

/**
 * @author linjp
 * @version V1.0
 * @since 2020/3/1 10:25 下午
 */
@MyRequestMapping("/demo")
@MyController
public class DemoController {

    @MyRequestMapping("/get")
    public void get(HttpServletRequest req, HttpServletResponse resp, @MyRequestParam(name = "name") String name)
            throws Exception {
        resp.getWriter().write("my Name is " + name);
    }

    @MyRequestMapping("/add")
    public Integer get(HttpServletRequest req, HttpServletResponse resp, @MyRequestParam(name = "a") Integer a,
            @MyRequestParam(name = "b") Integer b) throws Exception {
        return a + b;
    }
}
