package com.muxin.gateway.admin.entity;

import com.mybatisflex.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 过滤器配置实体类.
 * <p>
 * 对应数据库表gw_filter，用于存储网关过滤器的配置信息。
 * 支持自定义过滤器的配置、启用/禁用、排序等功能。
 * </p>
 *
 * @author muxin
 * @version 1.0.0
 * @since 1.0.0
 */
@Data
@Table("gw_filter")
public class GwFilter {

    /**
     * 主键ID，自增。
     */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /**
     * 过滤器名称.
     * <p>
     * 用于标识过滤器的唯一名称。
     * </p>
     */
    private String filterName;

    /**
     * 过滤器类型.
     * <p>
     * 如：Pre、Post、Route、Error等Gateway过滤器类型。
     * </p>
     */
    private String filterType;

    /**
     * 过滤器描述.
     * <p>
     * 用于说明过滤器的功能和用途。
     * </p>
     */
    private String description;

    /**
     * 过滤器参数.
     * <p>
     * JSON格式的参数，使用Jackson类型处理器进行序列化和反序列化。
     * </p>
     */
    @Column(typeHandler = com.mybatisflex.core.handler.JacksonTypeHandler.class)
    private Map<String, Object> args;

    /**
     * 过滤器执行顺序.
     * <p>
     * 数值越小，优先级越高。
     * </p>
     */
    @Column("order")
    private Integer order;

    /**
     * 是否为系统内置过滤器.
     * <p>
     * 系统内置过滤器不允许删除。
     * </p>
     */
    private Boolean isSystem;

    /**
     * 是否启用.
     * <p>
     * true表示启用，false表示禁用。
     * </p>
     */
    private Boolean enabled;

    /**
     * 是否删除.
     * <p>
     * 逻辑删除标记，true表示已删除。
     * </p>
     */
    @Column(isLogicDelete = true)
    private Boolean deleted;

    /**
     * 创建时间.
     */
    private LocalDateTime createTime;

    /**
     * 更新时间.
     */
    private LocalDateTime updateTime;

    /**
     * 创建人.
     */
    private String createBy;

    /**
     * 更新人.
     */
    private String updateBy;
} 