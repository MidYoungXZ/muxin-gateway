package com.muxin.gateway.admin.mapper;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.row.Db;
import com.mybatisflex.core.row.Row;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.stream.Collectors;

import static com.muxin.gateway.admin.entity.table.Tables.*;

@Mapper
public interface PermissionMapper {

    default List<String> selectPermissionsByUserId(Long userId) {
        QueryWrapper wrapper = QueryWrapper.create()
                .select(SYS_MENU.PERMS)
                .from(SYS_USER)
                .innerJoin(SYS_USER_ROLE).on(SYS_USER_ROLE.USER_ID.eq(SYS_USER.ID))
                .innerJoin(SYS_ROLE).on(SYS_ROLE.ID.eq(SYS_USER_ROLE.ROLE_ID))
                .innerJoin(SYS_ROLE_MENU).on(SYS_ROLE_MENU.ROLE_ID.eq(SYS_ROLE.ID))
                .innerJoin(SYS_MENU).on(SYS_MENU.ID.eq(SYS_ROLE_MENU.MENU_ID))
                .where(SYS_USER.ID.eq(userId))
                .and(SYS_USER.DELETED.eq(0))
                .and(SYS_ROLE.DELETED.eq(0))
                .and(SYS_ROLE.STATUS.eq(1))
                .and(SYS_MENU.DELETED.eq(0))
                .and(SYS_MENU.STATUS.eq(1))
                .and(SYS_MENU.PERMS.isNotNull())
                .and(SYS_MENU.PERMS.ne(""));
        
        List<Row> rows = Db.selectListByQuery(wrapper);
        return rows.stream()
                .map(row -> row.getString("perms"))
                .distinct()
                .collect(Collectors.toList());
    }

    default List<String> selectRolesByUserId(Long userId) {
        QueryWrapper wrapper = QueryWrapper.create()
                .select(SYS_ROLE.ROLE_CODE)
                .from(SYS_USER)
                .innerJoin(SYS_USER_ROLE).on(SYS_USER_ROLE.USER_ID.eq(SYS_USER.ID))
                .innerJoin(SYS_ROLE).on(SYS_ROLE.ID.eq(SYS_USER_ROLE.ROLE_ID))
                .where(SYS_USER.ID.eq(userId))
                .and(SYS_USER.DELETED.eq(0))
                .and(SYS_ROLE.DELETED.eq(0))
                .and(SYS_ROLE.STATUS.eq(1));
        
        List<Row> rows = Db.selectListByQuery(wrapper);
        return rows.stream()
                .map(row -> row.getString("role_code"))
                .distinct()
                .collect(Collectors.toList());
    }
}