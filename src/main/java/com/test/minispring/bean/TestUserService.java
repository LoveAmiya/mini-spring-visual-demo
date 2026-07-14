package com.test.minispring.bean;

/**
 * 用于展示两种 XML 属性类型的中间层。
 *
 * company 是字面量 value；userDao 是 BeanReference。工厂需直接转换前者，并通过
 * getBean 解析后者，然后分别调用对应 setter。
 */
public class TestUserService {

    private String company;
    private TestUserDao userDao;

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public TestUserDao getUserDao() {
        return userDao;
    }

    public void setUserDao(TestUserDao userDao) {
        this.userDao = userDao;
    }

    public String queryUserInfo() {
        // 这一调用在一个简短例子中展示已注入的 Controller -> Service -> Dao 链路。
        return "user=" + userDao.queryUserName() + ", company=" + company;
    }
}

