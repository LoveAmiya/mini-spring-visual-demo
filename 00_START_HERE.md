# Mini-Spring 启动手册

## 项目做什么

这是一个 Java/Spring 原理学习项目，实现了简化版 IoC 容器：读取 XML Bean 配置、注册 BeanDefinition、通过反射创建 Bean、解析依赖并注入属性，最后通过 `getBean` 返回可用对象。

浏览器页面用于可视化这条过程，不是生产级 Web 服务。

## 先测试

```powershell
cd "F:\All projects\workspace\workspace\mini-spring-test"
powershell -ExecutionPolicy Bypass -File .\run_tests.ps1
```

脚本会先安装 `mini-spring-core` 到本地 Maven 仓库，然后运行核心框架与外层演示项目的测试。

## 启动前端可视化

```powershell
cd "F:\All projects\workspace\workspace\mini-spring-test"
powershell -ExecutionPolicy Bypass -File .\run_visual_console.ps1
```

浏览器打开：`http://127.0.0.1:18080`

页面演示顺序：

```text
1. 查看 spring.xml 中的 Bean 配置。
2. 查看 userDao、userService、userController 的 Bean 列表。
3. 点击流程演示，讲 spring.xml -> BeanDefinition -> BeanFactory。
4. 展示 Controller -> Service -> Dao 的依赖注入。
5. 调用用户信息接口，证明 getBean 返回的对象已可用。
```

可用接口：

```text
GET /api/health
GET /api/user
GET /api/beans
GET /api/xml
GET /api/trace
GET /api/flow
```

## 失败先查

```text
1. java -version 是否显示 JDK。
2. mvn -v 是否可用。
3. 是否先成功安装 mini-spring-core。
4. 18080 是否被占用。
5. PowerShell 若拦截脚本，使用 -ExecutionPolicy Bypass。
```

正式介绍见：`README.md`
