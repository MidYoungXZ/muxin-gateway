-- Muxin Gateway 核心功能表结构
-- 用于网关路由、断言、过滤器等配置管理

-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS `muxin_gateway` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `muxin_gateway`;

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
    KEY `idx_deleted` (`deleted`)
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
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_route_filter` (`route_id`, `filter_id`),
    KEY `idx_route_id` (`route_id`),
    KEY `idx_filter_id` (`filter_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='路由-过滤器关联表';

-- 6. 路由模板表（可选，用于快速创建相似路由）
DROP TABLE IF EXISTS `gw_route_template`;
CREATE TABLE `gw_route_template` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `template_name` VARCHAR(100) NOT NULL COMMENT '模板名称',
    `description` VARCHAR(500) DEFAULT NULL COMMENT '模板描述',
    `route_config` JSON NOT NULL COMMENT '路由配置模板（JSON格式）',
    `predicate_config` JSON DEFAULT NULL COMMENT '断言配置模板（JSON格式）',
    `filter_config` JSON DEFAULT NULL COMMENT '过滤器配置模板（JSON格式）',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除：0-否，1-是',
    PRIMARY KEY (`id`),
    KEY `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='路由模板表';

-- 7. 网关指标监控表
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

-- 插入示例数据
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

-- 3. 插入示例路由
INSERT INTO `gw_route` (`route_id`, `route_name`, `description`, `uri`, `metadata`, `order`, `enabled`) VALUES
('user-service', '用户服务路由', '转发到用户服务的所有请求', 'http://localhost:8081', '{"service": "user", "version": "1.0"}', 1, 1),
('order-service', '订单服务路由', '转发到订单服务的所有请求', 'http://localhost:8082', '{"service": "order", "version": "1.0"}', 2, 1),
('product-service', '商品服务路由', '转发到商品服务的所有请求', 'http://localhost:8083', '{"service": "product", "version": "1.0"}', 3, 1);

-- 4. 配置路由断言关联（示例）
-- 用户服务路由配置路径匹配
INSERT INTO `gw_route_predicate` (`route_id`, `predicate_id`, `sort_order`) VALUES
(1, 1, 1);  -- user-service 使用路径前缀匹配断言

-- 5. 配置路由过滤器关联（示例）
-- 用户服务路由添加请求头过滤器
INSERT INTO `gw_route_filter` (`route_id`, `filter_id`, `sort_order`) VALUES
(1, 1, 1),  -- user-service 使用添加请求头过滤器
(1, 6, 2);  -- user-service 使用限流过滤器 