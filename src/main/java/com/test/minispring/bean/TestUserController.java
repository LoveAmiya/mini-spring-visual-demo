package com.test.minispring.bean;

/**
 * 演示依赖链的最上层。
 *
 * spring.xml 声明了 ref 属性，因此 Mini-Spring 会通过此 setter 注入 userService。
 * 这是 setter 形式的依赖注入；虽然类名是 Controller，但它并不是真正的 HTTP Controller。
 */
public class TestUserController {

    private TestUserService userService;

    public TestUserService getUserService() {
        return userService;
    }

    public void setUserService(TestUserService userService) {
        this.userService = userService;
    }

    public String queryUserInfo() {
        // 委托调用使入口逻辑与业务逻辑、数据访问逻辑保持分离。
        return userService.queryUserInfo();
    }

    public void printUserInfo() {
        System.out.println(queryUserInfo());
    }
}
