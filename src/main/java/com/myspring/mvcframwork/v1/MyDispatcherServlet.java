package com.myspring.mvcframwork.v1;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.myspring.mvcframwork.annotaion.MyAutoWired;
import com.myspring.mvcframwork.annotaion.MyController;
import com.myspring.mvcframwork.annotaion.MyRequestMapping;
import com.myspring.mvcframwork.annotaion.MyService;

/**
 * @author linjp
 * @version V1.0
 * @since 2020/3/1 8:44 下午
 */
public class MyDispatcherServlet extends HttpServlet {
    private Properties contextConfig = new Properties();

    private List<String> classNames = new ArrayList<>();

    private Map<String, Object> ioc = new ConcurrentHashMap<>();

    private Map<String, Method> handlerMap = new ConcurrentHashMap<>();

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
                    String url = (baseUrl + "/"
                            + method.getAnnotation(MyRequestMapping.class).value()).replaceAll("/+", "/");
                    handlerMap.put(url, method);
                    System.out.println("mapped" + url + "," + method);
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
        String requestURI = req.getRequestURI();
        if (!handlerMap.containsKey(requestURI)) {
            resp.getWriter().write("404 NOT FOUND");
        }
        Method method = handlerMap.get(requestURI);
        String beanName = toLowerFirstCase(method.getDeclaringClass().getSimpleName());
        try {
            Map<String, String[]> parameterMap = req.getParameterMap();
            method.invoke(ioc.get(beanName), new Object[] { req, resp, parameterMap.get("name")[0] });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
