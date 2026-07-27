# Mini-Spring 启动手册

## 项目做什么

这是一个 Java/Spring 原理学习项目，实现了简化版 IoC 容器：读取 XML Bean 配置、注册 BeanDefinition、通过反射创建 Bean、解析依赖并注入属性，最后通过 `getBean` 返回可用对象。

浏览器页面用于可视化这条过程，不是生产级 Web 服务。

## 最短启动路径

在仓库根目录执行：

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\test-local.ps1
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\run_visual_console.ps1
```

打开 <http://127.0.0.1:18080/>。服务只监听本机回环地址。

## 先测试

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\run_tests.ps1
```

脚本会先安装 `mini-spring-core` 到本地 Maven 仓库，然后运行核心框架与外层演示项目的测试。

## 启动前端可视化

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\run_visual_console.ps1
```

浏览器打开：`http://127.0.0.1:18080`

页面演示顺序：

```text
1. 先看“6 个名词”，理解 Bean、IoC、依赖注入、AOP、BeanDefinition 和单例。
2. 查看带中文注释的 spring.xml，并理解 id、class、value、ref 分别表示什么。
3. 从“找到配置文件”开始逐步播放十个通俗步骤；技术类名保留在次级信息中。
4. 查看 userDao、userService、userController 的用途、Java 类、依赖状态和 AOP 状态。
5. 第 10 步显示真实查询结果，证明 Controller -> Service -> Dao 依赖链可用。
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
5. PowerShell 若限制本地脚本，先确认脚本来源，再在当前用户范围使用合适的执行策略。
```

正式介绍见：`README.md`
