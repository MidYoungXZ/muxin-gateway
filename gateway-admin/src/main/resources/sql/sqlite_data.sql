-- Muxin Gateway SQLite Data Initialization
-- Initialize default data

-- ====================================
-- Gateway Core Data
-- ====================================

-- 1. 内置断言
INSERT INTO gw_predicate (predicate_name, predicate_type, description, config, is_system, enabled) VALUES
('路径前缀匹配', 'PATH', '匹配指定的路径前缀', '{"pattern": "/api/**"}', 1, 1),
('请求方法匹配', 'METHOD', '匹配HTTP请求方法', '{"methods": ["GET", "POST"]}', 1, 1),
('请求头匹配', 'HEADER', '匹配请求头', '{"name": "X-Request-Id", "regexp": ".*"}', 1, 1),
('查询参数匹配', 'QUERY', '匹配查询参数', '{"param": "token"}', 1, 1),
('Cookie匹配', 'COOKIE', '匹配Cookie', '{"name": "sessionId", "regexp": ".*"}', 1, 1),
('主机名匹配', 'HOST', '匹配主机名', '{"patterns": ["*.example.com"]}', 1, 1),
('远程地址匹配', 'REMOTE_ADDR', '匹配客户端IP地址', '{"sources": ["192.168.1.0/24"]}', 1, 1),
('时间范围匹配', 'BETWEEN', '匹配时间范围', '{"datetime1": "2024-01-01T00:00:00", "datetime2": "2024-12-31T23:59:59"}', 1, 1);

-- 2. 内置过滤器
INSERT INTO gw_filter (filter_name, filter_type, description, config, "order", is_system, enabled) VALUES
('请求ID', 'REQUEST_ID', '生成请求ID过滤器', '{"header-name": "X-Request-ID", "generate-if-missing": true}', 10, 1, 1),
('请求日志', 'REQUEST_LOG', '请求日志过滤器', '{"include-headers": true, "include-body": false}', 20, 1, 1),
('指标收集', 'METRICS', '指标收集过滤器', '{"collect-request-metrics": true}', 30, 1, 1),
('路径重写', 'PATH_REWRITE', '路径重写过滤器', '{}', 50, 1, 1);

-- 3. 内置路由模板
INSERT INTO gw_route_template (template_name, description, category, config, variables, is_system, enabled) VALUES
('基础HTTP服务模板', '适用于标准HTTP RESTful服务的基础模板', 'HTTP', 
'{"predicates": [{"type": "Path", "args": {"pattern": "/${serviceName}/**"}}], "filters": [{"type": "StripPrefix", "args": {"parts": 1}}]}',
'[{"name": "serviceName", "type": "string", "required": true, "description": "服务名称"}]', 1, 1);

-- ====================================
-- RBAC System Data
-- ====================================

-- 4. 部门数据
INSERT INTO sys_dept (id, parent_id, ancestors, dept_name, dept_code, order_num, leader, phone, email, status) VALUES
(1, 0, '0', 'Muxin科技', 'MUXIN_ROOT', 0, '张总', '13800138000', 'ceo@muxin.tech', 1),
(2, 1, '0,1', '研发中心', 'DEV_CENTER', 1, '李技术总监', '13800138001', 'dev@muxin.tech', 1),
(3, 1, '0,1', '产品中心', 'PRODUCT_CENTER', 2, '王产品总监', '13800138002', 'product@muxin.tech', 1),
(4, 1, '0,1', '运营中心', 'OPERATION_CENTER', 3, '赵运营总监', '13800138003', 'operation@muxin.tech', 1),
(5, 2, '0,1,2', '后端开发部', 'BACKEND_DEV', 1, '刘后端组长', '13800138011', 'backend@muxin.tech', 1),
(6, 2, '0,1,2', '前端开发部', 'FRONTEND_DEV', 2, '陈前端组长', '13800138012', 'frontend@muxin.tech', 1),
(7, 2, '0,1,2', '测试部', 'QA_DEPT', 3, '孙测试组长', '13800138013', 'qa@muxin.tech', 1);

-- 5. 用户数据 (密码: admin123)
INSERT INTO sys_user (id, username, password, nickname, email, mobile, dept_id, status) VALUES
(1, 'admin', '$2a$10$5Z1Kbm99AbBFN7y8Dd3.V.UGmeJX8nWKG47aPXXMuupC7kLe8lKIu', '超级管理员', 'admin@muxin.com', '13800138000', 1, 1);

-- 6. 角色数据
INSERT INTO sys_role (id, role_code, role_name, description, status) VALUES
(1, 'SUPER_ADMIN', '超级管理员', '系统超级管理员，拥有所有权限', 1),
(2, 'SYSTEM_ADMIN', '系统管理员', '负责系统管理相关配置', 1),
(3, 'ROUTE_ADMIN', '路由管理员', '负责网关路由配置管理', 1);

-- 7. 用户角色关联
INSERT INTO sys_user_role (user_id, role_id) VALUES
(1, 1);

-- 8. 菜单数据 - 一级菜单
INSERT INTO sys_menu (id, parent_id, menu_name, i18n_code, menu_type, path, component, perms, icon, sort_order, visible, status) VALUES
(1, 0, '路由管理', 'menu.routes', 'M', '/routes', '', '', 'Connection', 1, 1, 1),
(2, 0, '系统管理', 'menu.system', 'M', '/system', '', '', 'Setting', 2, 1, 1);

-- 二级菜单
INSERT INTO sys_menu (id, parent_id, menu_name, i18n_code, menu_type, path, component, perms, icon, sort_order, visible, status) VALUES
(100, 1, '路由列表', 'menu.routes.list', 'C', '/routes/list', 'routes/list/index', 'route:list', 'List', 1, 1, 1),
(101, 1, '服务节点', 'menu.routes.nodes', 'C', '/routes/nodes', 'routes/nodes/index', 'route:node:list', 'SetUp', 2, 1, 1),
(103, 1, '过滤器管理', 'menu.routes.filters', 'C', '/routes/filters', 'routes/filters/index', 'route:filter:list', 'Filter', 3, 1, 1),
(104, 1, '断言管理', 'menu.routes.predicates', 'C', '/routes/predicates', 'routes/predicates/index', 'route:predicate:list', 'Aim', 4, 1, 1),
(200, 2, '用户管理', 'menu.system.user', 'C', '/system/users', 'system/users/index', 'system:user:list', 'User', 1, 1, 1),
(201, 2, '角色管理', 'menu.system.role', 'C', '/system/roles', 'system/roles/index', 'system:role:list', 'UserFilled', 2, 1, 1),
(202, 2, '部门管理', 'menu.system.dept', 'C', '/system/departments', 'system/departments/index', 'system:dept:list', 'OfficeBuilding', 3, 1, 1),
(203, 2, '权限管理', 'menu.system.permission', 'C', '/system/permissions', 'system/permissions/index', 'system:menu:list', 'Menu', 4, 1, 1),
(204, 2, '操作日志', 'menu.system.log', 'C', '/system/operation-logs', 'system/operation-logs/index', 'system:log:list', 'Document', 5, 1, 1),
(205, 2, '系统配置', 'menu.system.config', 'C', '/system/config', 'system/config/index', 'system:config:list', 'Tools', 6, 1, 1);

-- 三级菜单（按钮权限）
INSERT INTO sys_menu (id, parent_id, menu_name, i18n_code, menu_type, path, component, perms, icon, sort_order, visible, status) VALUES
(1001, 100, '路由查看', '', 'F', '', '', 'route:view', '', 1, 1, 1),
(1002, 100, '路由新增', '', 'F', '', '', 'route:create', '', 2, 1, 1),
(1003, 100, '路由修改', '', 'F', '', '', 'route:update', '', 3, 1, 1),
(1004, 100, '路由删除', '', 'F', '', '', 'route:delete', '', 4, 1, 1),
(1011, 101, '节点查看', '', 'F', '', '', 'route:node:list', '', 1, 1, 1),
(1012, 101, '节点新增', '', 'F', '', '', 'route:node:create', '', 2, 1, 1),
(1013, 101, '节点修改', '', 'F', '', '', 'route:node:update', '', 3, 1, 1),
(1014, 101, '节点删除', '', 'F', '', '', 'route:node:delete', '', 4, 1, 1),
(2001, 200, '用户查看', '', 'F', '', '', 'system:user:view', '', 1, 1, 1),
(2002, 200, '用户新增', '', 'F', '', '', 'system:user:create', '', 2, 1, 1),
(2003, 200, '用户修改', '', 'F', '', '', 'system:user:update', '', 3, 1, 1),
(2004, 200, '用户删除', '', 'F', '', '', 'system:user:delete', '', 4, 1, 1),
(1031, 103, '过滤器查看', '', 'F', '', '', 'route:filter:view', '', 1, 1, 1),
(1032, 103, '过滤器新增', '', 'F', '', '', 'route:filter:create', '', 2, 1, 1),
(1033, 103, '过滤器修改', '', 'F', '', '', 'route:filter:update', '', 3, 1, 1),
(1034, 103, '过滤器删除', '', 'F', '', '', 'route:filter:delete', '', 4, 1, 1),
(1041, 104, '断言查看', '', 'F', '', '', 'route:predicate:view', '', 1, 1, 1),
(1042, 104, '断言新增', '', 'F', '', '', 'route:predicate:create', '', 2, 1, 1),
(1043, 104, '断言修改', '', 'F', '', '', 'route:predicate:update', '', 3, 1, 1),
(1044, 104, '断言删除', '', 'F', '', '', 'route:predicate:delete', '', 4, 1, 1);

-- 9. 角色菜单关联 - 超级管理员拥有所有权限
INSERT INTO sys_role_menu (role_id, menu_id) 
SELECT 1, id FROM sys_menu WHERE deleted = 0;

-- 系统管理员权限
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(2, 2), (2, 200), (2, 201), (2, 202), (2, 203), (2, 204), (2, 205),
(2, 2001), (2, 2002), (2, 2003), (2, 2004);

-- 路由管理员权限
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(3, 1), (3, 100), (3, 101), (3, 103), (3, 104),
(3, 1001), (3, 1002), (3, 1003), (3, 1004),
(3, 1011), (3, 1012), (3, 1013), (3, 1014),
(3, 1031), (3, 1032), (3, 1033), (3, 1034),
(3, 1041), (3, 1042), (3, 1043), (3, 1044);

-- 10. 示例路由
INSERT INTO gw_route (id, route_id, route_name, description, uri, metadata, "order", enabled) VALUES
(1, 'user-service', '用户服务路由', '转发到用户服务的所有请求', 'lb://user-service', '{"service": "user", "version": "1.0"}', 1, 1),
(2, 'order-service', '订单服务路由', '转发到订单服务的所有请求', 'lb://order-service', '{"service": "order", "version": "1.0"}', 2, 1),
(3, 'product-service', '商品服务路由', '转发到商品服务的所有请求', 'lb://payment-service', '{"service": "product", "version": "1.0"}', 3, 1);

-- 10.1 路由-断言关联
INSERT INTO gw_route_predicate (route_id, predicate_id, sort_order) VALUES
(1, 1, 1),
(1, 2, 2),
(2, 1, 1),
(2, 2, 2),
(3, 1, 1),
(3, 2, 2);

-- 10.2 路由-过滤器关联
INSERT INTO gw_route_filter (route_id, filter_id, sort_order) VALUES
(1, 1, 1),
(1, 2, 2),
(2, 1, 1),
(2, 2, 2),
(3, 1, 1),
(3, 2, 2);

-- 11. 系统配置数据
INSERT INTO sys_config (config_key, config_value, config_name, description, status) VALUES
('system.name', 'Muxin Gateway', '系统名称', '网关管理系统名称', 1),
('system.version', '1.0.0', '系统版本', '当前系统版本号', 1),
('system.logo', '/logo.png', '系统Logo', '系统Logo图片路径', 1),
('login.captcha.enabled', 'false', '验证码开关', '登录是否启用验证码', 1),
('login.max.retry', '5', '最大重试次数', '登录最大重试次数', 1),
('login.lock.time', '10', '锁定时间(分钟)', '账户锁定时间', 1),
('password.min.length', '6', '密码最小长度', '用户密码最小长度要求', 1),
('password.strength.check', 'false', '密码强度检查', '是否检查密码强度', 1),
('upload.max.size', '10485760', '上传文件大小限制', '上传文件最大字节数(默认10MB)', 1),
('upload.allowed.types', 'jpg,jpeg,png,gif,pdf,doc,docx,xls,xlsx', '允许上传的文件类型', '允许上传的文件扩展名', 1);

-- 12. 服务节点测试数据

-- user-service 服务
INSERT INTO gw_service_node (node_id, service_name, node_name, address, port, weight, max_fails, fail_timeout, backup, health_check_enabled, health_check_interval, health_check_timeout, health_check_path, health_check_expected_status, status, last_check_result, deleted) VALUES
('user-service-node-1', 'user-service', '用户服务节点1', '192.168.1.10', 8080, 100, 3, 30, 0, 1, 30, 5, '/health', '[200, 201]', 1, 1, 0),
('user-service-node-2', 'user-service', '用户服务节点2', '192.168.1.11', 8080, 100, 3, 30, 0, 1, 30, 5, '/health', '[200, 201]', 1, 1, 0),
('user-service-node-3', 'user-service', '用户服务节点3', '192.168.1.12', 8080, 50, 3, 30, 0, 1, 30, 5, '/health', '[200, 201]', 1, 0, 0),
('user-service-node-4', 'user-service', '用户服务节点4(备份)', '192.168.1.13', 8080, 100, 3, 30, 1, 1, 30, 5, '/health', '[200, 201]', 1, 1, 0);

-- order-service 服务
INSERT INTO gw_service_node (node_id, service_name, node_name, address, port, weight, max_fails, fail_timeout, backup, health_check_enabled, health_check_interval, health_check_timeout, health_check_path, health_check_expected_status, status, last_check_result, deleted) VALUES
('order-service-node-1', 'order-service', '订单服务节点1', '192.168.2.10', 8080, 100, 3, 30, 0, 1, 30, 5, '/health', '[200, 201]', 1, 1, 0),
('order-service-node-2', 'order-service', '订单服务节点2', '192.168.2.11', 8080, 100, 3, 30, 0, 1, 30, 5, '/health', '[200, 201]', 1, 1, 0),
('order-service-node-3', 'order-service', '订单服务节点3', '192.168.2.12', 8080, 100, 3, 30, 0, 1, 30, 5, '/health', '[200, 201]', 1, 1, 0);

-- payment-service 服务
INSERT INTO gw_service_node (node_id, service_name, node_name, address, port, weight, max_fails, fail_timeout, backup, health_check_enabled, health_check_interval, health_check_timeout, health_check_path, health_check_expected_status, status, last_check_result, deleted) VALUES
('payment-service-node-1', 'payment-service', '支付服务节点1', '192.168.3.10', 8080, 100, 3, 30, 0, 1, 30, 5, '/actuator/health', '[200]', 1, 1, 0),
('payment-service-node-2', 'payment-service', '支付服务节点2', '192.168.3.11', 8080, 100, 3, 30, 0, 1, 30, 5, '/actuator/health', '[200]', 1, 1, 0);

-- inventory-service 服务
INSERT INTO gw_service_node (node_id, service_name, node_name, address, port, weight, max_fails, fail_timeout, backup, health_check_enabled, health_check_interval, health_check_timeout, health_check_path, health_check_expected_status, status, last_check_result, deleted) VALUES
('inventory-service-node-1', 'inventory-service', '库存服务节点1', '192.168.4.10', 8080, 100, 3, 30, 0, 1, 30, 5, '/health', '[200, 201]', 1, 1, 0),
('inventory-service-node-2', 'inventory-service', '库存服务节点2', '192.168.4.11', 8080, 100, 3, 30, 0, 1, 30, 5, '/health', '[200, 201]', 1, 0, 0),
('inventory-service-node-3', 'inventory-service', '库存服务节点3', '192.168.4.12', 8080, 100, 3, 30, 0, 1, 30, 5, '/health', '[200, 201]', 0, NULL, 0);

-- notification-service 服务
INSERT INTO gw_service_node (node_id, service_name, node_name, address, port, weight, max_fails, fail_timeout, backup, health_check_enabled, health_check_interval, health_check_timeout, health_check_path, health_check_expected_status, status, last_check_result, deleted) VALUES
('notification-service-node-1', 'notification-service', '通知服务节点1', '192.168.5.10', 8080, 100, 3, 30, 0, 1, 30, 5, '/health', '[200]', 2, 1, 0);