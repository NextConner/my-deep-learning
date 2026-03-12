# JtCool Server

`jtcool-server` 是一个基于 Spring Boot 3 的多模块后端项目，当前包含以下模块：

- `jtcool-admin`：启动入口与控制层
- `jtcool-common`：公共工具与通用模型
- `jtcool-framework`：安全、配置与基础设施
- `jtcool-system`：系统业务能力
- `jtcool-quartz`：定时任务
- `jtcool-generator`：代码生成

## 环境要求

- JDK 21
- Maven 3.9+

## 常用命令

在项目根目录执行：

```bash
mvn -DskipTests compile
mvn clean package -DskipTests
mvn -pl jtcool-admin spring-boot:run
```

## 配置说明

- 主配置文件位于 `jtcool-admin/src/main/resources/application.yml`
- 数据源配置位于 `jtcool-admin/src/main/resources/application-druid.yml`
- 初始化 SQL 位于 `sql/`

## 当前状态

- 已完成 `ruoyi` 到 `jtcool` / `JtCool` 的包名、模块名和品牌名迁移
- 当前后端可正常执行 `mvn -DskipTests compile`
