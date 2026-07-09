# Mini-Spring Core

This module contains the simplified Spring framework implementation used by the demo application in the repository root.

It is included so the repository can be cloned and run without depending on a separate local project directory.

## Main Concepts

- IoC container
- Bean definition registration
- XML bean configuration
- `BeanFactory`
- `ApplicationContext`
- Bean lifecycle hooks
- Dependency injection
- AOP support
- Scope and type conversion helpers

## Build

```powershell
mvn clean install
```

The root demo project depends on this module through the local Maven artifact:

```text
com.kama:mini-spring:1.0-SNAPSHOT
```
