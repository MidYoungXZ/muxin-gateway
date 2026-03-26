-- Muxin Gateway SQLite Data Initialization
-- Initialize default data

-- ====================================
-- RBAC System Data
-- ====================================

-- 1. 部门数据
INSERT INTO sys_dept (id, parent_id, ancestors, dept_name, dept_code, order_num, leader, phone, email, status) VALUES
(1, 0, '0', 'Muxin科技', 'MUXIN_ROOT', 0, '张总', '13800138000', 'ceo@muxin.tech', 1),
(2, 1, '0,1', '研发中心', 'DEV_CENTER', 1, '李技术总监', '13800138001', 'dev@muxin.tech', 1),
(3, 1, '0,1', '产品中心', 'PRODUCT_CENTER', 2, '王产品总监', '13800138002', 'product@muxin.tech', 1),
(4, 1, '0,1', '运营中心', 'OPERATION_CENTER', 3, '赵运营总监', '13800138003', 'operation@muxin.tech', 1),
(5, 2, '0,1,2', '后端开发部', 'BACKEND_DEV', 1, '刘后端组长', '13800138011', 'backend@muxin.tech', 1),
(6, 2, '0,1,2', '前端开发部', 'FRONTEND_DEV', 2, '陈前端组长', '13800138012', 'frontend@muxin.tech', 1),
(7, 2, '0,1,2', '测试部', 'QA_DEPT', 3, '孙测试组长', '13800138013', 'qa@muxin.tech', 1);

-- 2. 用户数据 (密码: admin123)
INSERT INTO sys_user (id, username, password, nickname, email, mobile, dept_id, status) VALUES
(1, 'admin', '$2a$10$9YgE9k/vkqKYsusczKuv3ut2TPXp7upG6r0Xi8ChSBB31uciUxva.', '超级管理员', 'admin@muxin.com', '13800138000', 1, 1);

-- 3. 角色数据
INSERT INTO sys_role (id, role_code, role_name, description, status) VALUES
(1, 'SUPER_ADMIN', '超级管理员', '系统超级管理员，拥有所有权限', 1),
(2, 'SYSTEM_ADMIN', '系统管理员', '负责系统管理相关配置', 1),
(3, 'ROUTE_ADMIN', '路由管理员', '负责网关路由配置管理', 1);

-- 4. 用户角色关联
INSERT INTO sys_user_role (user_id, role_id) VALUES
(1, 1);

-- 5. 菜单数据 - 一级菜单
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
(204, 2, '操作日志', 'menu.system.log', 'C', '/system/operation-logs', 'system/operation-logs/index', 'system:log:operation:list', 'Document', 5, 1, 1),
(205, 2, '系统配置', 'menu.system.config', 'C', '/system/config', 'system/config/index', 'system:config:list', 'Tools', 6, 1, 1);

-- 三级菜单（按钮权限）
INSERT INTO sys_menu (id, parent_id, menu_name, i18n_code, menu_type, path, component, perms, icon, sort_order, visible, status) VALUES
-- 路由列表按钮
(1001, 100, '路由查看', '', 'F', '', '', 'route:view', '', 1, 1, 1),
(1002, 100, '路由新增', '', 'F', '', '', 'route:create', '', 2, 1, 1),
(1003, 100, '路由修改', '', 'F', '', '', 'route:update', '', 3, 1, 1),
(1004, 100, '路由删除', '', 'F', '', '', 'route:delete', '', 4, 1, 1),
-- 服务节点按钮
(1011, 101, '节点查看', '', 'F', '', '', 'route:node:view', '', 1, 1, 1),
(1012, 101, '节点新增', '', 'F', '', '', 'route:node:create', '', 2, 1, 1),
(1013, 101, '节点修改', '', 'F', '', '', 'route:node:update', '', 3, 1, 1),
(1014, 101, '节点删除', '', 'F', '', '', 'route:node:delete', '', 4, 1, 1),
-- 过滤器管理按钮
(1031, 103, '过滤器查看', '', 'F', '', '', 'route:filter:view', '', 1, 1, 1),
(1032, 103, '过滤器新增', '', 'F', '', '', 'route:filter:create', '', 2, 1, 1),
(1033, 103, '过滤器修改', '', 'F', '', '', 'route:filter:update', '', 3, 1, 1),
(1034, 103, '过滤器删除', '', 'F', '', '', 'route:filter:delete', '', 4, 1, 1),
-- 断言管理按钮
(1041, 104, '断言查看', '', 'F', '', '', 'route:predicate:view', '', 1, 1, 1),
(1042, 104, '断言新增', '', 'F', '', '', 'route:predicate:create', '', 2, 1, 1),
(1043, 104, '断言修改', '', 'F', '', '', 'route:predicate:update', '', 3, 1, 1),
(1044, 104, '断言删除', '', 'F', '', '', 'route:predicate:delete', '', 4, 1, 1),
-- 用户管理按钮
(2001, 200, '用户查看', '', 'F', '', '', 'system:user:view', '', 1, 1, 1),
(2002, 200, '用户新增', '', 'F', '', '', 'system:user:create', '', 2, 1, 1),
(2003, 200, '用户修改', '', 'F', '', '', 'system:user:update', '', 3, 1, 1),
(2004, 200, '用户删除', '', 'F', '', '', 'system:user:delete', '', 4, 1, 1),
(2005, 200, '重置密码', '', 'F', '', '', 'system:user:resetPwd', '', 5, 1, 1),
-- 角色管理按钮
(2011, 201, '角色查看', '', 'F', '', '', 'system:role:view', '', 1, 1, 1),
(2012, 201, '角色新增', '', 'F', '', '', 'system:role:create', '', 2, 1, 1),
(2013, 201, '角色修改', '', 'F', '', '', 'system:role:update', '', 3, 1, 1),
(2014, 201, '角色删除', '', 'F', '', '', 'system:role:delete', '', 4, 1, 1),
(2015, 201, '分配权限', '', 'F', '', '', 'system:role:auth', '', 5, 1, 1),
-- 部门管理按钮
(2021, 202, '部门查看', '', 'F', '', '', 'system:dept:view', '', 1, 1, 1),
(2022, 202, '部门新增', '', 'F', '', '', 'system:dept:create', '', 2, 1, 1),
(2023, 202, '部门修改', '', 'F', '', '', 'system:dept:update', '', 3, 1, 1),
(2024, 202, '部门删除', '', 'F', '', '', 'system:dept:delete', '', 4, 1, 1),
-- 权限管理按钮
(2031, 203, '权限查看', '', 'F', '', '', 'system:menu:view', '', 1, 1, 1),
(2032, 203, '权限新增', '', 'F', '', '', 'system:menu:create', '', 2, 1, 1),
(2033, 203, '权限修改', '', 'F', '', '', 'system:menu:update', '', 3, 1, 1),
(2034, 203, '权限删除', '', 'F', '', '', 'system:menu:delete', '', 4, 1, 1),
-- 操作日志按钮
(2041, 204, '日志查看', '', 'F', '', '', 'system:log:operation:view', '', 1, 1, 1),
(2042, 204, '日志删除', '', 'F', '', '', 'system:log:operation:delete', '', 2, 1, 1),
(2043, 204, '清空日志', '', 'F', '', '', 'system:log:operation:clear', '', 3, 1, 1),
(2044, 204, '导出日志', '', 'F', '', '', 'system:log:operation:export', '', 4, 1, 1),
(2045, 204, '日志统计', '', 'F', '', '', 'system:log:operation:stats', '', 5, 1, 1),
(2046, 204, '清理日志', '', 'F', '', '', 'system:log:operation:clean', '', 6, 1, 1),
-- 系统配置按钮
(2051, 205, '配置查看', '', 'F', '', '', 'system:config:view', '', 1, 1, 1),
(2052, 205, '配置修改', '', 'F', '', '', 'system:config:update', '', 2, 1, 1);

-- 6. 角色菜单关联 - 超级管理员拥有所有权限
-- 一级菜单
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 1), (1, 2);
-- 二级菜单
INSERT INTO sys_role_menu (role_id, menu_id) VALUES 
(1, 100), (1, 101), (1, 103), (1, 104),
(1, 200), (1, 201), (1, 202), (1, 203), (1, 204), (1, 205);
-- 三级菜单（按钮权限）
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(1, 1001), (1, 1002), (1, 1003), (1, 1004),
(1, 1011), (1, 1012), (1, 1013), (1, 1014),
(1, 1031), (1, 1032), (1, 1033), (1, 1034),
(1, 1041), (1, 1042), (1, 1043), (1, 1044),
(1, 2001), (1, 2002), (1, 2003), (1, 2004), (1, 2005),
(1, 2011), (1, 2012), (1, 2013), (1, 2014), (1, 2015),
(1, 2021), (1, 2022), (1, 2023), (1, 2024),
(1, 2031), (1, 2032), (1, 2033), (1, 2034),
(1, 2041), (1, 2042), (1, 2043), (1, 2044), (1, 2045), (1, 2046),
(1, 2051), (1, 2052);

-- 系统管理员权限
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(2, 2), (2, 200), (2, 201), (2, 202), (2, 203), (2, 204), (2, 205),
(2, 2001), (2, 2002), (2, 2003), (2, 2004), (2, 2005),
(2, 2011), (2, 2012), (2, 2013), (2, 2014), (2, 2015),
(2, 2021), (2, 2022), (2, 2023), (2, 2024),
(2, 2031), (2, 2032), (2, 2033), (2, 2034),
(2, 2041), (2, 2042), (2, 2043), (2, 2044), (2, 2045), (2, 2046),
(2, 2051), (2, 2052);

-- 路由管理员权限
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(3, 1), (3, 100), (3, 101), (3, 103), (3, 104),
(3, 1001), (3, 1002), (3, 1003), (3, 1004),
(3, 1011), (3, 1012), (3, 1013), (3, 1014),
(3, 1031), (3, 1032), (3, 1033), (3, 1034),
(3, 1041), (3, 1042), (3, 1043), (3, 1044);

-- 7. 系统配置数据
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