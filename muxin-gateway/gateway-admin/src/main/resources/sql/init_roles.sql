-- 角色管理初始化SQL脚本
-- 删除已存在的测试数据（仅在开发环境使用）
-- DELETE FROM sys_role_menu WHERE role_id IN (1, 2, 3, 4);
-- DELETE FROM sys_role WHERE id IN (1, 2, 3, 4);

-- 插入基础角色数据
INSERT IGNORE INTO sys_role (id, role_code, role_name, description, status, create_time, update_time, create_by, update_by, deleted) VALUES
(1, 'SUPER_ADMIN', '超级管理员', '系统超级管理员，拥有所有权限', 1, NOW(), NOW(), 'system', 'system', 0),
(2, 'ADMIN', '系统管理员', '系统管理员，拥有大部分管理权限', 1, NOW(), NOW(), 'system', 'system', 0),
(3, 'GATEWAY_ADMIN', '网关管理员', '网关管理员，负责路由、过滤器等网关配置管理', 1, NOW(), NOW(), 'system', 'system', 0),
(4, 'USER', '普通用户', '普通用户，只能查看基础信息', 1, NOW(), NOW(), 'system', 'system', 0),
(5, 'OPERATOR', '运维人员', '运维人员，负责监控、日志等运维相关功能', 1, NOW(), NOW(), 'system', 'system', 0),
(6, 'AUDITOR', '审计员', '审计员，负责查看各种日志和报表', 1, NOW(), NOW(), 'system', 'system', 0),
(7, 'GUEST', '访客', '访客用户，只能查看公开信息', 1, NOW(), NOW(), 'system', 'system', 0),
(8, 'DEVELOPER', '开发人员', '开发人员，可以进行API测试和调试', 1, NOW(), NOW(), 'system', 'system', 0);

-- 为默认用户分配角色（假设用户ID为1的是超级管理员）
INSERT IGNORE INTO sys_user_role (user_id, role_id, create_time) VALUES
(1, 1, NOW()),  -- admin用户分配超级管理员角色
(2, 4, NOW()),  -- 如果有其他用户，分配普通用户角色
(3, 3, NOW());  -- 网关管理员

-- 为角色分配菜单权限（需要根据实际的菜单数据来配置）
-- 注意：以下权限分配仅在菜单数据存在时生效

-- 超级管理员拥有所有权限（如果sys_menu表存在且有数据）
INSERT IGNORE INTO sys_role_menu (role_id, menu_id, create_time)
SELECT 1 as role_id, id as menu_id, NOW() as create_time
FROM sys_menu 
WHERE deleted = 0
  AND id IS NOT NULL;

-- 如果没有菜单数据，可以手动为超级管理员分配一些基础权限
-- 这里先跳过具体的权限分配，由管理员在界面中手动配置

-- 更新角色状态描述信息
UPDATE sys_role SET 
  description = CASE 
    WHEN role_code = 'SUPER_ADMIN' THEN '系统超级管理员，拥有所有权限，包括系统配置和用户管理'
    WHEN role_code = 'ADMIN' THEN '系统管理员，拥有大部分管理权限，可以管理用户、角色和基础配置'
    WHEN role_code = 'GATEWAY_ADMIN' THEN '网关管理员，专门负责API网关的路由、过滤器、负载均衡等配置管理'
    WHEN role_code = 'USER' THEN '普通用户，可以查看基础信息和个人资料，无管理权限'
    WHEN role_code = 'OPERATOR' THEN '运维人员，负责系统监控、日志查看、健康检查等运维相关功能'
    WHEN role_code = 'AUDITOR' THEN '审计员，拥有查看权限，可以查看各种日志、报表和审计信息'
    WHEN role_code = 'GUEST' THEN '访客用户，只能查看公开信息，权限最低'
    WHEN role_code = 'DEVELOPER' THEN '开发人员，可以进行API测试、查看文档、调试路由等开发相关功能'
    ELSE description
  END
WHERE role_code IN ('SUPER_ADMIN', 'ADMIN', 'GATEWAY_ADMIN', 'USER', 'OPERATOR', 'AUDITOR', 'GUEST', 'DEVELOPER');

-- 创建角色权限视图（可选）
CREATE OR REPLACE VIEW v_role_permissions AS
SELECT 
    r.id as role_id,
    r.role_code,
    r.role_name,
    r.description as role_description,
    m.id as menu_id,
    m.menu_name,
    m.menu_type,
    m.perms as permission,
    rm.create_time as assigned_time
FROM sys_role r
LEFT JOIN sys_role_menu rm ON r.id = rm.role_id
LEFT JOIN sys_menu m ON rm.menu_id = m.id
WHERE r.deleted = 0 
  AND (m.deleted = 0 OR m.deleted IS NULL)
ORDER BY r.id, m.sort_order;

-- 创建用户角色权限视图（可选）
CREATE OR REPLACE VIEW v_user_role_permissions AS
SELECT 
    u.id as user_id,
    u.username,
    u.nickname,
    r.id as role_id,
    r.role_code,
    r.role_name,
    m.id as menu_id,
    m.menu_name,
    m.perms as permission,
    ur.create_time as role_assigned_time
FROM sys_user u
LEFT JOIN sys_user_role ur ON u.id = ur.user_id
LEFT JOIN sys_role r ON ur.role_id = r.id
LEFT JOIN sys_role_menu rm ON r.id = rm.role_id
LEFT JOIN sys_menu m ON rm.menu_id = m.id
WHERE u.deleted = 0 
  AND (r.deleted = 0 OR r.deleted IS NULL)
  AND (m.deleted = 0 OR m.deleted IS NULL)
  AND u.status = 1
  AND (r.status = 1 OR r.status IS NULL)
ORDER BY u.id, r.id, m.sort_order;

-- 插入完成提示
SELECT 
    '角色初始化完成' as message,
    COUNT(*) as total_roles
FROM sys_role 
WHERE deleted = 0;

SELECT 
    '角色权限分配完成' as message,
    COUNT(*) as total_permissions
FROM sys_role_menu; 