package com.muxin.gateway.admin.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface PermissionMapper {

    @Select("SELECT DISTINCT m.perms " +
            "FROM sys_user u " +
            "INNER JOIN sys_user_role ur ON u.id = ur.user_id " +
            "INNER JOIN sys_role r ON ur.role_id = r.id " +
            "INNER JOIN sys_role_menu rm ON r.id = rm.role_id " +
            "INNER JOIN sys_menu m ON rm.menu_id = m.id " +
            "WHERE u.id = #{userId} " +
            "AND u.deleted = 0 " +
            "AND r.deleted = 0 " +
            "AND r.status = 1 " +
            "AND m.deleted = 0 " +
            "AND m.status = 1 " +
            "AND m.perms IS NOT NULL " +
            "AND m.perms != ''")
    List<String> selectPermissionsByUserId(@Param("userId") Long userId);

    @Select("SELECT DISTINCT r.role_code " +
            "FROM sys_user u " +
            "INNER JOIN sys_user_role ur ON u.id = ur.user_id " +
            "INNER JOIN sys_role r ON ur.role_id = r.id " +
            "WHERE u.id = #{userId} " +
            "AND u.deleted = 0 " +
            "AND r.deleted = 0 " +
            "AND r.status = 1")
    List<String> selectRolesByUserId(@Param("userId") Long userId);
}