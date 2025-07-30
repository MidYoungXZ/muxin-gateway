-- 角色管理初始化SQL脚本（简化版）
-- 只包含基础角色数据，避免字段不匹配问题

-- 插入基础角色数据
INSERT IGNORE INTO sys_role (role_code, role_name, description, status, create_time, update_time, create_by, update_by, deleted) VALUES
('SUPER_ADMIN', '超级管理员', '系统超级管理员，拥有所有权限，包括系统配置和用户管理', 1, NOW(), NOW(), 'system', 'system', 0),
('ADMIN', '系统管理员', '系统管理员，拥有大部分管理权限，可以管理用户、角色和基础配置', 1, NOW(), NOW(), 'system', 'system', 0),
('GATEWAY_ADMIN', '网关管理员', '网关管理员，专门负责API网关的路由、过滤器、负载均衡等配置管理', 1, NOW(), NOW(), 'system', 'system', 0),
('USER', '普通用户', '普通用户，可以查看基础信息和个人资料，无管理权限', 1, NOW(), NOW(), 'system', 'system', 0),
('OPERATOR', '运维人员', '运维人员，负责系统监控、日志查看、健康检查等运维相关功能', 1, NOW(), NOW(), 'system', 'system', 0),
('AUDITOR', '审计员', '审计员，拥有查看权限，可以查看各种日志、报表和审计信息', 1, NOW(), NOW(), 'system', 'system', 0),
('GUEST', '访客', '访客用户，只能查看公开信息，权限最低', 1, NOW(), NOW(), 'system', 'system', 0),
('DEVELOPER', '开发人员', '开发人员，可以进行API测试、查看文档、调试路由等开发相关功能', 1, NOW(), NOW(), 'system', 'system', 0);

-- 为默认用户分配角色（确保用户存在后再执行）
INSERT IGNORE INTO sys_user_role (user_id, role_id, create_time) 
SELECT u.id, r.id, NOW()
FROM sys_user u, sys_role r 
WHERE u.username = 'admin' AND r.role_code = 'SUPER_ADMIN'
  AND u.deleted = 0 AND r.deleted = 0;

-- 查询结果，确认插入成功
SELECT 
    '角色初始化完成' as message,
    COUNT(*) as total_roles
FROM sys_role 
WHERE deleted = 0;

SELECT 
    r.role_code,
    r.role_name,
    r.description,
    r.status,
    r.create_time
FROM sys_role r
WHERE r.deleted = 0
ORDER BY r.id;

-- 查询用户角色分配情况
SELECT 
    u.username,
    u.nickname,
    r.role_code,
    r.role_name,
    ur.create_time as assigned_time
FROM sys_user u
JOIN sys_user_role ur ON u.id = ur.user_id
JOIN sys_role r ON ur.role_id = r.id
WHERE u.deleted = 0 AND r.deleted = 0
ORDER BY u.username, r.role_code; 