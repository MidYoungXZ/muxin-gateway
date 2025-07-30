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
(1, 0, '系统管理', 'menu.system', 'M', '/system', '', '', 'Setting', 1, 1, 1, NOW(), NOW(), 'system', 'system', 0),
(2, 0, '网关管理', 'menu.gateway', 'M', '/gateway', '', '', 'Connection', 2, 1, 1, NOW(), NOW(), 'system', 'system', 0),
(3, 0, '监控管理', 'menu.monitor', 'M', '/monitor', '', '', 'Monitor', 3, 1, 1, NOW(), NOW(), 'system', 'system', 0);

-- 二级菜单（页面）
INSERT INTO sys_menu (id, parent_id, menu_name, i18n_code, menu_type, path, component, perms, icon, sort_order, visible, status, create_time, update_time, create_by, update_by, deleted) VALUES
-- 系统管理子菜单
(100, 1, '用户管理', 'menu.system.user', 'C', '/system/users', 'system/users/index', 'system:user:list', 'User', 1, 1, 1, NOW(), NOW(), 'system', 'system', 0),
(101, 1, '角色管理', 'menu.system.role', 'C', '/system/roles', 'system/roles/index', 'system:role:list', 'UserFilled', 2, 1, 1, NOW(), NOW(), 'system', 'system', 0),
(102, 1, '权限管理', 'menu.system.permission', 'C', '/system/permissions', 'system/permissions/index', 'system:menu:list', 'Menu', 3, 1, 1, NOW(), NOW(), 'system', 'system', 0),
(103, 1, '部门管理', 'menu.system.dept', 'C', '/system/departments', 'system/departments/index', 'system:dept:list', 'OfficeBuilding', 4, 1, 1, NOW(), NOW(), 'system', 'system', 0),

-- 网关管理子菜单
(200, 2, '路由管理', 'menu.gateway.route', 'C', '/gateway/routes', 'gateway/routes/index', 'gateway:route:list', 'Connection', 1, 1, 1, NOW(), NOW(), 'system', 'system', 0),
(201, 2, '服务管理', 'menu.gateway.service', 'C', '/gateway/services', 'gateway/services/index', 'gateway:service:list', 'SetUp', 2, 1, 1, NOW(), NOW(), 'system', 'system', 0),
(202, 2, '过滤器管理', 'menu.gateway.filter', 'C', '/gateway/filters', 'gateway/filters/index', 'gateway:filter:list', 'Filter', 3, 1, 1, NOW(), NOW(), 'system', 'system', 0),
(203, 2, '限流配置', 'menu.gateway.limit', 'C', '/gateway/rate-limits', 'gateway/rate-limits/index', 'gateway:limit:list', 'Timer', 4, 1, 1, NOW(), NOW(), 'system', 'system', 0),

-- 监控管理子菜单
(300, 3, '系统监控', 'menu.monitor.system', 'C', '/monitor/system', 'monitor/system/index', 'monitor:system:view', 'Monitor', 1, 1, 1, NOW(), NOW(), 'system', 'system', 0),
(301, 3, '接口监控', 'menu.monitor.api', 'C', '/monitor/api', 'monitor/api/index', 'monitor:api:view', 'DataAnalysis', 2, 1, 1, NOW(), NOW(), 'system', 'system', 0),
(302, 3, '日志管理', 'menu.monitor.log', 'C', '/monitor/logs', 'monitor/logs/index', 'monitor:log:view', 'Document', 3, 1, 1, NOW(), NOW(), 'system', 'system', 0);

-- 三级菜单（按钮权限）
INSERT INTO sys_menu (id, parent_id, menu_name, i18n_code, menu_type, path, component, perms, icon, sort_order, visible, status, create_time, update_time, create_by, update_by, deleted) VALUES
-- 用户管理按钮
(1001, 100, '用户查看', '', 'F', '', '', 'system:user:view', '', 1, 1, 1, NOW(), NOW(), 'system', 'system', 0),
(1002, 100, '用户新增', '', 'F', '', '', 'system:user:create', '', 2, 1, 1, NOW(), NOW(), 'system', 'system', 0),
(1003, 100, '用户修改', '', 'F', '', '', 'system:user:update', '', 3, 1, 1, NOW(), NOW(), 'system', 'system', 0),
(1004, 100, '用户删除', '', 'F', '', '', 'system:user:delete', '', 4, 1, 1, NOW(), NOW(), 'system', 'system', 0),
(1005, 100, '用户导出', '', 'F', '', '', 'system:user:export', '', 5, 1, 1, NOW(), NOW(), 'system', 'system', 0),
(1006, 100, '用户导入', '', 'F', '', '', 'system:user:import', '', 6, 1, 1, NOW(), NOW(), 'system', 'system', 0),
(1007, 100, '重置密码', '', 'F', '', '', 'system:user:resetPwd', '', 7, 1, 1, NOW(), NOW(), 'system', 'system', 0),

-- 角色管理按钮
(1101, 101, '角色查看', '', 'F', '', '', 'system:role:view', '', 1, 1, 1, NOW(), NOW(), 'system', 'system', 0),
(1102, 101, '角色新增', '', 'F', '', '', 'system:role:create', '', 2, 1, 1, NOW(), NOW(), 'system', 'system', 0),
(1103, 101, '角色修改', '', 'F', '', '', 'system:role:update', '', 3, 1, 1, NOW(), NOW(), 'system', 'system', 0),
(1104, 101, '角色删除', '', 'F', '', '', 'system:role:delete', '', 4, 1, 1, NOW(), NOW(), 'system', 'system', 0),
(1105, 101, '分配权限', '', 'F', '', '', 'system:role:auth', '', 5, 1, 1, NOW(), NOW(), 'system', 'system', 0),

-- 权限管理按钮
(1201, 102, '权限查看', '', 'F', '', '', 'system:menu:view', '', 1, 1, 1, NOW(), NOW(), 'system', 'system', 0),
(1202, 102, '权限新增', '', 'F', '', '', 'system:menu:create', '', 2, 1, 1, NOW(), NOW(), 'system', 'system', 0),
(1203, 102, '权限修改', '', 'F', '', '', 'system:menu:update', '', 3, 1, 1, NOW(), NOW(), 'system', 'system', 0),
(1204, 102, '权限删除', '', 'F', '', '', 'system:menu:delete', '', 4, 1, 1, NOW(), NOW(), 'system', 'system', 0),

-- 部门管理按钮
(1301, 103, '部门查看', '', 'F', '', '', 'system:dept:view', '', 1, 1, 1, NOW(), NOW(), 'system', 'system', 0),
(1302, 103, '部门新增', '', 'F', '', '', 'system:dept:create', '', 2, 1, 1, NOW(), NOW(), 'system', 'system', 0),
(1303, 103, '部门修改', '', 'F', '', '', 'system:dept:update', '', 3, 1, 1, NOW(), NOW(), 'system', 'system', 0),
(1304, 103, '部门删除', '', 'F', '', '', 'system:dept:delete', '', 4, 1, 1, NOW(), NOW(), 'system', 'system', 0),

-- 路由管理按钮
(2001, 200, '路由查看', '', 'F', '', '', 'gateway:route:view', '', 1, 1, 1, NOW(), NOW(), 'system', 'system', 0),
(2002, 200, '路由新增', '', 'F', '', '', 'gateway:route:create', '', 2, 1, 1, NOW(), NOW(), 'system', 'system', 0),
(2003, 200, '路由修改', '', 'F', '', '', 'gateway:route:update', '', 3, 1, 1, NOW(), NOW(), 'system', 'system', 0),
(2004, 200, '路由删除', '', 'F', '', '', 'gateway:route:delete', '', 4, 1, 1, NOW(), NOW(), 'system', 'system', 0),

-- 服务管理按钮
(2101, 201, '服务查看', '', 'F', '', '', 'gateway:service:view', '', 1, 1, 1, NOW(), NOW(), 'system', 'system', 0),
(2102, 201, '服务新增', '', 'F', '', '', 'gateway:service:create', '', 2, 1, 1, NOW(), NOW(), 'system', 'system', 0),
(2103, 201, '服务修改', '', 'F', '', '', 'gateway:service:update', '', 3, 1, 1, NOW(), NOW(), 'system', 'system', 0),
(2104, 201, '服务删除', '', 'F', '', '', 'gateway:service:delete', '', 4, 1, 1, NOW(), NOW(), 'system', 'system', 0),

-- 过滤器管理按钮
(2201, 202, '过滤器查看', '', 'F', '', '', 'gateway:filter:view', '', 1, 1, 1, NOW(), NOW(), 'system', 'system', 0),
(2202, 202, '过滤器新增', '', 'F', '', '', 'gateway:filter:create', '', 2, 1, 1, NOW(), NOW(), 'system', 'system', 0),
(2203, 202, '过滤器修改', '', 'F', '', '', 'gateway:filter:update', '', 3, 1, 1, NOW(), NOW(), 'system', 'system', 0),
(2204, 202, '过滤器删除', '', 'F', '', '', 'gateway:filter:delete', '', 4, 1, 1, NOW(), NOW(), 'system', 'system', 0),

-- 限流配置按钮
(2301, 203, '限流查看', '', 'F', '', '', 'gateway:limit:view', '', 1, 1, 1, NOW(), NOW(), 'system', 'system', 0),
(2302, 203, '限流新增', '', 'F', '', '', 'gateway:limit:create', '', 2, 1, 1, NOW(), NOW(), 'system', 'system', 0),
(2303, 203, '限流修改', '', 'F', '', '', 'gateway:limit:update', '', 3, 1, 1, NOW(), NOW(), 'system', 'system', 0),
(2304, 203, '限流删除', '', 'F', '', '', 'gateway:limit:delete', '', 4, 1, 1, NOW(), NOW(), 'system', 'system', 0);

-- ========================================
-- 3. 角色权限分配
-- ========================================

-- 超级管理员拥有所有权限
INSERT INTO sys_role_menu (role_id, menu_id) 
SELECT 1, id FROM sys_menu WHERE deleted = 0;

-- 系统管理员（系统管理相关权限）
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
-- 系统管理目录及其所有子权限
(2, 1), -- 系统管理目录
(2, 100), (2, 1001), (2, 1002), (2, 1003), (2, 1004), (2, 1005), (2, 1006), (2, 1007), -- 用户管理
(2, 101), (2, 1101), (2, 1102), (2, 1103), (2, 1104), (2, 1105), -- 角色管理
(2, 102), (2, 1201), (2, 1202), (2, 1203), (2, 1204), -- 权限管理
(2, 103), (2, 1301), (2, 1302), (2, 1303), (2, 1304), -- 部门管理
-- 监控管理
(2, 3), (2, 300), (2, 301), (2, 302);

-- 网关管理员（网关管理相关权限）
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
-- 网关管理目录及其所有子权限
(3, 2), -- 网关管理目录
(3, 200), (3, 2001), (3, 2002), (3, 2003), (3, 2004), -- 路由管理
(3, 201), (3, 2101), (3, 2102), (3, 2103), (3, 2104), -- 服务管理
(3, 202), (3, 2201), (3, 2202), (3, 2203), (3, 2204), -- 过滤器管理
(3, 203), (3, 2301), (3, 2302), (3, 2303), (3, 2304), -- 限流配置
-- 监控管理（只读）
(3, 3), (3, 300), (3, 301), (3, 302);

-- 普通用户（基础查看权限）
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
-- 监控查看
(4, 3), (4, 300), (4, 301), (4, 302);

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
-- - 角色权限分配
-- ======================================== 