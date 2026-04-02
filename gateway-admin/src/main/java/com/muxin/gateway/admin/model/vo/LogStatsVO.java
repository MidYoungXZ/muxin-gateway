package com.muxin.gateway.admin.model.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LogStatsVO {
    
    private Long totalCount;
    
    private Long todayCount;
    
    private Double successRate;
    
    private Long failureCount;
}