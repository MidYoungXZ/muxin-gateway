-- 数据权限升级脚本
-- 为 sys_role 表添加 data_scope 字段
-- 创建 sys_role_dept 关联表

-- 1. 为 sys_role 表添加 data_scope 字段
ALTER TABLE sys_role ADD COLUMN data_scope INTEGER DEFAULT 1;

-- data_scope 数据范围说明：
-- 1 - 全部数据
-- 2 - 自定义数据
-- 3 - 本部门数据
-- 4 - 本部门及以下数据
-- 5 - 仅本人数据

-- 2. 创建角色-部门关联表（用于自定义数据权限）
CREATE TABLE IF NOT EXISTS sys_role_dept (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    role_id INTEGER NOT NULL,
    dept_id INTEGER NOT NULL,
    create_time TEXT NOT NULL DEFAULT (datetime('now', 'localtime')),
    UNIQUE(role_id, dept_id)
);

CREATE INDEX IF NOT EXISTS idx_rd_role ON sys_role_dept(role_id);
CREATE INDEX IF NOT EXISTS idx_rd_dept ON sys_role_dept(dept_id);

-- 3. 更新超级管理员角色为全部数据权限
UPDATE sys_role SET data_scope = 1 WHERE role_code = 'SUPER_ADMIN';

-- 4. 更新其他角色为默认权限（本部门及以下）
UPDATE sys_role SET data_scope = 4 WHERE data_scope IS NULL;