package com.muxin.gateway.core.plus.route.loadbalance;

import lombok.Builder;
import lombok.Data;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 负载均衡统计信息
 * 记录负载均衡策略的运行统计数据
 *
 * @author muxin
 */
@Data
@Builder
public class LoadBalanceStats {
    
    /**
     * 总选择次数
     */
    private final AtomicLong totalSelections;
    
    /**
     * 每个地址的选择次数
     */
    private final Map<String, AtomicLong> addressSelections;
    
    /**
     * 策略启动时间
     */
    private final long startTime;
    
    /**
     * 最后选择时间
     */
    private volatile long lastSelectionTime;
    
    /**
     * 平均选择时间（纳秒）
     */
    private final AtomicLong totalSelectionTime;
    
    /**
     * 策略名称
     */
    private final String strategyName;
    
    /**
     * 获取平均选择耗时（毫秒）
     */
    public double getAverageSelectionTimeMs() {
        long total = totalSelections.get();
        if (total == 0) {
            return 0.0;
        }
        return totalSelectionTime.get() / (double) total / 1_000_000.0;
    }
    
    /**
     * 记录一次选择
     */
    public void recordSelection(String address, long selectionTimeNanos) {
        totalSelections.incrementAndGet();
        totalSelectionTime.addAndGet(selectionTimeNanos);
        lastSelectionTime = System.currentTimeMillis();
        
        if (addressSelections != null && address != null) {
            addressSelections.computeIfAbsent(address, k -> new AtomicLong(0))
                    .incrementAndGet();
        }
    }
    
    /**
     * 获取运行时间（秒）
     */
    public long getUptimeSeconds() {
        return (System.currentTimeMillis() - startTime) / 1000;
    }
    
    /**
     * 获取每秒选择次数
     */
    public double getSelectionsPerSecond() {
        long uptimeSeconds = getUptimeSeconds();
        if (uptimeSeconds == 0) {
            return 0.0;
        }
        return totalSelections.get() / (double) uptimeSeconds;
    }
    
    /**
     * 重置统计信息
     */
    public void reset() {
        totalSelections.set(0);
        totalSelectionTime.set(0);
        lastSelectionTime = 0;
        
        if (addressSelections != null) {
            addressSelections.values().forEach(counter -> counter.set(0));
        }
    }
    
    @Override
    public String toString() {
        return String.format(
            "LoadBalanceStats{strategy='%s', selections=%d, avgTimeMs=%.2f, selectionsPerSec=%.2f, uptimeSeconds=%d}",
            strategyName,
            totalSelections.get(),
            getAverageSelectionTimeMs(),
            getSelectionsPerSecond(),
            getUptimeSeconds()
        );
    }
} 