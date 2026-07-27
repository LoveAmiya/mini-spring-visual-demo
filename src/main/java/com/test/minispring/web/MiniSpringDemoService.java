package com.test.minispring.web;

import com.minispring.context.ApplicationContext;
import com.minispring.context.support.ClassPathXmlApplicationContext;
import com.test.minispring.bean.TestUserController;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * 浏览器演示层与 Mini-Spring 核心之间的只读适配器。
 *
 * 每次 API 调用都会创建新的 Context，以便页面反复展示完整启动过程。这适合教学，
 * 但不同于生产服务：生产环境通常让一个 ApplicationContext 存活到进程结束。
 */
public class MiniSpringDemoService {

    private static final String CONFIG_LOCATION = "classpath:spring.xml";

    public ApplicationContext createContext() {
        // 构造器会调用 refresh()，依次完成 XML 解析、定义注册、单例 Bean 创建和依赖注入。
        return new ClassPathXmlApplicationContext(CONFIG_LOCATION);
    }

    public String queryUserInfo() {
        ApplicationContext context = createContext();
        TestUserController controller =
                context.getBean("userController", TestUserController.class);
        return controller.queryUserInfo();
    }

    public List<BeanView> listBeans() {
        // 先列出定义，再解析每个 Bean，使可视化页面同时展示 XML 声明名和运行时实例类型。
        ApplicationContext context = createContext();
        String[] beanNames = context.getBeanDefinitionNames();
        List<BeanView> beans = new ArrayList<>();
        for (String beanName : beanNames) {
            Object bean = context.getBean(beanName);
            String runtimeClassName = bean.getClass().getName();
            String sourceClassName = runtimeClassName.split("\\$\\$", 2)[0];
            String displayClassName = sourceClassName.substring(sourceClassName.lastIndexOf('.') + 1);
            beans.add(new BeanView(
                    beanName,
                    runtimeClassName,
                    displayClassName,
                    describeRole(beanName),
                    !runtimeClassName.equals(sourceClassName)));
        }
        return beans;
    }

    public String readXmlConfig() {
        // 从 classpath 而不是本机绝对路径读取，保证 Maven、测试和可视化服务使用同一资源。
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
        // 这些是讲解里程碑，不是运行日志；真实运行状态仍通过 /api/beans 和 /api/user 展示。
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
        // 前端借助这些关系高亮 Mini-Spring 核心内部执行的同一条依赖链路。
        return List.of(
                new FlowStep(
                        "load-xml",
                        "找到配置文件",
                        "程序启动容器后，先找到 spring.xml；这份文件写着需要创建哪些对象，以及对象之间如何连接。",
                        "src/main/resources/spring.xml",
                        List.of("xml"),
                        List.of()
                ),
                new FlowStep(
                        "parse-definitions",
                        "读懂三条对象配置",
                        "容器逐条读取三个 bean 标签，把每一条翻译成一份对象说明书；此时还没有创建真正的 Java 对象。",
                        "mini-spring-core/.../XmlBeanDefinitionReader.java",
                        List.of("xml", "reader", "definition"),
                        List.of("xml-reader", "reader-definition")
                ),
                new FlowStep(
                        "register-definitions",
                        "记住三份对象说明书",
                        "容器把 userDao、userService、userController 的创建说明保存起来，后面可以随时按名字找到。",
                        "mini-spring-core/.../DefaultListableBeanFactory.java",
                        List.of("definition", "registry"),
                        List.of("definition-registry")
                ),
                new FlowStep(
                        "create-dao",
                        "按说明书创建 userDao",
                        "容器先创建负责提供用户数据的 userDao。它不依赖其他对象，所以可以直接创建。",
                        "src/main/java/com/test/minispring/bean/TestUserDao.java",
                        List.of("registry", "factory", "dao"),
                        List.of("registry-factory", "factory-dao")
                ),
                new FlowStep(
                        "create-service",
                        "按说明书创建 userService",
                        "容器接着创建负责业务处理的 userService；配置说明它还需要公司名称和 userDao。",
                        "src/main/java/com/test/minispring/bean/TestUserService.java",
                        List.of("factory", "service"),
                        List.of("registry-factory", "factory-service")
                ),
                new FlowStep(
                        "inject-dao",
                        "把 userDao 交给 userService",
                        "容器把已经创建好的 userDao 自动连接到 userService。这个自动连接过程叫依赖注入。",
                        "src/main/resources/spring.xml",
                        List.of("dao", "service"),
                        List.of("dao-service")
                ),
                new FlowStep(
                        "create-controller",
                        "按说明书创建 userController",
                        "容器创建对外接收查询的 userController；它还需要 userService 才能完成工作。",
                        "src/main/java/com/test/minispring/bean/TestUserController.java",
                        List.of("factory", "controller"),
                        List.of("factory-controller")
                ),
                new FlowStep(
                        "inject-service",
                        "把 userService 交给 userController",
                        "容器把 userService 自动连接到 userController，三者组成“接收查询 → 处理业务 → 提供数据”的调用链。",
                        "src/main/resources/spring.xml",
                        List.of("service", "controller"),
                        List.of("service-controller")
                ),
                new FlowStep(
                        "store-singletons",
                        "保存三个可复用对象",
                        "容器保存这三个已经连接好的对象。以后再次按名字获取时，会复用同一批对象，不必重新创建。",
                        "mini-spring-core/.../DefaultSingletonBeanRegistry.java",
                        List.of("dao", "service", "controller", "singletons"),
                        List.of("dao-singletons", "service-singletons", "controller-singletons")
                ),
                new FlowStep(
                        "get-bean",
                        "取出入口并完成查询",
                        "程序向容器索要 userController，然后沿着三层对象完成查询；最终结果证明自动创建和连接都成功。",
                        "src/main/java/com/test/minispring/App.java",
                        List.of("singletons", "client", "controller"),
                        List.of("singletons-client", "client-controller")
                )
        );
    }

    private String describeRole(String beanName) {
        if ("userDao".equals(beanName)) {
            return "数据访问对象：负责提供用户数据";
        }
        if ("userService".equals(beanName)) {
            return "业务服务：接收 company 配置，并使用 userDao 查询数据";
        }
        if ("userController".equals(beanName)) {
            return "调用入口：使用 userService 对外提供查询结果";
        }
        return "由 Mini-Spring 容器管理的对象";
    }

    public record BeanView(
            String name,
            String className,
            String displayClassName,
            String role,
            boolean proxied) {
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
