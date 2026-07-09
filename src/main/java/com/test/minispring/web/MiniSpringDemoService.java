package com.test.minispring.web;

import com.minispring.context.ApplicationContext;
import com.minispring.context.support.ClassPathXmlApplicationContext;
import com.test.minispring.bean.TestUserController;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class MiniSpringDemoService {

    private static final String CONFIG_LOCATION = "classpath:spring.xml";

    public ApplicationContext createContext() {
        return new ClassPathXmlApplicationContext(CONFIG_LOCATION);
    }

    public String queryUserInfo() {
        ApplicationContext context = createContext();
        TestUserController controller =
                context.getBean("userController", TestUserController.class);
        return controller.queryUserInfo();
    }

    public List<BeanView> listBeans() {
        ApplicationContext context = createContext();
        String[] beanNames = context.getBeanDefinitionNames();
        List<BeanView> beans = new ArrayList<>();
        for (String beanName : beanNames) {
            Object bean = context.getBean(beanName);
            beans.add(new BeanView(beanName, bean.getClass().getName(), describeRole(beanName)));
        }
        return beans;
    }

    public String readXmlConfig() {
        try (InputStream inputStream =
                     Thread.currentThread().getContextClassLoader().getResourceAsStream("spring.xml")) {
            if (inputStream == null) {
                return "spring.xml was not found on the classpath.";
            }
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            return "Failed to read spring.xml: " + e.getMessage();
        }
    }

    public List<String> traceSteps() {
        return List.of(
                "1. ClassPathXmlApplicationContext loads classpath:spring.xml.",
                "2. XmlBeanDefinitionReader parses bean definitions from XML.",
                "3. BeanFactory registers userDao, userService, and userController.",
                "4. userService receives company as a value property.",
                "5. userService receives userDao as a bean reference.",
                "6. userController receives userService as a bean reference.",
                "7. getBean(\"userController\") returns a wired controller instance.",
                "8. queryUserInfo() proves the dependency chain works end to end."
        );
    }

    public List<FlowStep> visualFlow() {
        return List.of(
                new FlowStep(
                        "load-xml",
                        "加载 XML 配置",
                        "ClassPathXmlApplicationContext 从 classpath:spring.xml 读取 Bean 配置文件，拿到容器启动的原始输入。",
                        "src/main/resources/spring.xml",
                        List.of("xml"),
                        List.of()
                ),
                new FlowStep(
                        "parse-definitions",
                        "解析 BeanDefinition",
                        "XmlBeanDefinitionReader 把 XML 中的 bean 标签解析成 BeanDefinition。此时只是得到对象蓝图，还没有真正创建 Java 对象。",
                        "mini-spring-core/.../XmlBeanDefinitionReader.java",
                        List.of("xml", "reader", "definition"),
                        List.of("xml-reader", "reader-definition")
                ),
                new FlowStep(
                        "register-definitions",
                        "注册 BeanDefinition",
                        "DefaultListableBeanFactory 将 userDao、userService、userController 的 BeanDefinition 注册到容器内部的定义表中。",
                        "mini-spring-core/.../DefaultListableBeanFactory.java",
                        List.of("definition", "registry"),
                        List.of("definition-registry")
                ),
                new FlowStep(
                        "create-dao",
                        "创建 userDao",
                        "容器先创建最底层依赖 userDao。这个 Bean 没有 ref 依赖，可以直接实例化并进入后续初始化流程。",
                        "src/main/java/com/test/minispring/bean/TestUserDao.java",
                        List.of("registry", "factory", "dao"),
                        List.of("registry-factory", "factory-dao")
                ),
                new FlowStep(
                        "create-service",
                        "创建 userService",
                        "容器创建 userService，并准备处理它的属性：company 是普通 value，userDao 是 ref 依赖。",
                        "src/main/java/com/test/minispring/bean/TestUserService.java",
                        List.of("factory", "service"),
                        List.of("registry-factory", "factory-service")
                ),
                new FlowStep(
                        "inject-dao",
                        "注入 userDao",
                        "容器发现 userService 依赖 userDao，于是从容器中取出 userDao 实例，注入到 userService.userDao 字段。",
                        "src/main/resources/spring.xml",
                        List.of("dao", "service"),
                        List.of("dao-service")
                ),
                new FlowStep(
                        "create-controller",
                        "创建 userController",
                        "容器创建 Controller 层 Bean。它代表外部调用入口，但此时还需要注入 userService 才能完整工作。",
                        "src/main/java/com/test/minispring/bean/TestUserController.java",
                        List.of("factory", "controller"),
                        List.of("factory-controller")
                ),
                new FlowStep(
                        "inject-service",
                        "注入 userService",
                        "容器把已经完成依赖注入的 userService 注入到 userController，形成 Controller -> Service -> Dao 调用链。",
                        "src/main/resources/spring.xml",
                        List.of("service", "controller"),
                        List.of("service-controller")
                ),
                new FlowStep(
                        "store-singletons",
                        "放入单例池",
                        "创建完成的单例 Bean 会进入 singletonObjects 缓存。后续 getBean 可以直接返回同一个已装配对象。",
                        "mini-spring-core/.../DefaultSingletonBeanRegistry.java",
                        List.of("dao", "service", "controller", "singletons"),
                        List.of("dao-singletons", "service-singletons", "controller-singletons")
                ),
                new FlowStep(
                        "get-bean",
                        "getBean 返回对象",
                        "应用调用 getBean(\"userController\")，容器返回已经完成创建和依赖注入的 Controller，queryUserInfo() 证明链路可用。",
                        "src/main/java/com/test/minispring/App.java",
                        List.of("singletons", "client", "controller"),
                        List.of("singletons-client", "client-controller")
                )
        );
    }

    private String describeRole(String beanName) {
        if ("userDao".equals(beanName)) {
            return "Data access bean";
        }
        if ("userService".equals(beanName)) {
            return "Business service bean with value and ref injection";
        }
        if ("userController".equals(beanName)) {
            return "Controller-style bean used by the demo";
        }
        return "Mini-Spring bean";
    }

    public record BeanView(String name, String className, String role) {
    }

    public record FlowStep(
            String id,
            String title,
            String description,
            String codeReference,
            List<String> activeNodes,
            List<String> activeEdges) {
    }
}
