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
.\run_tests.ps1
```

The test script first installs `mini-spring-core` into the local Maven repository, then runs the visual demo test suite.

### Start the Visual Console

```powershell
.\run_visual_console.ps1
```

Open the console at:

```text
http://127.0.0.1:18080
```

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

- JDK
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
.\run_tests.ps1
```

测试脚本会先把 `mini-spring-core` 安装到本地 Maven 仓库，再运行外层可视化示例的测试。

### 启动可视化控制台

```powershell
.\run_visual_console.ps1
```

浏览器打开：

```text
http://127.0.0.1:18080
```

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

- JDK
- Maven
- Windows PowerShell，用于运行辅助脚本
