package com.muxin.gateway.admin.mapper;

import com.mybatisflex.core.BaseMapper;
import com.muxin.gateway.admin.entity.SysRoleDept;
import org.apache.ibatis.annotations.Mapper;

/**
 * 角色部门关联Mapper
 *
 * @author muxin
 * @version 1.0.0
 * @since 1.0.0
 */
@Mapper
public interface RoleDeptMapper extends BaseMapper<SysRoleDept> {
}