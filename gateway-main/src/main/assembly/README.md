# Muxin Gateway 分发包

## 目录结构

```
gateway-main-1.0-SNAPSHOT/
├── gateway-main-1.0-SNAPSHOT.jar  # 应用JAR包
├── run.sh                         # 启动脚本
├── stop.sh                        # 停止脚本
├── restart.sh                     # 重启脚本
├── status.sh                      # 状态检查脚本
├── application.yml                # 主配置文件
├── application-sqlite.yml         # SQLite数据库配置
├── application-mysql.yml          # MySQL数据库配置
├── log4j2.xml                     # 日志配置
├── data/                          # 数据目录(SQLite数据库)
│   └── muxin-gateway.db
├── logs/                          # 日志目录
│   ├── gateway.log
│   ├── gateway-error.log
│   └── console.log
└── README.md                      # 说明文档
```

## 快速启动

```bash
cd gateway-main-1.0-SNAPSHOT
./run.sh
```

## 访问地址

启动成功后，可以通过以下地址访问：

- **管理界面**: http://localhost:9191
- **网关端口**: http://localhost:9292
- **API文档**: http://localhost:9191/swagger-ui.html

默认登录账号：
- 用户名: `admin`
- 密码: `admin123`

## 停止服务

```bash
./stop.sh
```

## 状态检查

```bash
./status.sh
```

## 配置说明

### 修改端口

编辑 `application.yml`：

```yaml
server:
  port: 9191              # 管理端口

muxin:
  gateway:
    netty:
      server:
        port: 9292        # 网关端口
```

### 切换数据库

默认使用 SQLite 数据库（数据文件位于 `data/muxin-gateway.db`）。

切换到 MySQL：

1. 编辑 `application.yml`，修改 `spring.profiles.active` 为 `mysql`
2. 编辑 `application-mysql.yml`，配置数据库连接信息

```yaml
spring:
  profiles:
    active: mysql    # 切换到MySQL
```

### JVM参数配置

通过环境变量设置：

```bash
export JAVA_OPTS="-Xms512m -Xmx2048m -XX:+UseG1GC"
export SPRING_PROFILES_ACTIVE=mysql
./run.sh
```

## 日志管理

日志文件位于 `logs/` 目录：

- `gateway.log` - 应用日志（按天滚动，保留7天）
- `gateway-error.log` - 错误日志（按天滚动，保留5天）
- `console.log` - 控制台输出日志

## 常见问题

### 1. 端口被占用

检查端口占用：

```bash
lsof -i:9191
lsof -i:9292
```

修改配置文件中的端口后重启。

### 2. 无法访问管理界面

检查服务状态：

```bash
./status.sh
curl http://localhost:9191
```

查看日志：

```bash
tail -f logs/gateway.log
```

### 3. SQLite数据库问题

数据库文件位于 `data/muxin-gateway.db`，首次启动会自动创建。

如需重建数据库，删除该文件后重启服务。

## 版本信息

- Version: ${project.version}