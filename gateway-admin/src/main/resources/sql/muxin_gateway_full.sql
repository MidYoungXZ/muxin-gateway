-- Muxin Gateway 完整数据库脚本
-- 包含网关核心功能表和RBAC权限管理系统表

-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS `muxin_gateway` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `muxin_gateway`;

-- ====================================
-- 第一部分：网关核心功能表
-- ====================================

-- 1. 网关路由表
DROP TABLE IF EXISTS `gw_route`;
CREATE TABLE `gw_route` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `route_id` VARCHAR(100) NOT NULL COMMENT '路由ID（唯一标识）',
    `route_name` VARCHAR(100) NOT NULL COMMENT '路由名称',
    `description` VARCHAR(500) DEFAULT NULL COMMENT '路由描述',
    `uri` VARCHAR(500) NOT NULL COMMENT '目标URI',
    `metadata` JSON DEFAULT NULL COMMENT '元数据（JSON格式）',
    `order` INT NOT NULL DEFAULT 0 COMMENT '路由顺序（值越小优先级越高）',
    `enabled` TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用：0-禁用，1-启用',
    `grayscale_enabled` TINYINT NOT NULL DEFAULT 0 COMMENT '是否启用灰度发布：0-禁用，1-启用',
    `grayscale_config` JSON DEFAULT NULL COMMENT '灰度配置（JSON格式）',
    `template_id` BIGINT DEFAULT NULL COMMENT '路由模板ID',
    `version` INT NOT NULL DEFAULT 1 COMMENT '版本号',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` VARCHAR(50) DEFAULT NULL COMMENT '创建人',
    `update_by` VARCHAR(50) DEFAULT NULL COMMENT '更新人',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除：0-否，1-是',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_route_id` (`route_id`),
    KEY `idx_enabled` (`enabled`),
    KEY `idx_order` (`order`),
    KEY `idx_deleted` (`deleted`),
    KEY `idx_template_id` (`template_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='网关路由表';

-- 2. 断言配置表
DROP TABLE IF EXISTS `gw_predicate`;
CREATE TABLE `gw_predicate` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `predicate_name` VARCHAR(100) NOT NULL COMMENT '断言名称',
    `predicate_type` VARCHAR(50) NOT NULL COMMENT '断言类型：Path/Method/Header/Query/Cookie/Host/RemoteAddr/Between',
    `description` VARCHAR(500) DEFAULT NULL COMMENT '断言描述',
    `config` JSON NOT NULL COMMENT '断言配置（JSON格式）',
    `is_system` TINYINT NOT NULL DEFAULT 0 COMMENT '是否系统内置：0-否，1-是',
    `enabled` TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用：0-禁用，1-启用',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` VARCHAR(50) DEFAULT NULL COMMENT '创建人',
    `update_by` VARCHAR(50) DEFAULT NULL COMMENT '更新人',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除：0-否，1-是',
    PRIMARY KEY (`id`),
    KEY `idx_predicate_type` (`predicate_type`),
    KEY `idx_enabled` (`enabled`),
    KEY `idx_is_system` (`is_system`),
    KEY `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='断言配置表';

-- 3. 过滤器配置表
DROP TABLE IF EXISTS `gw_filter`;
CREATE TABLE `gw_filter` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `filter_name` VARCHAR(100) NOT NULL COMMENT '过滤器名称',
    `filter_type` VARCHAR(50) NOT NULL COMMENT '过滤器类型',
    `description` VARCHAR(500) DEFAULT NULL COMMENT '过滤器描述',
    `config` JSON DEFAULT NULL COMMENT '过滤器配置（JSON格式）',
    `order` INT NOT NULL DEFAULT 0 COMMENT '执行顺序',
    `is_system` TINYINT NOT NULL DEFAULT 0 COMMENT '是否系统内置：0-否，1-是',
    `enabled` TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用：0-禁用，1-启用',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` VARCHAR(50) DEFAULT NULL COMMENT '创建人',
    `update_by` VARCHAR(50) DEFAULT NULL COMMENT '更新人',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除：0-否，1-是',
    PRIMARY KEY (`id`),
    KEY `idx_filter_type` (`filter_type`),
    KEY `idx_order` (`order`),
    KEY `idx_enabled` (`enabled`),
    KEY `idx_is_system` (`is_system`),
    KEY `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='过滤器配置表';

-- 4. 路由-断言关联表
DROP TABLE IF EXISTS `gw_route_predicate`;
CREATE TABLE `gw_route_predicate` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `route_id` BIGINT NOT NULL COMMENT '路由ID',
    `predicate_id` BIGINT NOT NULL COMMENT '断言ID',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_route_predicate` (`route_id`, `predicate_id`),
    KEY `idx_route_id` (`route_id`),
    KEY `idx_predicate_id` (`predicate_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='路由-断言关联表';

-- 5. 路由-过滤器关联表
DROP TABLE IF EXISTS `gw_route_filter`;
CREATE TABLE `gw_route_filter` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `route_id` BIGINT NOT NULL COMMENT '路由ID',
    `filter_id` BIGINT NOT NULL COMMENT '过滤器ID',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_route_filter` (`route_id`, `filter_id`),
    KEY `idx_route_id` (`route_id`),
    KEY `idx_filter_id` (`filter_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='路由-过滤器关联表';

-- 6. 路由模板表（新增）
DROP TABLE IF EXISTS `gw_route_template`;
CREATE TABLE `gw_route_template` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `template_name` VARCHAR(100) NOT NULL COMMENT '模板名称',
    `description` VARCHAR(500) DEFAULT NULL COMMENT '模板描述',
    `category` VARCHAR(50) DEFAULT NULL COMMENT '模板分类',
    `config` JSON NOT NULL COMMENT '模板配置（JSON格式）',
    `variables` JSON DEFAULT NULL COMMENT '模板变量定义（JSON格式）',
    `is_system` TINYINT NOT NULL DEFAULT 0 COMMENT '是否系统内置：0-否，1-是',
    `usage_count` INT NOT NULL DEFAULT 0 COMMENT '使用次数',
    `enabled` TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用：0-禁用，1-启用',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` VARCHAR(50) DEFAULT NULL COMMENT '创建人',
    `update_by` VARCHAR(50) DEFAULT NULL COMMENT '更新人',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除：0-否，1-是',
    PRIMARY KEY (`id`),
    KEY `idx_category` (`category`),
    KEY `idx_is_system` (`is_system`),
    KEY `idx_enabled` (`enabled`),
    KEY `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='路由模板表';

-- 7. 服务节点表（新增）
DROP TABLE IF EXISTS `gw_service_node`;
CREATE TABLE `gw_service_node` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `node_id` VARCHAR(100) NOT NULL COMMENT '节点ID（唯一标识）',
    `service_name` VARCHAR(100) NOT NULL COMMENT '服务名称',
    `node_name` VARCHAR(100) NOT NULL COMMENT '节点名称',
    `address` VARCHAR(200) NOT NULL COMMENT '节点地址',
    `port` INT NOT NULL COMMENT '端口号',
    `weight` INT NOT NULL DEFAULT 1 COMMENT '权重（1-100）',
    `max_fails` INT NOT NULL DEFAULT 3 COMMENT '最大失败次数',
    `fail_timeout` INT NOT NULL DEFAULT 30 COMMENT '失败超时时间（秒）',
    `backup` TINYINT NOT NULL DEFAULT 0 COMMENT '是否为备份节点：0-否，1-是',
    `health_check_enabled` TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用健康检查：0-禁用，1-启用',
    `health_check_interval` INT NOT NULL DEFAULT 30 COMMENT '健康检查间隔（秒）',
    `health_check_timeout` INT NOT NULL DEFAULT 5 COMMENT '健康检查超时时间（秒）',
    `health_check_path` VARCHAR(200) DEFAULT '/health' COMMENT '健康检查路径',
    `health_check_expected_status` JSON DEFAULT NULL COMMENT '健康检查期望状态码（JSON数组）',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '节点状态：0-禁用，1-启用，2-维护中',
    `last_check_time` DATETIME DEFAULT NULL COMMENT '最后检查时间',
    `last_check_result` TINYINT DEFAULT NULL COMMENT '最后检查结果：0-失败，1-成功',
    `metadata` JSON DEFAULT NULL COMMENT '节点元数据（JSON格式）',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` VARCHAR(50) DEFAULT NULL COMMENT '创建人',
    `update_by` VARCHAR(50) DEFAULT NULL COMMENT '更新人',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除：0-否，1-是',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_node_id` (`node_id`),
    KEY `idx_service_name` (`service_name`),
    KEY `idx_status` (`status`),
    KEY `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='服务节点表';

-- 8. 负载均衡配置表（新增）
DROP TABLE IF EXISTS `gw_load_balance`;
CREATE TABLE `gw_load_balance` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `route_id` BIGINT NOT NULL COMMENT '路由ID',
    `strategy` VARCHAR(50) NOT NULL DEFAULT 'ROUND_ROBIN' COMMENT '负载均衡策略：ROUND_ROBIN/WEIGHTED/LEAST_CONN/IP_HASH/RANDOM',
    `config` JSON DEFAULT NULL COMMENT '负载均衡配置（JSON格式）',
    `enabled` TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用：0-禁用，1-启用',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` VARCHAR(50) DEFAULT NULL COMMENT '创建人',
    `update_by` VARCHAR(50) DEFAULT NULL COMMENT '更新人',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_route_id` (`route_id`),
    KEY `idx_strategy` (`strategy`),
    KEY `idx_enabled` (`enabled`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='负载均衡配置表';

-- 9. 网关指标监控表
DROP TABLE IF EXISTS `gw_metrics`;
CREATE TABLE `gw_metrics` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `route_id` VARCHAR(100) NOT NULL COMMENT '路由ID',
    `request_count` BIGINT NOT NULL DEFAULT 0 COMMENT '请求总数',
    `success_count` BIGINT NOT NULL DEFAULT 0 COMMENT '成功请求数',
    `failure_count` BIGINT NOT NULL DEFAULT 0 COMMENT '失败请求数',
    `total_time` BIGINT NOT NULL DEFAULT 0 COMMENT '总耗时（毫秒）',
    `avg_time` BIGINT NOT NULL DEFAULT 0 COMMENT '平均耗时（毫秒）',
    `max_time` BIGINT NOT NULL DEFAULT 0 COMMENT '最大耗时（毫秒）',
    `min_time` BIGINT NOT NULL DEFAULT 0 COMMENT '最小耗时（毫秒）',
    `collect_time` DATETIME NOT NULL COMMENT '采集时间',
    PRIMARY KEY (`id`),
    KEY `idx_route_id` (`route_id`),
    KEY `idx_collect_time` (`collect_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='网关指标监控表';

-- ====================================
-- 第二部分：RBAC权限管理系统表
-- ====================================

-- 10. 用户表
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password` VARCHAR(255) NOT NULL COMMENT '密码',
    `nickname` VARCHAR(50) NOT NULL COMMENT '昵称',
    `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `mobile` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    `avatar` VARCHAR(500) DEFAULT NULL COMMENT '头像URL',
    `dept_id` BIGINT DEFAULT NULL COMMENT '部门ID',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` VARCHAR(50) DEFAULT NULL COMMENT '创建人',
    `update_by` VARCHAR(50) DEFAULT NULL COMMENT '更新人',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除：0-否，1-是',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    KEY `idx_dept_id` (`dept_id`),
    KEY `idx_status` (`status`),
    KEY `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 11. 角色表
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '角色ID',
    `role_code` VARCHAR(50) NOT NULL COMMENT '角色编码',
    `role_name` VARCHAR(50) NOT NULL COMMENT '角色名称',
    `description` VARCHAR(200) DEFAULT NULL COMMENT '描述',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` VARCHAR(50) DEFAULT NULL COMMENT '创建人',
    `update_by` VARCHAR(50) DEFAULT NULL COMMENT '更新人',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除：0-否，1-是',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_code` (`role_code`),
    KEY `idx_status` (`status`),
    KEY `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色表';

-- 12. 部门表（修改：添加ancestors字段）
DROP TABLE IF EXISTS `sys_dept`;
CREATE TABLE `sys_dept` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '部门ID',
    `parent_id` BIGINT NOT NULL DEFAULT 0 COMMENT '父部门ID',
    `ancestors` VARCHAR(500) DEFAULT '' COMMENT '祖级列表',
    `dept_name` VARCHAR(50) NOT NULL COMMENT '部门名称',
    `dept_code` VARCHAR(50) DEFAULT NULL COMMENT '部门编码',
    `leader` VARCHAR(50) DEFAULT NULL COMMENT '负责人',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '联系电话',
    `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `order_num` INT NOT NULL DEFAULT 0 COMMENT '排序',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` VARCHAR(50) DEFAULT NULL COMMENT '创建人',
    `update_by` VARCHAR(50) DEFAULT NULL COMMENT '更新人',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除：0-否，1-是',
    PRIMARY KEY (`id`),
    KEY `idx_parent_id` (`parent_id`),
    KEY `idx_status` (`status`),
    KEY `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='部门表';

-- 13. 菜单表
DROP TABLE IF EXISTS `sys_menu`;
CREATE TABLE `sys_menu` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '菜单ID',
    `parent_id` BIGINT NOT NULL DEFAULT 0 COMMENT '父菜单ID',
    `menu_name` VARCHAR(50) NOT NULL COMMENT '菜单名称',
    `i18n_code` VARCHAR(100) DEFAULT NULL COMMENT '国际化编码',
    `menu_type` CHAR(1) NOT NULL COMMENT '菜单类型：M-目录，C-菜单，F-按钮',
    `path` VARCHAR(200) DEFAULT NULL COMMENT '路由地址',
    `component` VARCHAR(255) DEFAULT NULL COMMENT '组件路径',
    `perms` VARCHAR(100) DEFAULT NULL COMMENT '权限标识',
    `icon` VARCHAR(100) DEFAULT NULL COMMENT '菜单图标',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序',
    `visible` TINYINT NOT NULL DEFAULT 1 COMMENT '是否显示：0-隐藏，1-显示',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` VARCHAR(50) DEFAULT NULL COMMENT '创建人',
    `update_by` VARCHAR(50) DEFAULT NULL COMMENT '更新人',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除：0-否，1-是',
    PRIMARY KEY (`id`),
    KEY `idx_parent_id` (`parent_id`),
    KEY `idx_status` (`status`),
    KEY `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='菜单表';

-- 14. 用户角色关联表
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `role_id` BIGINT NOT NULL COMMENT '角色ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_role` (`user_id`, `role_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户角色关联表';

-- 15. 角色菜单关联表（新增）
DROP TABLE IF EXISTS `sys_role_menu`;
CREATE TABLE `sys_role_menu` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `role_id` BIGINT NOT NULL COMMENT '角色ID',
    `menu_id` BIGINT NOT NULL COMMENT '菜单ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_menu` (`role_id`, `menu_id`),
    KEY `idx_role_id` (`role_id`),
    KEY `idx_menu_id` (`menu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色菜单关联表';

-- ====================================
-- 第三部分：初始化数据
-- ====================================

-- 1. 插入内置断言
INSERT INTO `gw_predicate` (`predicate_name`, `predicate_type`, `description`, `config`, `is_system`, `enabled`) VALUES
('路径前缀匹配', 'Path', '匹配指定的路径前缀', '{"pattern": "/api/**"}', 1, 1),
('请求方法匹配', 'Method', '匹配HTTP请求方法', '{"methods": ["GET", "POST"]}', 1, 1),
('请求头匹配', 'Header', '匹配请求头', '{"name": "X-Request-Id", "regexp": ".*"}', 1, 1),
('查询参数匹配', 'Query', '匹配查询参数', '{"param": "token"}', 1, 1),
('Cookie匹配', 'Cookie', '匹配Cookie', '{"name": "sessionId", "regexp": ".*"}', 1, 1),
('主机名匹配', 'Host', '匹配主机名', '{"patterns": ["*.example.com"]}', 1, 1),
('远程地址匹配', 'RemoteAddr', '匹配客户端IP地址', '{"sources": ["192.168.1.0/24"]}', 1, 1),
('时间范围匹配', 'Between', '匹配时间范围', '{"datetime1": "2024-01-01T00:00:00", "datetime2": "2024-12-31T23:59:59"}', 1, 1);

-- 2. 插入内置过滤器
INSERT INTO `gw_filter` (`filter_name`, `filter_type`, `description`, `config`, `order`, `is_system`, `enabled`) VALUES
('添加请求头', 'AddRequestHeader', '添加请求头过滤器', '{"name": "X-Request-From", "value": "gateway"}', 1, 1, 1),
('添加响应头', 'AddResponseHeader', '添加响应头过滤器', '{"name": "X-Response-From", "value": "gateway"}', 2, 1, 1),
('移除请求头', 'RemoveRequestHeader', '移除请求头过滤器', '{"name": "X-Internal-Header"}', 3, 1, 1),
('移除响应头', 'RemoveResponseHeader', '移除响应头过滤器', '{"name": "X-Internal-Response"}', 4, 1, 1),
('路径重写', 'RewritePath', '重写请求路径', '{"regexp": "/api/v1/(?<segment>.*)", "replacement": "/${segment}"}', 5, 1, 1),
('限流', 'RequestRateLimiter', '请求限流过滤器', '{"replenishRate": 10, "burstCapacity": 20}', 6, 1, 1),
('熔断', 'CircuitBreaker', '熔断器过滤器', '{"name": "myCircuitBreaker", "fallbackUri": "/fallback"}', 7, 1, 1),
('重试', 'Retry', '重试过滤器', '{"retries": 3, "statuses": ["BAD_GATEWAY"], "methods": ["GET", "POST"]}', 8, 1, 1);

-- 3. 插入内置路由模板
INSERT INTO `gw_route_template` (`template_name`, `description`, `category`, `config`, `variables`, `is_system`, `enabled`) VALUES
('基础HTTP服务模板', '适用于标准HTTP RESTful服务的基础模板', 'HTTP', 
'{"predicates": [{"type": "Path", "args": {"pattern": "/${serviceName}/**"}}], "filters": [{"type": "StripPrefix", "args": {"parts": 1}}]}',
'[{"name": "serviceName", "type": "string", "required": true, "description": "服务名称"}]', 1, 1),
('微服务网关模板', '适用于微服务架构的网关模板，包含负载均衡和熔断', 'Microservice',
'{"predicates": [{"type": "Path", "args": {"pattern": "/api/${serviceName}/**"}}], "filters": [{"type": "StripPrefix", "args": {"parts": 2}}, {"type": "CircuitBreaker", "args": {"name": "${serviceName}CircuitBreaker"}}]}',
'[{"name": "serviceName", "type": "string", "required": true, "description": "服务名称"}]', 1, 1),
('静态资源模板', '适用于静态资源代理的模板', 'Static',
'{"predicates": [{"type": "Path", "args": {"pattern": "/static/**"}}], "filters": [{"type": "AddResponseHeader", "args": {"name": "Cache-Control", "value": "max-age=3600"}}]}',
'[]', 1, 1);

-- 4. 插入测试部门（更新ancestors字段）
INSERT INTO `sys_dept` (`id`, `parent_id`, `ancestors`, `dept_name`, `dept_code`, `order_num`, `status`) VALUES
(1, 0, '0', '木心科技', 'MUXIN', 1, 1),
(2, 1, '0,1', '技术部', 'TECH', 1, 1),
(3, 1, '0,1', '运维部', 'OPS', 2, 1),
(4, 1, '0,1', '产品部', 'PRODUCT', 3, 1);

-- 5. 插入测试用户 (密码: admin123)
INSERT INTO `sys_user` (`id`, `username`, `password`, `nickname`, `email`, `mobile`, `dept_id`, `status`) VALUES
(1, 'admin', '$2a$10$5Z1Kbm99AbBFN7y8Dd3.V.UGmeJX8nWKG47aPXXMuupC7kLe8lKIu', '超级管理员', 'admin@muxin.com', '13800138000', 1, 1),
(2, 'zhangsan', '$2a$10$5Z1Kbm99AbBFN7y8Dd3.V.UGmeJX8nWKG47aPXXMuupC7kLe8lKIu', '张三', 'zhangsan@muxin.com', '13800138001', 2, 1),
(3, 'lisi', '$2a$10$5Z1Kbm99AbBFN7y8Dd3.V.UGmeJX8nWKG47aPXXMuupC7kLe8lKIu', '李四', 'lisi@muxin.com', '13800138002', 3, 1);

-- 6. 插入测试角色
INSERT INTO `sys_role` (`id`, `role_code`, `role_name`, `description`, `status`) VALUES
(1, 'SUPER_ADMIN', '超级管理员', '系统超级管理员，拥有所有权限', 1),
(2, 'GATEWAY_ADMIN', '网关管理员', '负责网关路由、断言、过滤器的配置管理', 1),
(3, 'OPERATOR', '运维人员', '负责系统监控和日常维护', 1),
(4, 'VIEWER', '查看人员', '只能查看，不能修改', 1);

-- 7. 插入用户角色关联
INSERT INTO `sys_user_role` (`user_id`, `role_id`) VALUES
(1, 1),  -- admin是超级管理员
(2, 2),  -- zhangsan是网关管理员
(3, 3);  -- lisi是运维人员

-- 8. 插入系统菜单
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `path`, `component`, `perms`, `icon`, `sort_order`) VALUES
-- 一级菜单
(1, 0, '监控中心', 'M', '/monitor', '', '', 'el-icon-monitor', 1),
(2, 0, '路由管理', 'M', '/route', '', '', 'el-icon-connection', 2),
(3, 0, '断言管理', 'M', '/predicate', '', '', 'el-icon-filter', 3),
(4, 0, '过滤器管理', 'M', '/filter', '', '', 'el-icon-set-up', 4),
(5, 0, '系统管理', 'M', '/system', '', '', 'el-icon-setting', 5),

-- 监控中心子菜单
(11, 1, '实时监控', 'C', '/monitor/realtime', 'monitor/realtime', 'monitor:realtime', '', 1),
(12, 1, '路由统计', 'C', '/monitor/route', 'monitor/route', 'monitor:route', '', 2),
(13, 1, '性能分析', 'C', '/monitor/performance', 'monitor/performance', 'monitor:performance', '', 3),

-- 路由管理子菜单
(21, 2, '路由列表', 'C', '/route/list', 'route/list', 'route:list', '', 1),
(22, 2, '路由测试', 'C', '/route/test', 'route/test', 'route:test', '', 2),
(23, 2, '节点管理', 'C', '/route/nodes', 'route/nodes', 'route:nodes', '', 3),
(24, 2, '负载均衡', 'C', '/route/loadbalance', 'route/loadbalance', 'route:loadbalance', '', 4),
(25, 2, '路由模板', 'C', '/route/templates', 'route/templates', 'route:templates', '', 5),

-- 断言管理子菜单
(31, 3, '断言列表', 'C', '/predicate/list', 'predicate/list', 'predicate:list', '', 1),
(32, 3, '断言类型', 'C', '/predicate/types', 'predicate/types', 'predicate:types', '', 2),

-- 过滤器管理子菜单
(41, 4, '过滤器列表', 'C', '/filter/list', 'filter/list', 'filter:list', '', 1),
(42, 4, '过滤器配置', 'C', '/filter/config', 'filter/config', 'filter:config', '', 2),

-- 系统管理子菜单
(51, 5, '用户管理', 'C', '/system/user', 'system/user', 'system:user', '', 1),
(52, 5, '角色管理', 'C', '/system/role', 'system/role', 'system:role', '', 2),
(53, 5, '部门管理', 'C', '/system/dept', 'system/dept', 'system:dept', '', 3),
(54, 5, '菜单管理', 'C', '/system/menu', 'system/menu', 'system:menu', '', 4),
(55, 5, '操作日志', 'C', '/system/log', 'system/log', 'system:log', '', 5);

-- 9. 插入角色菜单关联（给超级管理员分配所有菜单权限）
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) 
SELECT 1, id FROM `sys_menu` WHERE `deleted` = 0;

-- 10. 插入示例路由
INSERT INTO `gw_route` (`route_id`, `route_name`, `description`, `uri`, `metadata`, `order`, `enabled`) VALUES
('user-service', '用户服务路由', '转发到用户服务的所有请求', 'http://localhost:8081', '{"service": "user", "version": "1.0"}', 1, 1),
('order-service', '订单服务路由', '转发到订单服务的所有请求', 'http://localhost:8082', '{"service": "order", "version": "1.0"}', 2, 1),
('product-service', '商品服务路由', '转发到商品服务的所有请求', 'http://localhost:8083', '{"service": "product", "version": "1.0"}', 3, 1);

-- 11. 配置路由断言关联（示例）
INSERT INTO `gw_route_predicate` (`route_id`, `predicate_id`, `sort_order`) VALUES
(1, 1, 1);  -- user-service 使用路径前缀匹配断言

-- 12. 配置路由过滤器关联（示例）
INSERT INTO `gw_route_filter` (`route_id`, `filter_id`, `sort_order`) VALUES
(1, 1, 1),  -- user-service 使用添加请求头过滤器
(1, 6, 2);  -- user-service 使用限流过滤器

-- 13. 插入示例服务节点
INSERT INTO `gw_service_node` (`node_id`, `service_name`, `node_name`, `address`, `port`, `weight`, `status`) VALUES
('user-service-node-1', 'user-service', '用户服务节点1', '192.168.1.10', 8081, 100, 1),
('user-service-node-2', 'user-service', '用户服务节点2', '192.168.1.11', 8081, 100, 1),
('order-service-node-1', 'order-service', '订单服务节点1', '192.168.1.20', 8082, 100, 1),
('product-service-node-1', 'product-service', '商品服务节点1', '192.168.1.30', 8083, 100, 1);

-- 14. 插入负载均衡配置示例
INSERT INTO `gw_load_balance` (`route_id`, `strategy`, `config`, `enabled`) VALUES
(1, 'ROUND_ROBIN', '{"healthCheck": {"enabled": true, "interval": 30, "timeout": 5, "path": "/health", "expectedStatus": [200]}}', 1),
(2, 'WEIGHTED', '{"healthCheck": {"enabled": true, "interval": 30, "timeout": 5, "path": "/actuator/health", "expectedStatus": [200]}}', 1); 