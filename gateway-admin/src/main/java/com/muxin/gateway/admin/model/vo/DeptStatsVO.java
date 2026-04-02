package com.muxin.gateway.admin.model.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DeptStatsVO {
    
    private Long totalCount;
    
    private Long enabledCount;
    
    private Long disabledCount;
}