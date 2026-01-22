package com.muxin.gateway.admin.mapper;

import com.mybatisflex.core.BaseMapper;
import com.muxin.gateway.admin.entity.SysUserRole;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户角色关联Mapper
 *
 * @author muxin
 * @version 1.0.0
 * @since 1.0.0
 */
@Mapper
public interface UserRoleMapper extends BaseMapper<SysUserRole> {
} 