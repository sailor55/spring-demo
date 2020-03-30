package com.myspring.formwork.webmvc.servlet;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.myspring.formwork.annotation.MyController;
import com.myspring.formwork.annotation.MyRequestMapping;
import com.myspring.formwork.context.support.MyApplicationContext;

import lombok.extern.slf4j.Slf4j;

/**
 * @author linjp
 * @version V1.0
 * @since 2020/3/1 8:44 下午
 */
@Slf4j
public class MyDispatcherServlet extends HttpServlet {

    private MyApplicationContext context;

    private static final String CONTEXT_CONFIG_LOCATION = "contextConfigLocation";

    private List<MyHandlerMapping> handlerMappings = new ArrayList<>();

    private Map<MyHandlerMapping, MyHandlerAdapter> handlerAdapters = new ConcurrentHashMap();

    private List<MyViewResolver> viewResolvers = new ArrayList<>();

    @Override
    public void init(ServletConfig config) throws ServletException {
        context = new MyApplicationContext(config.getInitParameter(CONTEXT_CONFIG_LOCATION));
        initStrategies(context);
        log.info("servlet init success");
    }

    private void initStrategies(MyApplicationContext context) {
        // 多文件上传组件
        initMultipartResolver(context);
        // 国际化组件
        initLocaleResolver(context);
        // 主题解析器 用于切换css等样式
        initThemeResolver(context);
        // 用于保存controller和url的关系
        initHandlerMappings(context);
        // 参数适配器，解析各个参数
        initHandlerAdapters(context);
        // 异常拦截器
        initHandlerExceptionResolvers(context);
        // 返回内容为空时 把请求转换为默认的viewName
        initRequestToViewNameTranslator(context);
        // 视图解析器 将结果变为ftl，jsp页面
        initViewResolvers(context);
        // 重定向的时候把请求参数保存到flashMap中
        initFlashMapManager(context);
    }

    private void initFlashMapManager(MyApplicationContext context) {

    }

    private void initViewResolvers(MyApplicationContext context) {
        String templateRoot = context.getConfig().getProperty("templateRoot");
        String templateRootPath = this.getClass().getClassLoader().getResource(templateRoot).getFile();
        viewResolvers.add(new MyViewResolver(templateRootPath));

    }

    private void initRequestToViewNameTranslator(MyApplicationContext context) {

    }

    private void initHandlerExceptionResolvers(MyApplicationContext context) {

    }

    private void initHandlerAdapters(MyApplicationContext context) {
        for (MyHandlerMapping handlerMapping : handlerMappings) {
            handlerAdapters.put(handlerMapping, new MyHandlerAdapter());
        }

    }

    private void initHandlerMappings(MyApplicationContext context) {
        String[] beanNames = context.getBeanDefinitionNames();
        for (String beanName : beanNames) {
            Object bean = context.getBean(beanName);
            Class<?> clazz = bean.getClass();
            if (!clazz.isAnnotationPresent(MyController.class)) {
                continue;
            }

            String baseUrl = "";
            if (clazz.isAnnotationPresent(MyRequestMapping.class)) {
                baseUrl = ("/" + clazz.getAnnotation(MyRequestMapping.class).value()).replaceAll("/+", "/");
            }

            Method[] methods = clazz.getMethods();
            for (Method method : methods) {
                if (method.isAnnotationPresent(MyRequestMapping.class)) {
                    String url = ("/" + baseUrl + "/" + method.getAnnotation(MyRequestMapping.class).value())
                            .replaceAll("/+", "/");
                    Pattern pattern = Pattern.compile(url);
                    MyHandlerMapping handlerMapper = new MyHandlerMapping(pattern, method, bean);
                    this.handlerMappings.add(handlerMapper);
                    log.info("mapped:" + handlerMapper);
                }
            }
        }

    }

    private void initThemeResolver(MyApplicationContext context) {

    }

    private void initLocaleResolver(MyApplicationContext context) {

    }

    private void initMultipartResolver(MyApplicationContext context) {
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // 6. 执行方法
        try {
            doDispatch(req, resp);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void doDispatch(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        // 1.从req中拿到handlerMapper
        MyHandlerMapping handler = getHandler(req);

        if (handler == null) {
            processDispatchResult(req, resp, new MyModelAndView("404"));
            return;
        }

        // 2.准备调用前的参数
        MyHandlerAdapter ha = getHandlerAdapter(handler);
        // 3.调用真正的方法
        MyModelAndView mv = ha.handle(req, resp, handler);

        // 处理视图解析器 返回html、ftl页面
        processDispatchResult(req, resp, mv);

    }

    private void processDispatchResult(HttpServletRequest request, HttpServletResponse response, MyModelAndView mv)
            throws Exception {
        if (mv == null) {
            return;
        }

        if (this.viewResolvers.isEmpty()) {
            return;
        }

        for (MyViewResolver viewResolver : this.viewResolvers) {
            MyView view = viewResolver.resolveViewName(mv.getViewName(), null);
            view.render(mv.getModel(), request, response);
        }

    }

    private MyHandlerAdapter getHandlerAdapter(MyHandlerMapping handler) {
        return handlerAdapters.get(handler);
    }

    private MyHandlerMapping getHandler(HttpServletRequest req) {
        if (handlerMappings.isEmpty()) {
            return null;
        }
        String requestURI = req.getRequestURI();
        for (MyHandlerMapping handlerMapper : handlerMappings) {
            Matcher matcher = handlerMapper.getPattern().matcher(requestURI);
            if (matcher.matches()) {
                return handlerMapper;
            }
        }
        return null;
    }
}
