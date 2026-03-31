-- ========================================
-- 插件表结构
-- ========================================

-- 插件模板表
CREATE TABLE IF NOT EXISTS gw_plugin (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    plugin_name     VARCHAR(64) NOT NULL UNIQUE COMMENT '插件名称',
    plugin_type     VARCHAR(32) NOT NULL COMMENT '插件类型: AUTH/FILTER',
    description     VARCHAR(500) COMMENT '插件描述',
    schema          JSON COMMENT '配置Schema（JSON Schema格式）',
    default_config  JSON COMMENT '默认配置',
    default_priority INT NOT NULL DEFAULT 5000 COMMENT '默认执行优先级',
    phase           VARCHAR(32) COMMENT '执行阶段: AUTH/FILTER_PRE/FILTER_POST',
    icon            VARCHAR(64) COMMENT '图标',
    is_system       BOOLEAN DEFAULT FALSE COMMENT '是否系统内置',
    enabled         BOOLEAN DEFAULT TRUE COMMENT '是否启用',
    deleted         BOOLEAN DEFAULT FALSE COMMENT '是否删除',
    create_time     DATETIME COMMENT '创建时间',
    update_time     DATETIME COMMENT '更新时间',
    create_by       VARCHAR(64) COMMENT '创建人',
    update_by       VARCHAR(64) COMMENT '更新人',
    INDEX idx_plugin_type (plugin_type),
    INDEX idx_plugin_enabled (enabled, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='插件模板表';

-- 路由插件关联表
CREATE TABLE IF NOT EXISTS gw_route_plugin (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    route_id        BIGINT NOT NULL COMMENT '路由ID',
    plugin_id       BIGINT NOT NULL COMMENT '插件ID',
    config          JSON COMMENT '实例配置（覆盖默认配置）',
    priority_override INT COMMENT '自定义优先级',
    enabled         BOOLEAN DEFAULT TRUE COMMENT '该路由上是否启用此插件',
    sort_order      INT DEFAULT 0 COMMENT '排序',
    create_time     DATETIME COMMENT '创建时间',
    update_time     DATETIME COMMENT '更新时间',
    INDEX idx_route_plugin_route (route_id),
    INDEX idx_route_plugin_plugin (plugin_id),
    UNIQUE INDEX idx_route_plugin_unique (route_id, plugin_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='路由插件关联表';

-- ========================================
-- 预置系统插件数据
-- ========================================

-- AUTH 类型插件
INSERT INTO gw_plugin (plugin_name, plugin_type, description, schema, default_config, default_priority, phase, icon, is_system, enabled, deleted, create_time, update_time) VALUES
('jwt-auth', 'AUTH', 'JWT Token认证', 
 '{"type":"object","properties":{"secret":{"type":"string","format":"password","title":"签名密钥"},"header":{"type":"string","default":"Authorization","title":"Token Header"},"algorithm":{"type":"string","enum":["HS256","HS512","RS256"],"default":"HS256","title":"签名算法"}},"required":["secret"]}',
 '{"header":"Authorization","algorithm":"HS256"}',
 8000, 'AUTH', '🔐', true, true, false, NOW(), NOW()),

('basic-auth', 'AUTH', '基础认证',
 '{"type":"object","properties":{"username":{"type":"string","title":"用户名"},"password":{"type":"string","format":"password","title":"密码"}},"required":["username","password"]}',
 '{}',
 7900, 'AUTH', '🔐', true, true, false, NOW(), NOW()),

('api-key', 'AUTH', 'API密钥认证',
 '{"type":"object","properties":{"key":{"type":"string","title":"API密钥"},"header":{"type":"string","default":"X-API-Key","title":"Header名称"}},"required":["key"]}',
 '{"header":"X-API-Key"}',
 7800, 'AUTH', '🔐', true, true, false, NOW(), NOW());

-- FILTER 类型插件
INSERT INTO gw_plugin (plugin_name, plugin_type, description, schema, default_config, default_priority, phase, icon, is_system, enabled, deleted, create_time, update_time) VALUES
('rate-limit', 'FILTER', '请求限流',
 '{"type":"object","properties":{"rate":{"type":"number","minimum":1,"title":"每秒请求数(QPS)"},"burst":{"type":"number","minimum":0,"default":0,"title":"突发容量"},"key":{"type":"string","enum":["remote_addr","consumer_name","header:X-User-Id"],"default":"remote_addr","title":"限流维度"},"rejectedCode":{"type":"integer","default":429,"title":"拒绝响应码"}},"required":["rate"]}',
 '{"burst":0,"key":"remote_addr","rejectedCode":429}',
 6000, 'FILTER_PRE', '⚡', true, true, false, NOW(), NOW()),

('cors', 'FILTER', '跨域处理',
 '{"type":"object","properties":{"allowOrigins":{"type":"string","default":"*","title":"允许的Origin"},"allowMethods":{"type":"string","default":"*","title":"允许的方法"},"allowHeaders":{"type":"string","default":"*","title":"允许的Header"},"allowCredentials":{"type":"boolean","default":false,"title":"允许凭证"},"maxAge":{"type":"integer","default":3600,"title":"预检缓存时间"}}}',
 '{"allowOrigins":"*","allowMethods":"*","allowHeaders":"*","allowCredentials":false,"maxAge":3600}',
 4000, 'FILTER_PRE', '⚡', true, true, false, NOW(), NOW()),

('timeout', 'FILTER', '超时控制',
 '{"type":"object","properties":{"connectTimeout":{"type":"integer","minimum":100,"default":5000,"title":"连接超时(ms)"},"responseTimeout":{"type":"integer","minimum":1000,"default":30000,"title":"响应超时(ms)"}}}',
 '{"connectTimeout":5000,"responseTimeout":30000}',
 3000, 'FILTER_PRE', '⚡', true, true, false, NOW(), NOW()),

('circuit-breaker', 'FILTER', '熔断器',
 '{"type":"object","properties":{"failureThreshold":{"type":"integer","minimum":1,"default":5,"title":"失败阈值"},"successThreshold":{"type":"integer","minimum":1,"default":3,"title":"恢复阈值"},"timeout":{"type":"integer","minimum":1000,"default":60000,"title":"熔断时间(ms)"}}}',
 '{"failureThreshold":5,"successThreshold":3,"timeout":60000}',
 5500, 'FILTER_PRE', '⚡', true, true, false, NOW(), NOW()),

('request-rewrite', 'FILTER', '请求重写',
 '{"type":"object","properties":{"pathRegex":{"type":"string","title":"路径正则"},"pathReplacement":{"type":"string","title":"替换路径"},"headersToAdd":{"type":"object","title":"添加Header"},"headersToRemove":{"type":"array","items":{"type":"string"},"title":"移除Header"}}}',
 '{}',
 5000, 'FILTER_PRE', '⚡', true, true, false, NOW(), NOW()),

('response-rewrite', 'FILTER', '响应重写',
 '{"type":"object","properties":{"headersToAdd":{"type":"object","title":"添加Header"},"headersToRemove":{"type":"array","items":{"type":"string"},"title":"移除Header"},"bodyRegex":{"type":"string","title":"Body正则"},"bodyReplacement":{"type":"string","title":"Body替换"}}}',
 '{}',
 2000, 'FILTER_POST', '⚡', true, true, false, NOW(), NOW());