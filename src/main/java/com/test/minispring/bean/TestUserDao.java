package com.test.minispring.bean;

/**
 * 演示依赖链的最底层。
 *
 * 方法返回固定数据，以便项目将 IoC 机制与数据库配置隔离；即使替换为 Repository，
 * 容器的依赖注入机制也不需要改变。
 */
public class TestUserDao {

    public String queryUserName() {
        return "Richard";
    }
}

