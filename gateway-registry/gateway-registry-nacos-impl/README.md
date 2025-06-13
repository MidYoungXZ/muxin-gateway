# Nacos注册中心认证支持

本模块提供了支持用户名密码认证的Nacos注册中心实现。

## 功能特性

- 支持Nacos用户名密码认证
- 向下兼容无认证模式
- 完整的服务注册、发现、订阅功能
- 包含完整的单元测试

## 配置说明

### 带认证配置

```yaml
muxin:
  gateway:
    register:
      type: nacos
      address: 127.0.0.1:8848
      username: nacos
      password: nacos
      group: DEFAULT_GROUP
```

### 无认证配置（向下兼容）

```yaml
muxin:
  gateway:
    register:
      type: nacos
      address: 127.0.0.1:8848
      group: DEFAULT_GROUP
```

## 使用方式

### 1. 直接使用

```java
// 带认证
RegisterCenter registerCenter = new NacosRegisterCenter(
    "127.0.0.1:8848", 
    "DEFAULT_GROUP", 
    "DEFAULT", 
    "nacos", 
    "nacos"
);

// 无认证（向下兼容）
RegisterCenter registerCenter = new NacosRegisterCenter(
    "127.0.0.1:8848", 
    "DEFAULT_GROUP", 
    "DEFAULT"
);
```

### 2. Spring Boot自动配置

配置文件中添加相应配置即可，框架会自动注入带认证信息的NacosRegisterCenter Bean。

## 测试

运行测试类：

```bash
mvn test -Dtest=NacosRegisterCenterTest
```

测试包含以下场景：
- Nacos连接状态检查
- 服务注册和注销
- 服务实例查询
- 服务订阅和取消订阅
- 认证功能测试
- 错误认证测试

## 注意事项

1. 确保Nacos服务器已启动并配置了认证
2. 用户名密码配置错误会导致连接失败
3. 测试时请根据实际Nacos服务器地址调整配置 