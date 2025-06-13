package com.muxin.gateway.core.factory.impl;

import com.muxin.gateway.core.factory.FilterFactory;
import com.muxin.gateway.core.filter.FilterTypeEnum;
import com.muxin.gateway.core.filter.RouteRuleFilter;
import com.muxin.gateway.core.http.ServerWebExchange;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

import static com.muxin.gateway.core.common.GatewayConstants.*;

/**
 * 路径前缀剥离过滤器工厂
 */
@Slf4j
public class StripPrefixFilterFactory implements FilterFactory {

    @Override
    public RouteRuleFilter create(Map<String, String> args) {
        String partsStr = args.get(PARTS_ARG);
        if (partsStr == null) {
            partsStr = args.get(GENKEY_PREFIX + "0"); // 兼容简化配置
        }
        
        int parts = partsStr != null ? Integer.parseInt(partsStr) : 1;
        
        return new StripPrefixFilter(parts);
    }

    /**
     * 路径前缀剥离过滤器
     */
    @Slf4j
    private static class StripPrefixFilter implements RouteRuleFilter {
        
        private final int parts;
        
        public StripPrefixFilter(int parts) {
            this.parts = parts;
        }

        @Override
        public void filter(ServerWebExchange exchange) {
            String originalPath = exchange.getRequest().uri();
            
            // 去掉查询参数
            String queryString = "";
            int queryIndex = originalPath.indexOf('?');
            if (queryIndex > 0) {
                queryString = originalPath.substring(queryIndex);
                originalPath = originalPath.substring(0, queryIndex);
            }
            
            String[] segments = originalPath.split("/");
            StringBuilder newPath = new StringBuilder();
            
            // 跳过指定数量的路径段
            int skipped = 0;
            for (String segment : segments) {
                if (!segment.isEmpty() && skipped < parts) {
                    skipped++;
                    continue;
                }
                if (!segment.isEmpty() || newPath.length() > 0) {
                    newPath.append("/").append(segment);
                }
            }
            
            // 如果新路径为空，设置为根路径
            if (newPath.length() == 0) {
                newPath.append("/");
            }
            
            // 添加回查询参数
            String finalPath = newPath.toString() + queryString;
            
            // 将修改后的路径存储在Exchange属性中
            exchange.getAttributes().put(GATEWAY_REQUEST_URL_ATTR, finalPath);
            
            log.debug("Stripped {} parts from path: {} -> {}", parts, exchange.getRequest().uri(), finalPath);
        }

        @Override
        public FilterTypeEnum filterType() {
            return FilterTypeEnum.REQUEST;
        }

        @Override
        public int getOrder() {
            return 10000;
        }
    }
} 