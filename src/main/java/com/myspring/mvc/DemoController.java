package com.myspring.mvc;

import com.myspring.formwork.annotation.MyAutoWired;
import com.myspring.formwork.annotation.MyController;
import com.myspring.formwork.annotation.MyRequestMapping;
import com.myspring.formwork.annotation.MyRequestParam;
import com.myspring.formwork.webmvc.servlet.MyModelAndView;

import java.util.HashMap;
import java.util.Map;

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

    @MyAutoWired(value = "queryService")
    private QueryService queryService;

    @MyRequestMapping("/get")
    public MyModelAndView get(HttpServletRequest req, HttpServletResponse resp,
        @MyRequestParam(name = "name") String name)
            throws Exception {
        Map<String, Object> map = new HashMap<>(4);
        map.put("name", "tom");
        MyModelAndView mv = new MyModelAndView("get", map);
        return mv;
    }

    @MyRequestMapping("/add")
    public MyModelAndView get(HttpServletRequest req, HttpServletResponse resp, @MyRequestParam(name = "a") Integer a,
            @MyRequestParam(name = "b") Integer b) throws Exception {
        Map<String, Object> map = new HashMap<>(4);
        map.put("result", a + b);
        MyModelAndView mv = new MyModelAndView("add", map);
        return mv;
    }
}
