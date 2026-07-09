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
}
