package com.lagou.edu.mvcframework.servlet;

import com.lagou.demo.service.IDemoService;
import com.lagou.edu.mvcframework.annotations.LagouAutowired;
import com.lagou.edu.mvcframework.annotations.LagouController;
import com.lagou.edu.mvcframework.annotations.LagouRequestMapping;
import com.lagou.edu.mvcframework.annotations.LagouService;
import com.lagou.edu.mvcframework.pojo.Handler;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Jaa
 * @date 2022/1/15 18:35
 */
public class LgDispatcherServlet extends HttpServlet {

    private Properties properties = new Properties();

    private List<String> classNames = new ArrayList<>(); // 缓存扫描到的类的全限定类名

    private Map<String, Object> ioc = new HashMap<String, Object>();

    // private Map<String, Method> handlerMapping = new HashMap<>();
    private List<Handler> handlerMapping = new ArrayList<>();

    @Override
    public void init(ServletConfig config) throws ServletException {
        // 1 加载配置文件 springmvc.properties
        String contextConfigLocation = config.getInitParameter("contextConfigLocation");
        doLoadConfig(contextConfigLocation);

        // 2 扫描相关类, 扫描注解
        doScan(properties.getProperty("scanPackage"));

        // 3 初始化bean对象 (实现Ioc容器, 基于注解)
        doInstance();

        // 4 实现依赖注入
        doAutowired();

        // 5 构造一个HandlerMapping处理器映射器, 将配置的url和method建立映射关系
        doHandlerMapping();

        System.out.println("lagou mvc 初始化完成...");

        // 等待请求进入, 处理请求
    }

    // 构造一个HandlerMapping处理器映射器
    private void doHandlerMapping() {
        if (ioc.isEmpty()) return;

        for(Map.Entry<String, Object> entry : ioc.entrySet()) {
            // 获取ioc中当前遍历的对象的class类型
            Class<?> aClass = entry.getValue().getClass();

            if (!aClass.isAnnotationPresent(LagouController.class)) continue;

            String baseUrl = "";
            if (aClass.isAnnotationPresent(LagouRequestMapping.class)) {
                LagouRequestMapping annotation = aClass.getAnnotation(LagouRequestMapping.class);
                baseUrl = annotation.value(); // 等于 /demo
            }

            // 获取方法
            Method[] methods = aClass.getMethods();
            for (int i = 0; i < methods.length; i++) {
                Method method = methods[i];
                // 方法上没有@LagouRequestMapping就不处理
                if (!method.isAnnotationPresent(LagouRequestMapping.class)) continue;

                // 如果标识，就处理
                LagouRequestMapping annotation = method.getAnnotation(LagouRequestMapping.class);
                String methodUrl = annotation.value(); // /query
                String url = baseUrl + methodUrl;

                // 把method所有属性封装以及url封装为一个handler
                Handler handler = new Handler(entry.getValue(), method, Pattern.compile(url));

                // 计算方法的参数位置信息 query(HttpServletRequest request, HttpServletResponse response, String name)
                Parameter[] parameters = method.getParameters();
                for (int j = 0; j < parameters.length; j++) {
                    Parameter parameter = parameters[j];

                    if (parameter.getType() == HttpServletRequest.class || parameter.getType() == HttpServletResponse.class) {
                        // 如果是request和response对象，那么参数名称写HttpServletRequest和HttpServletResponse
                        handler.getParamIndexMapping().put(parameter.getType().getSimpleName(), j);
                    } else {
                        handler.getParamIndexMapping().put(parameter.getName(), j); // <name, 2>
                    }
                }

                // 建立url和method的映射关系（map缓存起来）
                handlerMapping.add(handler);
            }
        }
    }

    // 实现依赖注入
    private void doAutowired() {
        if (ioc.isEmpty()) return;
        // 有对象, 再进行依赖注入处理
        for (Map.Entry<String, Object> entry : ioc.entrySet()) {
            Field[] declaredFields = entry.getValue().getClass().getDeclaredFields();
            // 遍历判断
            for (int i = 0; i < declaredFields.length; i++) {
                Field declaredField = declaredFields[i]; // @LagouAutowired private IDemoService demoService;
                if (!declaredField.isAnnotationPresent(LagouAutowired.class)) {
                    continue;
                }
                // 有该注解
                LagouAutowired annotation = declaredField.getAnnotation(LagouAutowired.class);
                String beanName = annotation.value(); // 需要注入的bean的id
                if ("".equals(beanName.trim())) {
                    // 没有配置具体的bean id，那就需要根据当前字段类型注入（接口注入）  IDemoService
                    beanName = declaredField.getType().getName();
                }
                // 开启赋值
                declaredField.setAccessible(true);
                try {
                    declaredField.set(entry.getValue(), ioc.get(beanName));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }

    }
    // 实现IoC容器
    // 基于classNames缓存的类的全限定类名，以及反射技术，完成对象的创建和管理
    private void doInstance() {
        if (classNames.size() == 0) return;

        try {
            for (int i = 0; i < classNames.size(); i++) {
                String className = classNames.get(i); // com.lagou.demo.controller.DemoController
                // 反射
                Class<?> aClass = Class.forName(className);
                // 区分controller，区分service
                if (aClass.isAnnotationPresent(LagouController.class)) {
                    // controller的id此处不做过多处理，不取value了，就拿类名将首字母小写作为id，保存到ioc中
                    String simpleName = aClass.getSimpleName(); // DemoController
                    String lowerFirstSimpleName = lowerFirst(simpleName); // demoController
                    // 实例化, 放入IoC容器
                    Object o = aClass.newInstance();
                    ioc.put(lowerFirstSimpleName, o);
                } else if (aClass.isAnnotationPresent(LagouService.class)){
                    LagouService annotation = aClass.getAnnotation(LagouService.class);
                    // 获取注解value值
                    String beanName = annotation.value();

                    // 如果指定了id，就以指定的为准
                    if (!"".equals(beanName.trim())) {
                        ioc.put(beanName, aClass.newInstance());
                    } else {
                        // 如果没有指定，就以类名首字母小写
                        beanName = lowerFirst(aClass.getSimpleName());
                        ioc.put(beanName, aClass.newInstance());
                    }

                    // service层往往是接口，面向接口开发，此时再以接口名为id，放入一份对象到ioc中，便于后期根据接口类型注入
                    Class<?>[] interfaces = aClass.getInterfaces();
                    for (int j = 0; j < interfaces.length; j++) {
                        Class<?> anInterface = interfaces[j];
                        // 以接口的全限定类名作为id放入
                        ioc.put(anInterface.getName(), aClass.newInstance());
                    }
                } else {
                    continue;
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 扫描类
    // scanPackage: com.lagou.demo package----> 磁盘上的文件夹(File) com/lagou/demo
    private void doScan(String scanPackage) {
        String scanPackagePath = Thread.currentThread().getContextClassLoader().getResource("").getPath() + scanPackage.replaceAll("\\.", "/");
        File pack = new File(scanPackagePath);

        File[] files = pack.listFiles();

        for(File file: files) {
            if(file.isDirectory()) { // 子package
                // 递归
                doScan(scanPackage + "." + file.getName());  // com.lagou.demo.controller
            }else if(file.getName().endsWith(".class")) {
                String className = scanPackage + "." + file.getName().replaceAll(".class", "");
                classNames.add(className);
            }
        }
    }

    // 加载配置文件
    private void doLoadConfig(String contextConfigLocation) {
        InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream(contextConfigLocation);
        try {
            properties.load(resourceAsStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // 处理请求：根据url，找到对应的method方法，进行调用
        // 获取uri
        // String requestURI = req.getRequestURI();
        // Method method = handlerMapping.get(requestURI); // 获取到一个反射的方法
        // // 反射调用，需要传入对象，参数，此处无法调用，没有把对象缓存起来，也没有参数！！！
        // method.invoke(obj, );

        // 根据uri获取到能够处理当前请求的hanlder（从handlermapping中（list））
        Handler handler = getHandler(req);
        if (handler == null) {
            resp.getWriter().write("404 not found");
            return;
        }

        // 参数绑定
        // 获取所有参数类型的数组，这个数组的长度是我们要传入参数args数组的长度
        Class<?>[] parameterTypes = handler.getMethod().getParameterTypes();
        // 根据上述数组长度创建一个新的数组 （参数数组，是要传入反射调用的）
        Object[] paramValues = new Object[parameterTypes.length];

        // 以下是为了向参数数组中塞值，而且还得保证参数的顺序和方法中形参的顺序一致
        Map<String, String[]> parameterMap = req.getParameterMap();
        // 遍历request中所有参数  （填充除了request，response之外的参数）
        for (Map.Entry<String, String[]> param : parameterMap.entrySet() ) {
            // name=1&name=2   name [1,2]
            String value = StringUtils.join(param.getValue(), ","); // 如同 1,2
            // 如果参数和方法中的参数匹配上了，填充数据
            if (!handler.getParamIndexMapping().containsKey(param.getKey())) continue;

            // 方法形参确实有该参数，找到它的索引位置，对应的把参数值放入paraValues
            Integer index = handler.getParamIndexMapping().get(param.getKey()); // name在第 2 个位置

            // 把前台传递过来的参数值填充到对应的位置去
            paramValues[index] = value;
        }

        Integer requestIndex = handler.getParamIndexMapping().get(HttpServletRequest.class.getSimpleName()); // 0
        paramValues[requestIndex] = req;

        Integer responseIndex = handler.getParamIndexMapping().get(HttpServletResponse.class.getSimpleName()); // 1
        paramValues[responseIndex] = resp;

        // 最终调用handler的method属性
        try {
            handler.getMethod().invoke(handler.getController(), paramValues);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    private Handler getHandler(HttpServletRequest req) {
        if (handlerMapping.isEmpty()) return null;

        String url = req.getRequestURI();

        for (Handler handler : handlerMapping) {
            Matcher matcher = handler.getPattern().matcher(url);
            if (!matcher.matches()) continue;
            return handler;
        }

        return null;
    }

    // 首字母小写
    public String lowerFirst(String str) {
        char[] chars = str.toCharArray();
        if ('A' <= chars[0] && chars[0] <= 'Z') {
            chars[0] += 32;
        }
        return String.valueOf(chars);
    }
}
