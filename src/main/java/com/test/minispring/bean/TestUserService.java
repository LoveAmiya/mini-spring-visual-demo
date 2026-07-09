package com.test.minispring.bean;

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
        return "user=" + userDao.queryUserName() + ", company=" + company;
    }
}

