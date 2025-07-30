package com.muxin.gateway.admin.entity;

import com.mybatisflex.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 操作日志实体
 *
 * @author muxin
 */
@Data
@Table("sys_operation_log")
public class SysOperationLog {
    
    @Id(keyType = KeyType.Auto)
    private Long id;
    
    /**
     * 模块名称
     */
    private String module;
    
    /**
     * 操作类型
     */
    private String operation;
    
    /**
     * 请求方法
     */
    private String method;
    
    /**
     * 请求URL
     */
    private String requestUrl;
    
    /**
     * 请求参数
     */
    private String params;
    
    /**
     * 返回结果
     */
    private String result;
    
    /**
     * 异常信息
     */
    private String error;
    
    /**
     * 执行时长（毫秒）
     */
    private Long duration;
    
    /**
     * 操作人员
     */
    private String operator;
    
    /**
     * 操作人员ID
     */
    private Long operatorId;
    
    /**
     * 操作IP地址
     */
    private String operatorIp;
    
    /**
     * 操作地点
     */
    private String operatorLocation;
    
    /**
     * 浏览器信息
     */
    private String browser;
    
    /**
     * 操作系统
     */
    private String os;
    
    /**
     * 操作状态：0-失败，1-成功
     */
    private Integer status;
    
    /**
     * 操作时间
     */
    private LocalDateTime operateTime;
    
    /**
     * 是否删除：0-否，1-是
     */
    @Column(isLogicDelete = true)
    private Integer deleted;
} 