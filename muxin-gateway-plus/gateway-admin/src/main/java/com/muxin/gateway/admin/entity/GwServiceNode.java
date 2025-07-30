package com.muxin.gateway.admin.entity;

import com.mybatisflex.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.List;

/**
 * 服务节点实体
 *
 * @author muxin
 */
@Data
@Table("gw_service_node")
public class GwServiceNode {
    
    @Id(keyType = KeyType.Auto)
    private Long id;
    
    /**
     * 节点ID（唯一标识）
     */
    private String nodeId;
    
    /**
     * 服务名称
     */
    private String serviceName;
    
    /**
     * 节点名称
     */
    private String nodeName;
    
    /**
     * 节点地址
     */
    private String address;
    
    /**
     * 端口号
     */
    private Integer port;
    
    /**
     * 权重（1-100）
     */
    private Integer weight;
    
    /**
     * 最大失败次数
     */
    private Integer maxFails;
    
    /**
     * 失败超时时间（秒）
     */
    private Integer failTimeout;
    
    /**
     * 是否为备份节点：0-否，1-是
     */
    private Boolean backup;
    
    /**
     * 是否启用健康检查：0-禁用，1-启用
     */
    private Boolean healthCheckEnabled;
    
    /**
     * 健康检查间隔（秒）
     */
    private Integer healthCheckInterval;
    
    /**
     * 健康检查超时时间（秒）
     */
    private Integer healthCheckTimeout;
    
    /**
     * 健康检查路径
     */
    private String healthCheckPath;
    
    /**
     * 健康检查期望状态码（JSON数组）
     */
    @Column(typeHandler = com.mybatisflex.core.handler.JacksonTypeHandler.class)
    private List<Integer> healthCheckExpectedStatus;
    
    /**
     * 节点状态：0-禁用，1-启用，2-维护中
     */
    private Integer status;
    
    /**
     * 最后检查时间
     */
    private LocalDateTime lastCheckTime;
    
    /**
     * 最后检查结果：0-失败，1-成功
     */
    private Integer lastCheckResult;
    
    /**
     * 节点元数据（JSON格式）
     */
    @Column(typeHandler = com.mybatisflex.core.handler.JacksonTypeHandler.class)
    private Map<String, Object> metadata;
    
    /**
     * 是否删除：0-否，1-是
     */
    @Column(isLogicDelete = true)
    private Boolean deleted;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    
    /**
     * 创建人
     */
    private String createBy;
    
    /**
     * 更新人
     */
    private String updateBy;
} 