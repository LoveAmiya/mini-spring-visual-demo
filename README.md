# Mini-Spring Visual Demo

[English](#english) | [中文](#中文)

---

## English

Mini-Spring Visual Demo is a Java learning project that demonstrates how a simplified Spring-like IoC container loads XML bean definitions, creates beans, resolves dependencies, and exposes the result through a small browser UI.

The repository is self-contained:

- `mini-spring-core/` contains the simplified Spring framework implementation.
- `src/` contains a small demo application using the framework.
- `MiniSpringWebServer` exposes a local visual console for interviews and demonstrations.

This is not a production web application. It is a learning project for understanding Spring fundamentals.

### Features

- Simplified IoC container
- XML bean configuration
- `ClassPathXmlApplicationContext`
- `getBean` usage
- Controller -> Service -> Dao dependency chain
- JUnit tests
- Local browser visual console
- PowerShell run scripts

### Project Structure

```text
mini-spring-core/        Simplified Spring framework implementation
src/main/java/           Demo application and visual web console
src/main/resources/      spring.xml bean configuration
src/test/java/           JUnit tests
pom.xml                  Demo Maven project
run_tests.ps1            Installs core and runs tests
run_visual_console.ps1   Installs core and starts the browser console
```

### Quick Start

```powershell
cd "F:\All projects\workspace\workspace\mini-spring-test"
.\run_tests.ps1
```

### Run the Visual Console

```powershell
.\run_visual_console.ps1
```

Open:

```text
http://127.0.0.1:18080
```

Available endpoints:

```text
GET /api/health
GET /api/user
GET /api/beans
```

### Run the CLI Demo

The scripts install `mini-spring-core` into the local Maven repository first. To run manually:

```powershell
cd mini-spring-core
mvn clean install
cd ..
mvn exec:java '-Dexec.mainClass=com.test.minispring.App'
```

### What to Explain in an Interview

1. `mini-spring-core/`: simplified Spring framework implementation.
2. `src/main/resources/spring.xml`: XML bean configuration.
3. `App.java`: loads `ClassPathXmlApplicationContext` and calls `getBean`.
4. `TestUserController`, `TestUserService`, `TestUserDao`: dependency chain.
5. `MiniSpringDemoService`: reuses the mini-spring container for the web demo.
6. `MiniSpringWebServer`: exposes the local HTML UI and JSON endpoints.

### Current Scope

- This is not Spring Boot.
- This is not a production web service.
- The browser UI is only a visual layer for demonstrating the framework behavior.
- Docker and hosted deployment are intentionally out of scope for this small learning demo.

---

## 中文

Mini-Spring Visual Demo 是一个 Java/Spring 原理学习项目，用来展示一个简化版 Spring IoC 容器如何读取 XML Bean 配置、创建 Bean、处理依赖关系，并通过一个本地浏览器页面可视化展示结果。

这个仓库是自包含的：

- `mini-spring-core/`：简化版 Spring 框架核心实现。
- `src/`：使用该框架的 demo 应用。
- `MiniSpringWebServer`：本地可视化控制台，方便面试和演示。

这不是生产级 Web 应用，而是用于理解 Spring 核心原理的学习项目。

### 功能特点

- 简化版 IoC 容器
- XML Bean 配置
- `ClassPathXmlApplicationContext`
- `getBean` 调用
- Controller -> Service -> Dao 依赖链
- JUnit 测试
- 本地浏览器可视化控制台
- PowerShell 运行脚本

### 项目结构

```text
mini-spring-core/        简化版 Spring 框架核心实现
src/main/java/           demo 应用和可视化 Web 控制台
src/main/resources/      spring.xml Bean 配置
src/test/java/           JUnit 测试
pom.xml                  demo Maven 项目
run_tests.ps1            安装核心框架并运行测试
run_visual_console.ps1   安装核心框架并启动浏览器控制台
```

### 快速开始

```powershell
cd "F:\All projects\workspace\workspace\mini-spring-test"
.\run_tests.ps1
```

### 运行可视化控制台

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

### 运行命令行 Demo

脚本会先把 `mini-spring-core` 安装到本地 Maven 仓库。如果想手动运行：

```powershell
cd mini-spring-core
mvn clean install
cd ..
mvn exec:java '-Dexec.mainClass=com.test.minispring.App'
```

### 面试讲解顺序

1. `mini-spring-core/`：简化版 Spring 框架核心实现。
2. `src/main/resources/spring.xml`：XML Bean 配置。
3. `App.java`：加载 `ClassPathXmlApplicationContext` 并调用 `getBean`。
4. `TestUserController`、`TestUserService`、`TestUserDao`：依赖调用链。
5. `MiniSpringDemoService`：Web demo 如何复用 mini-spring 容器。
6. `MiniSpringWebServer`：如何暴露本地 HTML 页面和 JSON 接口。

### 当前边界

- 这不是 Spring Boot 项目。
- 这不是生产级 Web 服务。
- 浏览器界面只是为了可视化展示框架行为。
- Docker 和线上部署暂时不属于这个小型学习 demo 的重点。
