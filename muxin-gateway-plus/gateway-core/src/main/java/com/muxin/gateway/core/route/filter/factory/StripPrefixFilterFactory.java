package com.muxin.gateway.core.route.filter.factory;

import com.muxin.gateway.core.route.filter.FilterTypeEnum;
import com.muxin.gateway.core.route.filter.PartFilter;
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
    public PartFilter create(Map<String, String> args) {
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
    private record StripPrefixFilter(int parts) implements PartFilter {

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
                if (!segment.isEmpty() || !newPath.isEmpty()) {
                    newPath.append("/").append(segment);
                }
            }

            // 如果新路径为空，设置为根路径
            if (newPath.isEmpty()) {
                newPath.append("/");
            }

            // 添加回查询参数
            String finalPath = newPath.toString() + queryString;

            // 将修改后的路径存储在Exchange属性中，供后续URL重构使用
            exchange.getAttributes().put(GATEWAY_STRIPPED_PATH_ATTR, finalPath);

            log.debug("Stripped {} parts from path: {} -> {}", parts, exchange.getRequest().uri(), finalPath);
        }

        @Override
        public FilterTypeEnum filterType() {
            return FilterTypeEnum.PART;
        }

        @Override
        public int getOrder() {
            return 10000;
        }
    }
} 