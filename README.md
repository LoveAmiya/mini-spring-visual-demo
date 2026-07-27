# Mini-Spring Visual Demo

[English](#english) | [中文](#中文)

---

## English

Mini-Spring Visual Demo is a self-contained Java project that implements a compact Spring-style IoC container and exposes its runtime behavior through a browser-based inspection console.

The project includes a simplified framework core, an XML-configured sample application, JUnit coverage, and a small local web console for exploring bean creation, dependency injection, and application context behavior.

### Highlights

- Spring-style IoC container implemented in Java
- XML bean definition loading
- `ClassPathXmlApplicationContext` support
- Bean lookup through `getBean`
- Controller-Service-Dao dependency wiring
- Bean lifecycle and dependency-injection tests
- Local browser console for runtime inspection
- Self-contained Maven setup with PowerShell helpers
- Beginner-facing glossary for Bean, IoC, dependency injection, AOP proxy, BeanDefinition, and singleton scope
- Annotated `spring.xml`, plain-language flow steps, and per-bean role/status explanations
- Responsive flow layout that switches to a vertical sequence on narrow screens

### Repository Layout

```text
mini-spring-core/        Framework core implementation
src/main/java/           Sample application and browser console
src/main/resources/      XML bean configuration
src/test/java/           JUnit tests for the sample application
pom.xml                  Maven project for the visual demo
run_tests.ps1            Installs the core module and runs tests
run_visual_console.ps1   Installs the core module and starts the console
```

### Getting Started

```powershell
git clone https://github.com/LoveAmiya/mini-spring-visual-demo.git
cd mini-spring-visual-demo
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\run_tests.ps1
```

The test script first installs `mini-spring-core` into the local Maven repository, then runs the visual demo test suite.

XML bean definitions are treated as trusted, classpath-owned application configuration. The parser rejects `DOCTYPE` declarations and external entities, and the demo does not accept XML from HTTP requests or uploads.

### Start the Visual Console

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\run_visual_console.ps1
```

Open the console at:

```text
http://127.0.0.1:18080
```

The console always binds to the loopback interface. Ports supplied through the command line or `MINI_SPRING_WEB_PORT` must be between 1 and 65535.

As of 2026-07-28, the complete core and demo regression suites pass `82/82` tests, including malicious `DOCTYPE`/external-entity rejection and browser HTML/API contracts.

Available API endpoints:

```text
GET /api/health
GET /api/user
GET /api/beans
```

### Run the CLI Example

```powershell
cd mini-spring-core
mvn clean install
cd ..
mvn exec:java '-Dexec.mainClass=com.test.minispring.App'
```

### How It Works

1. `spring.xml` defines the application beans and dependencies.
2. `ClassPathXmlApplicationContext` loads and parses the XML configuration.
3. The container creates the configured beans and resolves dependencies.
4. The sample application retrieves beans with `getBean`.
5. The browser console reads the same application context and exposes the resolved beans through HTML and JSON endpoints.

### Requirements

- JDK 17 or newer
- Maven
- PowerShell on Windows for the helper scripts

---

## 中文

Mini-Spring Visual Demo 是一个自包含的 Java 项目，实现了一个精简版 Spring 风格 IoC 容器，并通过浏览器控制台展示容器运行时行为。

项目包含简化框架核心、基于 XML 配置的示例应用、JUnit 测试，以及一个本地 Web 控制台，用于观察 Bean 创建、依赖注入和应用上下文加载过程。

### 项目亮点

- Java 实现的 Spring 风格 IoC 容器
- XML Bean 定义加载
- 支持 `ClassPathXmlApplicationContext`
- 支持通过 `getBean` 获取 Bean
- Controller-Service-Dao 依赖注入链路
- Bean 生命周期和依赖注入测试
- 本地浏览器控制台，可观察运行结果
- 自包含 Maven 项目，并提供 PowerShell 辅助脚本
- 面向初学者解释 Bean、IoC、依赖注入、AOP 代理、BeanDefinition 和单例
- 带中文注释的 `spring.xml`、通俗化十步流程和每个 Bean 的用途/状态说明
- 窄屏自动切换为纵向流程，避免节点和文字重叠

### 项目结构

```text
mini-spring-core/        框架核心实现
src/main/java/           示例应用和浏览器控制台
src/main/resources/      XML Bean 配置
src/test/java/           示例应用的 JUnit 测试
pom.xml                  可视化示例的 Maven 项目
run_tests.ps1            安装核心模块并运行测试
run_visual_console.ps1   安装核心模块并启动可视化控制台
```

### 快速开始

```powershell
git clone https://github.com/LoveAmiya/mini-spring-visual-demo.git
cd mini-spring-visual-demo
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\run_tests.ps1
```

测试脚本会先把 `mini-spring-core` 安装到本地 Maven 仓库，再运行外层可视化示例的测试。

XML Bean 定义被视为由应用维护的、可信的类路径配置。解析器会拒绝 `DOCTYPE` 和外部实体，演示服务也不会从 HTTP 请求或上传文件中读取 Bean XML。

### 启动可视化控制台

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\run_visual_console.ps1
```

浏览器打开：

```text
http://127.0.0.1:18080
```

控制台始终绑定回环地址。命令行或 `MINI_SPRING_WEB_PORT` 提供的端口必须在 1 到 65535 之间。

截至 2026-07-28，核心模块与演示模块完整回归为 `82/82` 通过，覆盖恶意 `DOCTYPE`/外部实体拒绝以及浏览器 HTML/API 契约。

可用接口：

```text
GET /api/health
GET /api/user
GET /api/beans
```

### 运行命令行示例

```powershell
cd mini-spring-core
mvn clean install
cd ..
mvn exec:java '-Dexec.mainClass=com.test.minispring.App'
```

### 工作流程

1. `spring.xml` 定义应用 Bean 和依赖关系。
2. `ClassPathXmlApplicationContext` 加载并解析 XML 配置。
3. 容器创建 Bean，并完成依赖关系注入。
4. 示例应用通过 `getBean` 获取容器中的对象。
5. 浏览器控制台复用同一个应用上下文，并通过 HTML 和 JSON 接口展示解析结果。

### 环境要求

- JDK 17 或更高版本
- Maven
- Windows PowerShell，用于运行辅助脚本
