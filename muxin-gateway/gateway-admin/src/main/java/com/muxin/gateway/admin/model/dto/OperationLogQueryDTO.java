package com.muxin.gateway.admin.model.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 操作日志查询DTO
 *
 * @author muxin
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class OperationLogQueryDTO extends PageQueryDTO {
    
    /**
     * 模块名称
     */
    private String module;
    
    /**
     * 操作类型
     */
    private String operation;
    
    /**
     * 操作人员
     */
    private String operator;
    
    /**
     * 操作状态：0-失败，1-成功
     */
    private Integer status;
    
    /**
     * 开始时间
     */
    private String startTime;
    
    /**
     * 结束时间
     */
    private String endTime;
    
    /**
     * 请求方法
     */
    private String method;
    
    /**
     * 关键字搜索（操作人员、模块、操作类型）
     */
    private String keyword;
} 