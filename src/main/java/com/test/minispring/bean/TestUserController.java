package com.test.minispring.bean;

public class TestUserController {

    private TestUserService userService;

    public TestUserService getUserService() {
        return userService;
    }

    public void setUserService(TestUserService userService) {
        this.userService = userService;
    }

    public String queryUserInfo() {
        return userService.queryUserInfo();
    }

    public void printUserInfo() {
        System.out.println(queryUserInfo());
    }
}
