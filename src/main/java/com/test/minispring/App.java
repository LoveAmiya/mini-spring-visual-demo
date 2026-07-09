package com.test.minispring;

import com.minispring.context.ApplicationContext;
import com.minispring.context.support.ClassPathXmlApplicationContext;
import com.test.minispring.bean.TestUserController;

public class App {

    public static void main(String[] args) {
        ApplicationContext applicationContext =
                new ClassPathXmlApplicationContext("classpath:spring.xml");

        TestUserController userController =
                applicationContext.getBean("userController", TestUserController.class);

        System.out.println(userController.queryUserInfo());
    }
}

