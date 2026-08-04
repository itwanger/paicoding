# Backend Development Guidelines

> Best practices for backend development in this project.

---

## Overview

This directory contains guidelines for backend development. Fill in each file with your project's specific conventions.

---

## Guidelines Index

| Guide | Description | Status |
|-------|-------------|--------|
| [Directory Structure](./directory-structure.md) | 模块分层、DO/DTO/VO、Controller/Service 组织 | Filled |
| [Database Guidelines](./database-guidelines.md) | MyBatis-Plus 继承体系、分页、事务、Liquibase | Filled |
| [Error Handling](./error-handling.md) | ResVo/StatusEnum/ForumException/全局异常 | Filled |
| [Quality Guidelines](./quality-guidelines.md) | 命名风格、测试现状、反模式清单 | Filled |
| [Logging Guidelines](./logging-guidelines.md) | @Slf4j/logback/MDC/邮件告警 | Filled |
| [Integration Patterns](./integration-patterns.md) | Redis/RabbitMQ/ES/Spring 事件/异步 | Filled (新增) |
| [AI / LLM Integration](./ai-llm-integration.md) | 派聪明多供应商抽象与复用约定 | Filled (新增) |
| [Config & Environment](./config-environment.md) | resources-env/.env/动态配置/部署参考 | Filled (新增) |

上游自带规范文档（权威来源，本 spec 与其对齐）：`docs/约定.md`。

---

## How to Fill These Guidelines

For each guideline file:

1. Document your project's **actual conventions** (not ideals)
2. Include **code examples** from your codebase
3. List **forbidden patterns** and why
4. Add **common mistakes** your team has made

The goal is to help AI assistants and new team members understand how YOUR project works.

---

**Language**: All documentation should be written in **English**.
