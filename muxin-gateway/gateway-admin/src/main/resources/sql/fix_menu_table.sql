-- ========================================
-- 修复 sys_menu 表结构
-- 添加缺失的 ancestors 字段
-- ========================================

-- 检查并添加 ancestors 字段
ALTER TABLE sys_menu 
ADD COLUMN IF NOT EXISTS ancestors VARCHAR(500) COMMENT '祖级列表' AFTER parent_id;

-- 更新现有数据的 ancestors 字段
-- 对于根菜单（parent_id = 0）
UPDATE sys_menu SET ancestors = '0' WHERE parent_id = 0 AND ancestors IS NULL;

-- 对于一级菜单
UPDATE sys_menu SET ancestors = CONCAT('0,', parent_id) 
WHERE parent_id > 0 AND parent_id IN (SELECT id FROM (SELECT id FROM sys_menu WHERE parent_id = 0) t) 
AND ancestors IS NULL;

-- 对于二级菜单
UPDATE sys_menu m1 
JOIN sys_menu m2 ON m1.parent_id = m2.id 
SET m1.ancestors = CONCAT(m2.ancestors, ',', m1.parent_id)
WHERE m1.ancestors IS NULL AND m2.ancestors IS NOT NULL;

-- 对于三级菜单（如果有的话）
UPDATE sys_menu m1 
JOIN sys_menu m2 ON m1.parent_id = m2.id 
SET m1.ancestors = CONCAT(m2.ancestors, ',', m1.parent_id)
WHERE m1.ancestors IS NULL AND m2.ancestors IS NOT NULL;

-- 验证修复结果
SELECT 
    id, 
    parent_id, 
    ancestors, 
    menu_name, 
    menu_type 
FROM sys_menu 
ORDER BY id;

-- ========================================
-- 修复完成
-- ======================================== 