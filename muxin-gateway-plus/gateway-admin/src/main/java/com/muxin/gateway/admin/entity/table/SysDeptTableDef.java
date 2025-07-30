package com.muxin.gateway.admin.entity.table;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.table.TableDef;

/**
 * sys_dept 表定义
 *
 * @author mybatis-flex-processor
 * @since 2024-01-01
 */
public class SysDeptTableDef extends TableDef {

    /**
     * 部门表
     */
    public static final SysDeptTableDef SYS_DEPT = new SysDeptTableDef();

    /**
     * ID
     */
    public final QueryColumn ID = new QueryColumn(this, "id");

    /**
     * 父部门ID
     */
    public final QueryColumn PARENT_ID = new QueryColumn(this, "parent_id");

    /**
     * 部门名称
     */
    public final QueryColumn DEPT_NAME = new QueryColumn(this, "dept_name");

    /**
     * 部门编码
     */
    public final QueryColumn DEPT_CODE = new QueryColumn(this, "dept_code");

    /**
     * 负责人
     */
    public final QueryColumn LEADER = new QueryColumn(this, "leader");

    /**
     * 联系电话
     */
    public final QueryColumn PHONE = new QueryColumn(this, "phone");

    /**
     * 邮箱
     */
    public final QueryColumn EMAIL = new QueryColumn(this, "email");

    /**
     * 排序
     */
    public final QueryColumn ORDER_NUM = new QueryColumn(this, "order_num");

    /**
     * 状态
     */
    public final QueryColumn STATUS = new QueryColumn(this, "status");

    /**
     * 创建时间
     */
    public final QueryColumn CREATE_TIME = new QueryColumn(this, "create_time");

    /**
     * 更新时间
     */
    public final QueryColumn UPDATE_TIME = new QueryColumn(this, "update_time");

    /**
     * 创建人
     */
    public final QueryColumn CREATE_BY = new QueryColumn(this, "create_by");

    /**
     * 更新人
     */
    public final QueryColumn UPDATE_BY = new QueryColumn(this, "update_by");

    /**
     * 是否删除
     */
    public final QueryColumn DELETED = new QueryColumn(this, "deleted");

    /**
     * 所有字段
     */
    public final QueryColumn ALL_COLUMNS = new QueryColumn(this, "*");

    /**
     * 默认字段，排除逻辑删除字段
     */
    public final QueryColumn[] DEFAULT_COLUMNS = new QueryColumn[]{
            ID, PARENT_ID, DEPT_NAME, DEPT_CODE, LEADER, PHONE, EMAIL,
            ORDER_NUM, STATUS, CREATE_TIME, UPDATE_TIME, CREATE_BY, UPDATE_BY
    };

    public SysDeptTableDef() {
        super("", "sys_dept");
    }

    private SysDeptTableDef(String schema, String name, String alisa) {
        super(schema, name, alisa);
    }

    public SysDeptTableDef as(String alias) {
        String key = getNameWithSchema() + "." + alias;
        return getCache(key, k -> new SysDeptTableDef("", "sys_dept", alias));
    }
} 