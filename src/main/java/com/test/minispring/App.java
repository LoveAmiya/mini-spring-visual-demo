package com.test.minispring;

import com.minispring.context.ApplicationContext;
import com.minispring.context.support.ClassPathXmlApplicationContext;
import com.test.minispring.bean.TestUserController;

/**
 * Mini-Spring 容器的最小客户端。
 *
 * 面试时可从上到下讲解：创建 Context 会触发 XML 解析和 Bean 创建；getBean 返回
 * 已完成依赖注入的 Controller；最后的方法调用证明 Controller -> Service -> Dao 可用。
 */
public class App {

    public static void main(String[] args) {
        // 对象创建交给 Context，而不是由应用手动调用构造器和 setter。
        ApplicationContext applicationContext =
                new ClassPathXmlApplicationContext("classpath:spring.xml");

        // 类型安全查找同时校验 XML Bean 名称和预期的应用层类型。
        TestUserController userController =
                applicationContext.getBean("userController", TestUserController.class);

        // 能成功调用，是因为容器在启动时已注入下游 Service 和 Dao 引用。
        System.out.println(userController.queryUserInfo());
    }
}

