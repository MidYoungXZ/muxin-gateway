package com.muxin.gateway.core.utils;

import com.muxin.gateway.registry.api.ServiceInstance;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * 请求交换工具类
 */
@Slf4j
public class ExchangeUtil {

    /**
     * URL重建 - 将原始请求URL重建为指向具体服务实例的URL
     * 
     * @param serviceInstance 服务实例
     * @param original 原始URI
     * @return 重建后的URI
     */
    public static URI doReconstructURI(ServiceInstance serviceInstance, URI original) {
        if (serviceInstance == null || original == null) {
            throw new IllegalArgumentException("ServiceInstance and original URI cannot be null");
        }

        try {
            String scheme = serviceInstance.isSecure() ? "https" : "http";
            String host = serviceInstance.getHost();
            int port = serviceInstance.getPort();
            String path = original.getPath();
            String query = original.getQuery();
            String fragment = original.getFragment();

            // 构建新的URI
            StringBuilder uriBuilder = new StringBuilder();
            uriBuilder.append(scheme).append("://").append(host);
            
            // 只有当端口不是默认端口时才添加端口
            if ((scheme.equals("http") && port != 80) || (scheme.equals("https") && port != 443)) {
                uriBuilder.append(":").append(port);
            }
            
            if (path != null) {
                uriBuilder.append(path);
            }
            
            if (query != null && !query.isEmpty()) {
                uriBuilder.append("?").append(query);
            }
            
            if (fragment != null && !fragment.isEmpty()) {
                uriBuilder.append("#").append(fragment);
            }

            URI reconstructedURI = new URI(uriBuilder.toString());
            log.debug("Reconstructed URI: {} -> {}", original, reconstructedURI);
            return reconstructedURI;
            
        } catch (URISyntaxException e) {
            log.error("Failed to reconstruct URI for service instance: {}, original: {}", 
                serviceInstance, original, e);
            throw new RuntimeException("URL reconstruction failed", e);
        }
    }

    /**
     * 提取服务ID从lb://协议的URI中
     * 
     * @param uri lb://service-name格式的URI
     * @return 服务名称
     */
    public static String extractServiceId(URI uri) {
        if (uri == null) {
            return null;
        }
        
        String scheme = uri.getScheme();
        if (!"lb".equalsIgnoreCase(scheme)) {
            return null;
        }
        
        return uri.getHost();
    }

    /**
     * 判断是否为负载均衡URI
     * 
     * @param uri URI
     * @return 是否为lb://格式
     */
    public static boolean isLoadBalanceUri(URI uri) {
        return uri != null && "lb".equalsIgnoreCase(uri.getScheme());
    }
}
