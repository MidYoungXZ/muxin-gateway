-- 路由配置表
CREATE TABLE IF NOT EXISTS gateway_route (
    id VARCHAR(64) PRIMARY KEY,
    uri VARCHAR(255) NOT NULL,
    predicates CLOB,
    filters CLOB,
    metadata CLOB,
    order_num INT DEFAULT 0,
    enabled BOOLEAN DEFAULT TRUE,
    version INT DEFAULT 1,
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 操作审计日志表
CREATE TABLE IF NOT EXISTS gateway_audit_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT,
    username VARCHAR(50),
    operation_type VARCHAR(50),
    resource_type VARCHAR(50),
    resource_id VARCHAR(100),
    operation_detail CLOB,
    ip_address VARCHAR(50),
    user_agent VARCHAR(255),
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 系统配置表
CREATE TABLE IF NOT EXISTS gateway_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    config_key VARCHAR(100) UNIQUE NOT NULL,
    config_value CLOB,
    description VARCHAR(255),
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_route_enabled ON gateway_route(enabled);
CREATE INDEX IF NOT EXISTS idx_route_order ON gateway_route(order_num);
CREATE INDEX IF NOT EXISTS idx_audit_log_time ON gateway_audit_log(created_time);
CREATE INDEX IF NOT EXISTS idx_audit_log_user ON gateway_audit_log(username);
CREATE INDEX IF NOT EXISTS idx_config_key ON gateway_config(config_key); 