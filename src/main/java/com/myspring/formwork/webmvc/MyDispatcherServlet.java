package com.myspring.formwork.webmvc;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.myspring.formwork.annotation.MyAutoWired;
import com.myspring.formwork.annotation.MyController;
import com.myspring.formwork.annotation.MyRequestMapping;
import com.myspring.formwork.annotation.MyRequestParam;
import com.myspring.formwork.annotation.MyService;

/**
 * @author linjp
 * @version V1.0
 * @since 2020/3/1 8:44 下午
 */
public class MyDispatcherServlet extends HttpServlet {
    private Properties contextConfig = new Properties();

    private List<String> classNames = new ArrayList<>();

    private Map<String, Object> ioc = new ConcurrentHashMap<>();

    private List<HandlerMapper> handlerMappers = new ArrayList<>();

    @Override
    public void init(ServletConfig config) throws ServletException {
        // 1. 读取配置文件
        doLoadConfiguration(config.getInitParameter("contextConfigLocation"));
        // 2. 扫描类
        doScanner(contextConfig.getProperty("scanPackage"));
        // 3. 初始化类
        doInstance();
        // 4. 初始化依赖
        doAutoWired();
        // 5. 初始化路径和方法
        initHandleMapper();
        System.out.println("spring mvc init success");

    }

    private void initHandleMapper() {
        if (ioc.isEmpty()) {
            return;
        }
        for (Map.Entry<String, Object> entry : ioc.entrySet()) {
            Class<?> clazz = entry.getValue().getClass();
            if (!clazz.isAnnotationPresent(MyController.class)) {
                return;
            }

            String baseUrl = "";
            if (clazz.isAnnotationPresent(MyRequestMapping.class)) {
                baseUrl = ("/" + clazz.getAnnotation(MyRequestMapping.class).value()).replaceAll("/+", "/");
            }

            Method[] methods = clazz.getMethods();
            for (Method method : methods) {
                if (method.isAnnotationPresent(MyRequestMapping.class)) {
                    String url = (baseUrl + "/" + method.getAnnotation(MyRequestMapping.class).value()).replaceAll("/+",
                            "/");
                    HandlerMapper handlerMapper = new HandlerMapper(url, method, entry.getValue());
                    handlerMappers.add(handlerMapper);
                    System.out.println("mapped:" + handlerMapper);
                }
            }

        }
    }

    private void doAutoWired() {
        if (ioc.isEmpty()) {
            return;
        }

        for (Map.Entry<String, Object> entry : ioc.entrySet()) {
            Field[] fields = entry.getValue().getClass().getDeclaredFields();
            for (Field field : fields) {
                if (!field.isAnnotationPresent(MyAutoWired.class)) {
                    return;
                }

                MyAutoWired annotation = field.getAnnotation(MyAutoWired.class);
                String beanName = annotation.value().trim();
                if ("".equals(beanName)) {
                    beanName = field.getType().getName();
                }
                field.setAccessible(true);

                try {
                    field.set(entry.getValue(), ioc.get(beanName));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    private void doInstance() {
        try {

            for (String className : classNames) {
                Class<?> clazz = Class.forName(className);
                if (clazz.isAnnotationPresent(MyController.class)) {
                    Object instance = clazz.newInstance();
                    String beanName = clazz.getSimpleName();
                    ioc.put(toLowerFirstCase(beanName), instance);
                }

                if (clazz.isAnnotationPresent(MyService.class)) {
                    Object instance = clazz.newInstance();
                    // 1. 取val
                    String beanName = clazz.getAnnotation(MyService.class).value();
                    // 2. 首字母小写
                    if ("".equals(beanName)) {
                        beanName = clazz.getSimpleName();
                    }
                    ioc.put(beanName, instance);
                    // 3. 按类型注入
                    // ioc.put(clazz.getName(), instance);
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private String toLowerFirstCase(String beanName) {
        char[] chars = beanName.toCharArray();
        chars[0] += 32;
        return String.valueOf(chars);
    }

    private void doScanner(String scanPackage) {
        // 获取到了com.myspring转成/com/myspring
        String bastPath = "/" + scanPackage.replaceAll("\\.", "/");
        URL url = this.getClass().getClassLoader().getResource(bastPath);
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
            contextConfig.load(fis);
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

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // 6. 执行方法
        doDispatch(req, resp);
    }

    private void doDispatch(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HandlerMapper handlerMapper = getHandler(req);

        if (handlerMapper == null) {
            resp.getWriter().write("404 NOT FOUND");
            return;
        }

        Class<?>[] parameterTypes = handlerMapper.getMethod().getParameterTypes();
        Map<String, String[]> parameterMap = req.getParameterMap();
        Object[] parameterValues = new Object[parameterTypes.length];
        for (Map.Entry<String, String[]> param : parameterMap.entrySet()) {
            String value = Arrays.toString(param.getValue()).replaceAll("\\[|\\]", "").replaceAll("\\s", ",");
            if (!handlerMapper.getParamIndexMap().containsKey(param.getKey())) {
                continue;
            }
            Integer index = handlerMapper.getParamIndexMap().get(param.getKey());
            parameterValues[index] = convert(parameterTypes[index], value);
        }

        if (handlerMapper.getParamIndexMap().containsKey(HttpServletRequest.class.getName())) {
            int reqIndex = handlerMapper.getParamIndexMap().get(HttpServletRequest.class.getName());
            parameterValues[reqIndex] = req;
        }

        if (handlerMapper.getParamIndexMap().containsKey(HttpServletResponse.class.getName())) {
            int responseIndex = handlerMapper.getParamIndexMap().get(HttpServletResponse.class.getName());
            parameterValues[responseIndex] = resp;
        }

        try {
            Object result = handlerMapper.getMethod().invoke(handlerMapper.getController(), parameterValues);
            if (result == null || result instanceof Void) {
                return;
            }
            resp.getWriter().write(result.toString());
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    private Object convert(Class<?> parameterType, String value) {
        if (value == null) {
            return value;
        }
        if (parameterType == Integer.class) {
            return Integer.parseInt(value);
        }
        return value;
    }

    private HandlerMapper getHandler(HttpServletRequest req) {
        if (handlerMappers.isEmpty()) {
            return null;
        }
        String requestURI = req.getRequestURI();
        for (HandlerMapper handlerMapper : handlerMappers) {
            if (handlerMapper.getUrl().equals(requestURI)) {
                return handlerMapper;
            }
        }
        return null;
    }

    public class HandlerMapper {
        private String url;
        private Method method;
        private Object controller;
        private Map<String, Integer> paramIndexMap;

        public HandlerMapper(String url, Method method, Object controller) {
            this.url = url;
            this.method = method;
            this.controller = controller;
            paramIndexMap = new HashMap<>();
            putParam(method);
        }

        public String getUrl() {
            return url;
        }

        public Method getMethod() {
            return method;
        }

        public Object getController() {
            return controller;
        }

        public Map<String, Integer> getParamIndexMap() {
            return paramIndexMap;
        }

        @Override
        public String toString() {
            return "HandlerMapper{" + "url='" + url + '\'' + ", method=" + method + ", controller=" + controller
                    + ", paramIndexMap=" + paramIndexMap + '}';
        }

        private void putParam(Method method) {
            Annotation[][] parameterAnnotations = method.getParameterAnnotations();
            for (int i = 0; i < parameterAnnotations.length; i++) {
                for (Annotation annotation : parameterAnnotations[i]) {
                    if (annotation instanceof MyRequestParam) {
                        String paramName = ((MyRequestParam) annotation).name();
                        paramIndexMap.put(paramName, i);
                    }
                }

            }

            Class<?>[] parameterTypes = method.getParameterTypes();
            for (int i = 0; i < parameterTypes.length; i++) {
                Class<?> type = parameterTypes[i];
                if (type == HttpServletRequest.class || type == HttpServletResponse.class) {
                    paramIndexMap.put(type.getName(), i);
                }
            }

        }
    }

}
