-- Muxin Gateway SQLite Data Initialization
-- Initialize default data

-- ====================================
-- 1. Department Data
-- ====================================

INSERT INTO sys_dept (id, parent_id, ancestors, dept_name, dept_code, order_num, leader, phone, email, status) VALUES
(1, 0, '0', 'Muxin科技', 'MUXIN_ROOT', 0, '管理员', '13800138000', 'admin@muxin.com', 1),
(2, 1, '0,1', '研发中心', 'DEV_CENTER', 1, '张三', '13800138001', 'dev@muxin.com', 1),
(3, 1, '0,1', '产品中心', 'PRODUCT_CENTER', 2, '李四', '13800138002', 'product@muxin.com', 1),
(4, 1, '0,1', '运营中心', 'OPERATION_CENTER', 3, '王五', '13800138003', 'ops@muxin.com', 1),
(5, 2, '0,1,2', '后端开发部', 'BACKEND_DEV', 1, '赵六', '13800138004', 'backend@muxin.com', 1),
(6, 2, '0,1,2', '前端开发部', 'FRONTEND_DEV', 2, '钱七', '13800138005', 'frontend@muxin.com', 1),
(7, 2, '0,1,2', '测试部', 'QA_DEPT', 3, '孙八', '13800138006', 'qa@muxin.com', 1),
(8, 3, '0,1,3', '产品设计部', 'PRODUCT_DESIGN', 1, '周九', '13800138007', 'design@muxin.com', 1),
(9, 4, '0,1,4', '市场部', 'MARKETING_DEPT', 1, '吴十', '13800138008', 'market@muxin.com', 1),
(10, 4, '0,1,4', '客服部', 'CUSTOMER_SERVICE', 2, '郑十一', '13800138009', 'cs@muxin.com', 1);

-- ====================================
-- 2. User Data
-- ====================================

INSERT INTO sys_user (id, username, password, nickname, email, mobile, dept_id, status, create_by, update_by) VALUES
(1, 'admin', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '超级管理员', 'admin@muxin.com', '13800138000', 1, 1, 'system', 'system'),
(2, 'gateway_admin', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '网关管理员', 'gateway@muxin.com', '13800138001', 2, 1, 'system', 'system');

-- ====================================
-- 3. Role Data
-- ====================================

INSERT INTO sys_role (id, role_code, role_name, description, data_scope, status, create_by, update_by) VALUES
(1, 'SUPER_ADMIN', '超级管理员', '系统超级管理员，拥有所有权限', 1, 1, 'system', 'system'),
(2, 'ADMIN', '系统管理员', '系统管理员，拥有大部分管理权限', 2, 1, 'system', 'system'),
(3, 'GATEWAY_ADMIN', '网关管理员', '网关管理员，负责网关路由和插件配置', 4, 1, 'system', 'system');

-- ====================================
-- 4. User-Role Association
-- ====================================

INSERT INTO sys_user_role (user_id, role_id) VALUES
(1, 1),
(2, 3);

-- ====================================
-- 5. Menu Data
-- ====================================

-- Top-level menus
INSERT INTO sys_menu (id, parent_id, menu_name, i18n_code, menu_type, path, component, perms, icon, sort_order, visible, status) VALUES
(1, 0, '路由管理', 'menu.routes', 'M', '/routes', NULL, NULL, 'Guide', 1, 1, 1),
(2, 0, '系统管理', 'menu.system', 'M', '/system', NULL, NULL, 'Setting', 2, 1, 1);

-- Route management sub-menus
INSERT INTO sys_menu (id, parent_id, menu_name, i18n_code, menu_type, path, component, perms, icon, sort_order, visible, status) VALUES
(10, 1, '路由', 'menu.routes.list', 'C', '/routes/list', 'routes/list/index', 'route:list', 'List', 1, 1, 1),
(11, 1, '服务', 'menu.routes.nodes', 'C', '/routes/nodes', 'routes/nodes/index', 'route:node:list', 'Connection', 2, 1, 1),
(105, 1, '插件', 'menu.routes.plugins', 'C', '/routes/plugins', 'routes/plugins/index', 'route:plugin:list', 'SetUp', 3, 1, 1);

-- Route list buttons
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, perms, sort_order, visible, status) VALUES
(101, 10, '路由查看', 'F', 'route:detail', 1, 1, 1),
(102, 10, '路由新增', 'F', 'route:create', 2, 1, 1),
(103, 10, '路由修改', 'F', 'route:update', 3, 1, 1),
(104, 10, '路由删除', 'F', 'route:delete', 4, 1, 1);

-- Service node buttons
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, perms, sort_order, visible, status) VALUES
(111, 11, '节点查看', 'F', 'route:node:detail', 1, 1, 1),
(112, 11, '节点新增', 'F', 'route:node:create', 2, 1, 1),
(113, 11, '节点修改', 'F', 'route:node:update', 3, 1, 1),
(114, 11, '节点删除', 'F', 'route:node:delete', 4, 1, 1);

-- Plugin management buttons
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, perms, sort_order, visible, status) VALUES
(1051, 105, '插件查看', 'F', 'route:plugin:detail', 1, 1, 1),
(1052, 105, '插件新增', 'F', 'route:plugin:create', 2, 1, 1),
(1053, 105, '插件修改', 'F', 'route:plugin:update', 3, 1, 1),
(1054, 105, '插件删除', 'F', 'route:plugin:delete', 4, 1, 1);

-- System management sub-menus
INSERT INTO sys_menu (id, parent_id, menu_name, i18n_code, menu_type, path, component, perms, icon, sort_order, visible, status) VALUES
(20, 2, '用户管理', 'menu.system.users', 'C', '/system/users', 'system/users/index', 'system:user:list', 'User', 1, 1, 1),
(21, 2, '角色管理', 'menu.system.roles', 'C', '/system/roles', 'system/roles/index', 'system:role:list', 'UserFilled', 2, 1, 1),
(22, 2, '部门管理', 'menu.system.depts', 'C', '/system/depts', 'system/departments/index', 'system:dept:list', 'OfficeBuilding', 3, 1, 1);

-- User management buttons
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, perms, sort_order, visible, status) VALUES
(201, 20, '用户查看', 'F', 'system:user:detail', 1, 1, 1),
(202, 20, '用户新增', 'F', 'system:user:create', 2, 1, 1),
(203, 20, '用户修改', 'F', 'system:user:update', 3, 1, 1),
(204, 20, '用户删除', 'F', 'system:user:delete', 4, 1, 1),
(205, 20, '重置密码', 'F', 'system:user:resetPwd', 5, 1, 1);

-- Role management buttons
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, perms, sort_order, visible, status) VALUES
(211, 21, '角色查看', 'F', 'system:role:detail', 1, 1, 1),
(212, 21, '角色新增', 'F', 'system:role:create', 2, 1, 1),
(213, 21, '角色修改', 'F', 'system:role:update', 3, 1, 1),
(214, 21, '角色删除', 'F', 'system:role:delete', 4, 1, 1),
(215, 21, '分配权限', 'F', 'system:role:auth', 5, 1, 1);

-- Department management buttons
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, perms, sort_order, visible, status) VALUES
(221, 22, '部门查看', 'F', 'system:dept:detail', 1, 1, 1),
(222, 22, '部门新增', 'F', 'system:dept:create', 2, 1, 1),
(223, 22, '部门修改', 'F', 'system:dept:update', 3, 1, 1),
(224, 22, '部门删除', 'F', 'system:dept:delete', 4, 1, 1);

-- ====================================
-- 6. Role-Menu Association
-- ====================================

-- SUPER_ADMIN gets all menus (without menu management ID=23)
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(1, 1), (1, 2),
(1, 10), (1, 11), (1, 105),
(1, 101), (1, 102), (1, 103), (1, 104),
(1, 111), (1, 112), (1, 113), (1, 114),
(1, 1051), (1, 1052), (1, 1053), (1, 1054),
(1, 20), (1, 21), (1, 22),
(1, 201), (1, 202), (1, 203), (1, 204), (1, 205),
(1, 211), (1, 212), (1, 213), (1, 214), (1, 215),
(1, 221), (1, 222), (1, 223), (1, 224);

-- GATEWAY_ADMIN gets route management menus only
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(3, 1),
(3, 10), (3, 11), (3, 105),
(3, 101), (3, 102), (3, 103), (3, 104),
(3, 111), (3, 112), (3, 113), (3, 114),
(3, 1051), (3, 1052), (3, 1053), (3, 1054);

-- ====================================
-- 7. System Config Data
-- ====================================

INSERT INTO sys_config (id, config_key, config_value, config_name, description, status, create_by, update_by) VALUES
(1, 'sys.account.initPassword', '123456', '初始密码', '用户初始密码', 1, 'system', 'system'),
(2, 'sys.account.register', 'false', '注册开关', '是否开放用户注册', 1, 'system', 'system');

-- ====================================
-- 8. Preset Plugin Data (FILTER type only)
-- ====================================

INSERT INTO gw_plugin (plugin_name, plugin_type, description, schema, default_config, default_priority, phase, icon, is_system, enabled) VALUES
('rate-limit', 'FILTER', '请求限流',
 '{"type":"object","properties":{"rate":{"type":"number","minimum":1,"title":"每秒请求数(QPS)"},"burst":{"type":"number","minimum":0,"default":0,"title":"突发容量"},"key":{"type":"string","enum":["remote_addr","consumer_name","header:X-User-Id"],"default":"remote_addr","title":"限流维度"},"rejectedCode":{"type":"integer","default":429,"title":"拒绝响应码"}},"required":["rate"]}',
 '{"burst":0,"key":"remote_addr","rejectedCode":429}',
 6000, 'FILTER_PRE', '⚡', 1, 1);

INSERT INTO gw_plugin (plugin_name, plugin_type, description, schema, default_config, default_priority, phase, icon, is_system, enabled) VALUES
('cors', 'FILTER', '跨域处理',
 '{"type":"object","properties":{"allowOrigins":{"type":"string","default":"*","title":"允许的Origin"},"allowMethods":{"type":"string","default":"*","title":"允许的方法"},"allowHeaders":{"type":"string","default":"*","title":"允许的Header"},"allowCredentials":{"type":"boolean","default":false,"title":"允许凭证"},"maxAge":{"type":"integer","default":3600,"title":"预检缓存时间"}}}',
 '{"allowOrigins":"*","allowMethods":"*","allowHeaders":"*","allowCredentials":false,"maxAge":3600}',
 4000, 'FILTER_PRE', '⚡', 1, 1);

INSERT INTO gw_plugin (plugin_name, plugin_type, description, schema, default_config, default_priority, phase, icon, is_system, enabled) VALUES
('timeout', 'FILTER', '超时控制',
 '{"type":"object","properties":{"connectTimeout":{"type":"integer","minimum":100,"default":5000,"title":"连接超时(ms)"},"responseTimeout":{"type":"integer","minimum":1000,"default":30000,"title":"响应超时(ms)"}}}',
 '{"connectTimeout":5000,"responseTimeout":30000}',
 3000, 'FILTER_PRE', '⚡', 1, 1);

INSERT INTO gw_plugin (plugin_name, plugin_type, description, schema, default_config, default_priority, phase, icon, is_system, enabled) VALUES
('circuit-breaker', 'FILTER', '熔断器',
 '{"type":"object","properties":{"failureThreshold":{"type":"integer","minimum":1,"default":5,"title":"失败阈值"},"successThreshold":{"type":"integer","minimum":1,"default":3,"title":"恢复阈值"},"timeout":{"type":"integer","minimum":1000,"default":60000,"title":"熔断时间(ms)"}}}',
 '{"failureThreshold":5,"successThreshold":3,"timeout":60000}',
 5500, 'FILTER_PRE', '⚡', 1, 1);

INSERT INTO gw_plugin (plugin_name, plugin_type, description, schema, default_config, default_priority, phase, icon, is_system, enabled) VALUES
('request-rewrite', 'FILTER', '请求重写',
 '{"type":"object","properties":{"pathRegex":{"type":"string","title":"路径正则"},"pathReplacement":{"type":"string","title":"替换路径"},"headersToAdd":{"type":"object","title":"添加Header"},"headersToRemove":{"type":"array","items":{"type":"string"},"title":"移除Header"}}}',
 '{}',
 5000, 'FILTER_PRE', '⚡', 1, 1);

INSERT INTO gw_plugin (plugin_name, plugin_type, description, schema, default_config, default_priority, phase, icon, is_system, enabled) VALUES
('response-rewrite', 'FILTER', '响应重写',
 '{"type":"object","properties":{"headersToAdd":{"type":"object","title":"添加Header"},"headersToRemove":{"type":"array","items":{"type":"string"},"title":"移除Header"},"bodyRegex":{"type":"string","title":"Body正则"},"bodyReplacement":{"type":"string","title":"Body替换"}}}',
 '{}',
 2000, 'FILTER_POST', '⚡', 1, 1);