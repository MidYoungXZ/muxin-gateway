-- ========================================
-- Muxin Gateway 系统基础数据初始化脚本
-- 包含：部门数据、菜单权限数据
-- 执行前请确保相关表已创建
-- ========================================

-- 清理历史数据（可选，谨慎执行）
-- DELETE FROM sys_role_menu WHERE role_id > 0;
-- DELETE FROM sys_user_role WHERE user_id > 1;
-- DELETE FROM sys_menu WHERE id > 0;
-- DELETE FROM sys_dept WHERE id > 0;

-- ========================================
-- 1. 初始化部门数据
-- ========================================
INSERT INTO sys_dept (id, parent_id, dept_name, dept_code, ancestors, order_num, leader, phone, email, status, create_time, update_time, create_by, update_by, deleted) VALUES
(1, 0, 'Muxin科技', 'MUXIN_ROOT', '0', 0, '张总', '13800138000', 'ceo@muxin.tech', 1, NOW(), NOW(), 'system', 'system', 0),
(2, 1, '研发中心', 'DEV_CENTER', '0,1', 1, '李技术总监', '13800138001', 'dev@muxin.tech', 1, NOW(), NOW(), 'system', 'system', 0),
(3, 1, '产品中心', 'PRODUCT_CENTER', '0,1', 2, '王产品总监', '13800138002', 'product@muxin.tech', 1, NOW(), NOW(), 'system', 'system', 0),
(4, 1, '运营中心', 'OPERATION_CENTER', '0,1', 3, '赵运营总监', '13800138003', 'operation@muxin.tech', 1, NOW(), NOW(), 'system', 'system', 0),
(5, 2, '后端开发部', 'BACKEND_DEV', '0,1,2', 1, '刘后端组长', '13800138011', 'backend@muxin.tech', 1, NOW(), NOW(), 'system', 'system', 0),
(6, 2, '前端开发部', 'FRONTEND_DEV', '0,1,2', 2, '陈前端组长', '13800138012', 'frontend@muxin.tech', 1, NOW(), NOW(), 'system', 'system', 0),
(7, 2, '测试部', 'QA_DEPT', '0,1,2', 3, '孙测试组长', '13800138013', 'qa@muxin.tech', 1, NOW(), NOW(), 'system', 'system', 0),
(8, 3, '产品设计部', 'PRODUCT_DESIGN', '0,1,3', 1, '周设计师', '13800138021', 'design@muxin.tech', 1, NOW(), NOW(), 'system', 'system', 0),
(9, 4, '市场部', 'MARKETING_DEPT', '0,1,4', 1, '吴市场经理', '13800138031', 'marketing@muxin.tech', 1, NOW(), NOW(), 'system', 'system', 0),
(10, 4, '客服部', 'CUSTOMER_SERVICE', '0,1,4', 2, '郑客服主管', '13800138032', 'service@muxin.tech', 1, NOW(), NOW(), 'system', 'system', 0);

-- ========================================
-- 2. 初始化菜单权限数据
-- ========================================

-- 一级菜单（目录）
INSERT INTO sys_menu (id, parent_id, menu_name, i18n_code, menu_type, path, component, perms, icon, sort_order, visible, status, create_time, update_time, create_by, update_by, deleted) VALUES
(1, 0, '路由管理', 'menu.routes', 'M', '/routes', '', '', 'Connection', 1, 1, 1, NOW(), NOW(), 'system', 'system', 0),
(2, 0, '系统管理', 'menu.system', 'M', '/system', '', '', 'Setting', 2, 1, 1, NOW(), NOW(), 'system', 'system', 0);

-- 二级菜单（页面）
INSERT INTO sys_menu (id, parent_id, menu_name, i18n_code, menu_type, path, component, perms, icon, sort_order, visible, status, create_time, update_time, create_by, update_by, deleted) VALUES
-- 路由管理子菜单
(100, 1, '路由列表', 'menu.routes.list', 'C', '/routes/list', 'routes/list/index', 'route:list', 'List', 1, 1, 1, NOW(), NOW(), 'system', 'system', 0),
(101, 1, '服务节点', 'menu.routes.nodes', 'C', '/routes/nodes', 'routes/nodes/index', 'route:node:list', 'SetUp', 2, 1, 1, NOW(), NOW(), 'system', 'system', 0),
(103, 1, '过滤器管理', 'menu.routes.filters', 'C', '/routes/filters', 'routes/filters/index', 'route:filter:list', 'Filter', 3, 1, 1, NOW(), NOW(), 'system', 'system', 0),
(104, 1, '断言管理', 'menu.routes.predicates', 'C', '/routes/predicates', 'routes/predicates/index', 'route:predicate:list', 'Aim', 4, 1, 1, NOW(), NOW(), 'system', 'system', 0),

-- 系统管理子菜单
(200, 2, '用户管理', 'menu.system.user', 'C', '/system/users', 'system/users/index', 'system:user:list', 'User', 1, 1, 1, NOW(), NOW(), 'system', 'system', 0),
(201, 2, '角色管理', 'menu.system.role', 'C', '/system/roles', 'system/roles/index', 'system:role:list', 'UserFilled', 2, 1, 1, NOW(), NOW(), 'system', 'system', 0),
(202, 2, '部门管理', 'menu.system.dept', 'C', '/system/departments', 'system/departments/index', 'system:dept:list', 'OfficeBuilding', 3, 1, 1, NOW(), NOW(), 'system', 'system', 0),
(203, 2, '权限管理', 'menu.system.permission', 'C', '/system/permissions', 'system/permissions/index', 'system:menu:list', 'Menu', 4, 1, 1, NOW(), NOW(), 'system', 'system', 0),
(204, 2, '操作日志', 'menu.system.log', 'C', '/system/operation-logs', 'system/operation-logs/index', 'system:log:operation:list', 'Document', 5, 1, 1, NOW(), NOW(), 'system', 'system', 0),
(205, 2, '系统配置', 'menu.system.config', 'C', '/system/config', 'system/config/index', 'system:config:list', 'Tools', 6, 1, 1, NOW(), NOW(), 'system', 'system', 0);

-- 三级菜单（按钮权限）
INSERT INTO sys_menu (id, parent_id, menu_name, i18n_code, menu_type, path, component, perms, icon, sort_order, visible, status, create_time, update_time, create_by, update_by, deleted) VALUES
-- 路由列表按钮
(1001, 100, '路由查看', '', 'F', '', '', 'route:view', '', 1, 1, 1, NOW(), NOW(), 'system', 'system', 0),
(1002, 100, '路由新增', '', 'F', '', '', 'route:create', '', 2, 1, 1, NOW(), NOW(), 'system', 'system', 0),
(1003, 100, '路由修改', '', 'F', '', '', 'route:update', '', 3, 1, 1, NOW(), NOW(), 'system', 'system', 0),
(1004, 100, '路由删除', '', 'F', '', '', 'route:delete', '', 4, 1, 1, NOW(), NOW(), 'system', 'system', 0),

-- 服务节点按钮
(1011, 101, '节点查看', '', 'F', '', '', 'route:node:view', '', 1, 1, 1, NOW(), NOW(), 'system', 'system', 0),
(1012, 101, '节点新增', '', 'F', '', '', 'route:node:create', '', 2, 1, 1, NOW(), NOW(), 'system', 'system', 0),
(1013, 101, '节点修改', '', 'F', '', '', 'route:node:update', '', 3, 1, 1, NOW(), NOW(), 'system', 'system', 0),
(1014, 101, '节点删除', '', 'F', '', '', 'route:node:delete', '', 4, 1, 1, NOW(), NOW(), 'system', 'system', 0),

-- 过滤器管理按钮
(1031, 103, '过滤器查看', '', 'F', '', '', 'route:filter:view', '', 1, 1, 1, NOW(), NOW(), 'system', 'system', 0),
(1032, 103, '过滤器新增', '', 'F', '', '', 'route:filter:create', '', 2, 1, 1, NOW(), NOW(), 'system', 'system', 0),
(1033, 103, '过滤器修改', '', 'F', '', '', 'route:filter:update', '', 3, 1, 1, NOW(), NOW(), 'system', 'system', 0),
(1034, 103, '过滤器删除', '', 'F', '', '', 'route:filter:delete', '', 4, 1, 1, NOW(), NOW(), 'system', 'system', 0),

-- 断言管理按钮
(1041, 104, '断言查看', '', 'F', '', '', 'route:predicate:view', '', 1, 1, 1, NOW(), NOW(), 'system', 'system', 0),
(1042, 104, '断言新增', '', 'F', '', '', 'route:predicate:create', '', 2, 1, 1, NOW(), NOW(), 'system', 'system', 0),
(1043, 104, '断言修改', '', 'F', '', '', 'route:predicate:update', '', 3, 1, 1, NOW(), NOW(), 'system', 'system', 0),
(1044, 104, '断言删除', '', 'F', '', '', 'route:predicate:delete', '', 4, 1, 1, NOW(), NOW(), 'system', 'system', 0),

-- 用户管理按钮
(2001, 200, '用户查看', '', 'F', '', '', 'system:user:view', '', 1, 1, 1, NOW(), NOW(), 'system', 'system', 0),
(2002, 200, '用户新增', '', 'F', '', '', 'system:user:create', '', 2, 1, 1, NOW(), NOW(), 'system', 'system', 0),
(2003, 200, '用户修改', '', 'F', '', '', 'system:user:update', '', 3, 1, 1, NOW(), NOW(), 'system', 'system', 0),
(2004, 200, '用户删除', '', 'F', '', '', 'system:user:delete', '', 4, 1, 1, NOW(), NOW(), 'system', 'system', 0),
(2005, 200, '重置密码', '', 'F', '', '', 'system:user:resetPwd', '', 5, 1, 1, NOW(), NOW(), 'system', 'system', 0),

-- 角色管理按钮
(2011, 201, '角色查看', '', 'F', '', '', 'system:role:view', '', 1, 1, 1, NOW(), NOW(), 'system', 'system', 0),
(2012, 201, '角色新增', '', 'F', '', '', 'system:role:create', '', 2, 1, 1, NOW(), NOW(), 'system', 'system', 0),
(2013, 201, '角色修改', '', 'F', '', '', 'system:role:update', '', 3, 1, 1, NOW(), NOW(), 'system', 'system', 0),
(2014, 201, '角色删除', '', 'F', '', '', 'system:role:delete', '', 4, 1, 1, NOW(), NOW(), 'system', 'system', 0),
(2015, 201, '分配权限', '', 'F', '', '', 'system:role:auth', '', 5, 1, 1, NOW(), NOW(), 'system', 'system', 0),

-- 部门管理按钮
(2021, 202, '部门查看', '', 'F', '', '', 'system:dept:view', '', 1, 1, 1, NOW(), NOW(), 'system', 'system', 0),
(2022, 202, '部门新增', '', 'F', '', '', 'system:dept:create', '', 2, 1, 1, NOW(), NOW(), 'system', 'system', 0),
(2023, 202, '部门修改', '', 'F', '', '', 'system:dept:update', '', 3, 1, 1, NOW(), NOW(), 'system', 'system', 0),
(2024, 202, '部门删除', '', 'F', '', '', 'system:dept:delete', '', 4, 1, 1, NOW(), NOW(), 'system', 'system', 0),

-- 权限管理按钮
(2031, 203, '权限查看', '', 'F', '', '', 'system:menu:view', '', 1, 1, 1, NOW(), NOW(), 'system', 'system', 0),
(2032, 203, '权限新增', '', 'F', '', '', 'system:menu:create', '', 2, 1, 1, NOW(), NOW(), 'system', 'system', 0),
(2033, 203, '权限修改', '', 'F', '', '', 'system:menu:update', '', 3, 1, 1, NOW(), NOW(), 'system', 'system', 0),
(2034, 203, '权限删除', '', 'F', '', '', 'system:menu:delete', '', 4, 1, 1, NOW(), NOW(), 'system', 'system', 0),

-- 操作日志按钮
(2041, 204, '日志查看', '', 'F', '', '', 'system:log:operation:view', '', 1, 1, 1, NOW(), NOW(), 'system', 'system', 0),
(2042, 204, '日志删除', '', 'F', '', '', 'system:log:operation:delete', '', 2, 1, 1, NOW(), NOW(), 'system', 'system', 0),
(2043, 204, '清空日志', '', 'F', '', '', 'system:log:operation:clear', '', 3, 1, 1, NOW(), NOW(), 'system', 'system', 0),
(2044, 204, '导出日志', '', 'F', '', '', 'system:log:operation:export', '', 4, 1, 1, NOW(), NOW(), 'system', 'system', 0),
(2045, 204, '日志统计', '', 'F', '', '', 'system:log:operation:stats', '', 5, 1, 1, NOW(), NOW(), 'system', 'system', 0),
(2046, 204, '清理日志', '', 'F', '', '', 'system:log:operation:clean', '', 6, 1, 1, NOW(), NOW(), 'system', 'system', 0),

-- 系统配置按钮
(2051, 205, '配置查看', '', 'F', '', '', 'system:config:view', '', 1, 1, 1, NOW(), NOW(), 'system', 'system', 0),
(2052, 205, '配置修改', '', 'F', '', '', 'system:config:update', '', 2, 1, 1, NOW(), NOW(), 'system', 'system', 0);

-- ========================================
-- 3. 角色权限分配
-- ========================================

-- 超级管理员拥有所有权限
INSERT INTO sys_role_menu (role_id, menu_id) 
SELECT 1, id FROM sys_menu WHERE deleted = 0;

-- 系统管理员（系统管理相关权限）
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
-- 系统管理目录及其所有子权限
(2, 2),
(2, 200), (2, 2001), (2, 2002), (2, 2003), (2, 2004), (2, 2005),
(2, 201), (2, 2011), (2, 2012), (2, 2013), (2, 2014), (2, 2015),
(2, 202), (2, 2021), (2, 2022), (2, 2023), (2, 2024),
(2, 203), (2, 2031), (2, 2032), (2, 2033), (2, 2034),
(2, 204), (2, 2041), (2, 2042), (2, 2043), (2, 2044), (2, 2045), (2, 2046),
(2, 205), (2, 2051), (2, 2052);

-- 路由管理员（路由管理相关权限）
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
-- 路由管理目录及其所有子权限
(3, 1),
(3, 100), (3, 1001), (3, 1002), (3, 1003), (3, 1004),
(3, 101), (3, 1011), (3, 1012), (3, 1013), (3, 1014),
(3, 103), (3, 1031), (3, 1032), (3, 1033), (3, 1034),
(3, 104), (3, 1041), (3, 1042), (3, 1043), (3, 1044);

-- ========================================
-- 输出统计信息
-- ========================================
SELECT 
    '部门数据' as '类型', 
    COUNT(*) as '数量' 
FROM sys_dept WHERE deleted = 0
UNION ALL
SELECT 
    '菜单权限', 
    COUNT(*) 
FROM sys_menu WHERE deleted = 0
UNION ALL
SELECT 
    '角色权限关联', 
    COUNT(*) 
FROM sys_role_menu;

-- ========================================
-- 初始化完成
-- 已创建：
-- - 10个部门（含总公司及各业务部门）
-- - 完整的三级菜单权限结构
--   - 路由管理：路由列表、服务节点、过滤器管理、断言管理
--   - 系统管理：用户管理、角色管理、部门管理、权限管理、操作日志、系统配置
-- - 角色权限分配
-- ========================================