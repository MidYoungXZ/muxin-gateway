package com.muxin.gateway.admin.mapper;

import com.mybatisflex.core.BaseMapper;
import com.muxin.gateway.admin.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户Mapper
 *
 * @author muxin
 */
@Mapper
public interface UserMapper extends BaseMapper<SysUser> {
} 