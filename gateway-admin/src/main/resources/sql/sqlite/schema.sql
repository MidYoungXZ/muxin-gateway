-- Muxin Gateway SQLite Schema

-- ====================================
-- Gateway Core Tables
-- ====================================

-- 1. 网关路由表
CREATE TABLE IF NOT EXISTS gw_route (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    route_id VARCHAR(100) NOT NULL UNIQUE,
    route_name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    uri VARCHAR(500) NOT NULL,
    metadata TEXT,
    "order" INTEGER NOT NULL DEFAULT 0,
    load_balance_strategy VARCHAR(50) DEFAULT 'ROUND_ROBIN',
    enabled INTEGER NOT NULL DEFAULT 1,
    grayscale_enabled INTEGER NOT NULL DEFAULT 0,
    grayscale_config TEXT,
    template_id INTEGER,
    version INTEGER NOT NULL DEFAULT 1,
    create_time TEXT NOT NULL DEFAULT (datetime('now', 'localtime')),
    update_time TEXT NOT NULL DEFAULT (datetime('now', 'localtime')),
    create_by VARCHAR(50),
    update_by VARCHAR(50)
);

CREATE INDEX IF NOT EXISTS idx_route_enabled ON gw_route(enabled);
CREATE INDEX IF NOT EXISTS idx_route_order ON gw_route("order");

-- 2. 断言配置表
CREATE TABLE IF NOT EXISTS gw_predicate (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    predicate_name VARCHAR(100) NOT NULL,
    predicate_type VARCHAR(50) NOT NULL,
    description VARCHAR(500),
    args TEXT NOT NULL,
    is_system INTEGER NOT NULL DEFAULT 0,
    enabled INTEGER NOT NULL DEFAULT 1,
    create_time TEXT NOT NULL DEFAULT (datetime('now', 'localtime')),
    update_time TEXT NOT NULL DEFAULT (datetime('now', 'localtime')),
    create_by VARCHAR(50),
    update_by VARCHAR(50)
);

CREATE INDEX IF NOT EXISTS idx_predicate_type ON gw_predicate(predicate_type);
CREATE INDEX IF NOT EXISTS idx_predicate_enabled ON gw_predicate(enabled);

-- 3. 路由-断言关联表
CREATE TABLE IF NOT EXISTS gw_route_predicate (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    route_id INTEGER NOT NULL,
    predicate_id INTEGER NOT NULL,
    sort_order INTEGER NOT NULL DEFAULT 0,
    create_time TEXT NOT NULL DEFAULT (datetime('now', 'localtime')),
    UNIQUE(route_id, predicate_id)
);

CREATE INDEX IF NOT EXISTS idx_rp_route_id ON gw_route_predicate(route_id);
CREATE INDEX IF NOT EXISTS idx_rp_predicate_id ON gw_route_predicate(predicate_id);

-- 4. 插件模板表
CREATE TABLE IF NOT EXISTS gw_plugin (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    plugin_name VARCHAR(64) NOT NULL UNIQUE,
    plugin_type VARCHAR(32) NOT NULL,
    description VARCHAR(500),
    schema TEXT,
    default_config TEXT,
    default_priority INTEGER NOT NULL DEFAULT 5000,
    phase VARCHAR(32),
    icon VARCHAR(64),
    is_system INTEGER NOT NULL DEFAULT 0,
    enabled INTEGER NOT NULL DEFAULT 1,
    create_time TEXT,
    update_time TEXT,
    create_by VARCHAR(64),
    update_by VARCHAR(64)
);

CREATE INDEX IF NOT EXISTS idx_plugin_type ON gw_plugin(plugin_type);
CREATE INDEX IF NOT EXISTS idx_plugin_enabled ON gw_plugin(enabled);

-- 5. 路由-插件关联表
CREATE TABLE IF NOT EXISTS gw_route_plugin (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    route_id INTEGER NOT NULL,
    plugin_id INTEGER NOT NULL,
    config TEXT,
    priority_override INTEGER,
    enabled INTEGER NOT NULL DEFAULT 1,
    sort_order INTEGER NOT NULL DEFAULT 0,
    create_time TEXT,
    update_time TEXT
);

CREATE INDEX IF NOT EXISTS idx_rplugin_route ON gw_route_plugin(route_id);
CREATE INDEX IF NOT EXISTS idx_rplugin_plugin ON gw_route_plugin(plugin_id);
CREATE UNIQUE INDEX IF NOT EXISTS idx_rplugin_unique ON gw_route_plugin(route_id, plugin_id);

-- 6. 路由模板表
CREATE TABLE IF NOT EXISTS gw_route_template (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    template_name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    category VARCHAR(50),
    config TEXT NOT NULL,
    variables TEXT,
    is_system INTEGER NOT NULL DEFAULT 0,
    usage_count INTEGER NOT NULL DEFAULT 0,
    enabled INTEGER NOT NULL DEFAULT 1,
    create_time TEXT NOT NULL DEFAULT (datetime('now', 'localtime')),
    update_time TEXT NOT NULL DEFAULT (datetime('now', 'localtime')),
    create_by VARCHAR(50),
    update_by VARCHAR(50)
);

CREATE INDEX IF NOT EXISTS idx_template_category ON gw_route_template(category);

-- 7. 服务节点表
CREATE TABLE IF NOT EXISTS gw_service_node (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    node_id VARCHAR(100) NOT NULL UNIQUE,
    service_name VARCHAR(100) NOT NULL,
    node_name VARCHAR(100) NOT NULL,
    address VARCHAR(200) NOT NULL,
    port INTEGER NOT NULL,
    weight INTEGER NOT NULL DEFAULT 1,
    max_fails INTEGER NOT NULL DEFAULT 3,
    fail_timeout INTEGER NOT NULL DEFAULT 30,
    backup INTEGER NOT NULL DEFAULT 0,
    health_check_enabled INTEGER NOT NULL DEFAULT 1,
    health_check_interval INTEGER NOT NULL DEFAULT 30,
    health_check_timeout INTEGER NOT NULL DEFAULT 5,
    health_check_path VARCHAR(200) DEFAULT '/health',
    health_check_expected_status TEXT,
    status INTEGER NOT NULL DEFAULT 1,
    last_check_time TEXT,
    last_check_result INTEGER,
    metadata TEXT,
    create_time TEXT NOT NULL DEFAULT (datetime('now', 'localtime')),
    update_time TEXT NOT NULL DEFAULT (datetime('now', 'localtime')),
    create_by VARCHAR(50),
    update_by VARCHAR(50)
);

CREATE INDEX IF NOT EXISTS idx_node_service ON gw_service_node(service_name);
CREATE INDEX IF NOT EXISTS idx_node_status ON gw_service_node(status);

-- ====================================
-- RBAC System Tables
-- ====================================

-- 8. 用户表
CREATE TABLE IF NOT EXISTS sys_user (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    nickname VARCHAR(50) NOT NULL,
    email VARCHAR(100),
    mobile VARCHAR(20),
    avatar VARCHAR(500),
    dept_id INTEGER,
    status INTEGER NOT NULL DEFAULT 1,
    create_time TEXT NOT NULL DEFAULT (datetime('now', 'localtime')),
    update_time TEXT NOT NULL DEFAULT (datetime('now', 'localtime')),
    create_by VARCHAR(50),
    update_by VARCHAR(50)
);

CREATE INDEX IF NOT EXISTS idx_user_dept ON sys_user(dept_id);
CREATE INDEX IF NOT EXISTS idx_user_status ON sys_user(status);

-- 9. 角色表
CREATE TABLE IF NOT EXISTS sys_role (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    role_code VARCHAR(50) NOT NULL UNIQUE,
    role_name VARCHAR(50) NOT NULL,
    description VARCHAR(200),
    data_scope INTEGER DEFAULT 1,
    status INTEGER NOT NULL DEFAULT 1,
    create_time TEXT NOT NULL DEFAULT (datetime('now', 'localtime')),
    update_time TEXT NOT NULL DEFAULT (datetime('now', 'localtime')),
    create_by VARCHAR(50),
    update_by VARCHAR(50)
);

CREATE INDEX IF NOT EXISTS idx_role_status ON sys_role(status);

-- 10. 部门表
CREATE TABLE IF NOT EXISTS sys_dept (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    parent_id INTEGER NOT NULL DEFAULT 0,
    ancestors VARCHAR(500) DEFAULT '',
    dept_name VARCHAR(50) NOT NULL,
    dept_code VARCHAR(50),
    leader VARCHAR(50),
    phone VARCHAR(20),
    email VARCHAR(100),
    order_num INTEGER NOT NULL DEFAULT 0,
    status INTEGER NOT NULL DEFAULT 1,
    create_time TEXT NOT NULL DEFAULT (datetime('now', 'localtime')),
    update_time TEXT NOT NULL DEFAULT (datetime('now', 'localtime')),
    create_by VARCHAR(50),
    update_by VARCHAR(50)
);

CREATE INDEX IF NOT EXISTS idx_dept_parent ON sys_dept(parent_id);
CREATE INDEX IF NOT EXISTS idx_dept_status ON sys_dept(status);

-- 11. 菜单表
CREATE TABLE IF NOT EXISTS sys_menu (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    parent_id INTEGER NOT NULL DEFAULT 0,
    menu_name VARCHAR(50) NOT NULL,
    i18n_code VARCHAR(100),
    menu_type CHAR(1) NOT NULL,
    path VARCHAR(200),
    component VARCHAR(255),
    perms VARCHAR(100),
    icon VARCHAR(100),
    sort_order INTEGER NOT NULL DEFAULT 0,
    visible INTEGER NOT NULL DEFAULT 1,
    status INTEGER NOT NULL DEFAULT 1,
    create_time TEXT NOT NULL DEFAULT (datetime('now', 'localtime')),
    update_time TEXT NOT NULL DEFAULT (datetime('now', 'localtime')),
    create_by VARCHAR(50),
    update_by VARCHAR(50)
);

CREATE INDEX IF NOT EXISTS idx_menu_parent ON sys_menu(parent_id);
CREATE INDEX IF NOT EXISTS idx_menu_status ON sys_menu(status);

-- 12. 用户角色关联表
CREATE TABLE IF NOT EXISTS sys_user_role (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    role_id INTEGER NOT NULL,
    create_time TEXT NOT NULL DEFAULT (datetime('now', 'localtime')),
    UNIQUE(user_id, role_id)
);

CREATE INDEX IF NOT EXISTS idx_ur_user ON sys_user_role(user_id);
CREATE INDEX IF NOT EXISTS idx_ur_role ON sys_user_role(role_id);

-- 13. 角色菜单关联表
CREATE TABLE IF NOT EXISTS sys_role_menu (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    role_id INTEGER NOT NULL,
    menu_id INTEGER NOT NULL,
    create_time TEXT NOT NULL DEFAULT (datetime('now', 'localtime')),
    UNIQUE(role_id, menu_id)
);

CREATE INDEX IF NOT EXISTS idx_rm_role ON sys_role_menu(role_id);
CREATE INDEX IF NOT EXISTS idx_rm_menu ON sys_role_menu(menu_id);

-- 14. 角色部门关联表
CREATE TABLE IF NOT EXISTS sys_role_dept (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    role_id INTEGER NOT NULL,
    dept_id INTEGER NOT NULL,
    create_time TEXT NOT NULL DEFAULT (datetime('now', 'localtime')),
    UNIQUE(role_id, dept_id)
);

CREATE INDEX IF NOT EXISTS idx_rd_role ON sys_role_dept(role_id);
CREATE INDEX IF NOT EXISTS idx_rd_dept ON sys_role_dept(dept_id);

-- 15. 操作日志表
CREATE TABLE IF NOT EXISTS sys_operation_log (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    module VARCHAR(100),
    operation VARCHAR(100),
    method VARCHAR(200),
    request_url VARCHAR(500),
    params TEXT,
    result TEXT,
    error TEXT,
    duration INTEGER,
    operator VARCHAR(50),
    operator_id INTEGER,
    operator_ip VARCHAR(50),
    operator_location VARCHAR(200),
    browser VARCHAR(200),
    os VARCHAR(200),
    status INTEGER DEFAULT 0,
    operate_time TEXT NOT NULL DEFAULT (datetime('now', 'localtime'))
);

CREATE INDEX IF NOT EXISTS idx_log_operator ON sys_operation_log(operator_id);
CREATE INDEX IF NOT EXISTS idx_log_operate_time ON sys_operation_log(operate_time);

-- 16. 系统配置表
CREATE TABLE IF NOT EXISTS sys_config (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    config_key VARCHAR(100) NOT NULL UNIQUE,
    config_value TEXT,
    config_name VARCHAR(100),
    description VARCHAR(500),
    status INTEGER NOT NULL DEFAULT 1,
    create_time TEXT NOT NULL DEFAULT (datetime('now', 'localtime')),
    update_time TEXT NOT NULL DEFAULT (datetime('now', 'localtime')),
    create_by VARCHAR(50),
    update_by VARCHAR(50)
);

CREATE INDEX IF NOT EXISTS idx_config_status ON sys_config(status);