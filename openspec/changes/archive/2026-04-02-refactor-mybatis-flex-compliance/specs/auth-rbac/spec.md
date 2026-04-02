## ADDED Requirements

### Requirement: Auth & RBAC 查询必须符合 MyBatis-Flex 规范
所有认证授权相关服务（UserServiceImpl、RoleServiceImpl、MenuServiceImpl、DeptServiceImpl）中的数据库查询操作必须遵循 `mybatis-flex-query-standard` 规格定义的标准实现方式。

#### Scenario: 用户查询使用动态条件
- **WHEN** 分页查询用户列表
- **THEN** 必须使用 `.and(SYS_USER.USERNAME.like(dto.getUsername(), dto.getUsername() != null))` 而非 if 语句手动添加条件

#### Scenario: 用户查询过滤逻辑删除
- **WHEN** 查询任何用户数据
- **THEN** 必须添加条件 `.where(SYS_USER.DELETED.eq(0))` 过滤已删除记录

#### Scenario: 权限查询使用多表关联 QueryWrapper
- **WHEN** 查询用户权限列表
- **THEN** 必须使用 `.innerJoin(SYS_ROLE_MENU).on(SYS_MENU.ID.eq(SYS_ROLE_MENU.MENU_ID))` 而非 XML SQL 或字符串拼接

#### Scenario: 数据权限过滤使用动态条件
- **WHEN** 应用数据权限过滤
- **THEN** 必须使用 `.and(SYS_USER.DEPT_ID.in(deptIds, !deptIds.isEmpty()))` 而非 if 语句手动添加条件

### Requirement: Auth & RBAC 时间字段手动设置
所有认证授权相关实体（SysUser、SysRole、SysMenu、SysDept、SysUserRole、SysRoleMenu）的创建和更新操作必须手动设置 `create_time` 和 `update_time` 字段。

#### Scenario: 创建用户时设置时间
- **WHEN** 创建新用户记录
- **THEN** 必须手动设置 `user.setCreateTime(LocalDateTime.now())` 和 `user.setUpdateTime(LocalDateTime.now())`

#### Scenario: 创建角色时设置时间
- **WHEN** 创建新角色记录
- **THEN** 必须手动设置 `role.setCreateTime(LocalDateTime.now())` 和 `role.setUpdateTime(LocalDateTime.now())`

#### Scenario: 创建菜单时设置时间
- **WHEN** 创建新菜单记录
- **THEN** 必须手动设置 `menu.setCreateTime(LocalDateTime.now())` 和 `menu.setUpdateTime(LocalDateTime.now())`

### Requirement: 多表关联查询优先使用 QueryWrapper
涉及用户-角色-菜单等多表关联查询时，应优先使用 QueryWrapper 的 join 语法而非 XML SQL 文件。

#### Scenario: 用户角色关联查询
- **WHEN** 查询用户的角色列表
- **THEN** 应使用 `.innerJoin(SYS_USER_ROLE).on(SYS_USER.ID.eq(SYS_USER_ROLE.USER_ID))` 构建 QueryWrapper

#### Scenario: 用户菜单树查询
- **WHEN** 查询用户的菜单树
- **THEN** 应使用多表 join QueryWrapper：`.from(SYS_MENU).innerJoin(SYS_ROLE_MENU).on(SYS_MENU.ID.eq(SYS_ROLE_MENU.MENU_ID)).innerJoin(SYS_USER_ROLE).on(SYS_ROLE_MENU.ROLE_ID.eq(SYS_USER_ROLE.ROLE_ID))`