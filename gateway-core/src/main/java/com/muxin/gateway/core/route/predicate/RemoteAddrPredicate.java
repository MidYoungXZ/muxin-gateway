package com.muxin.gateway.core.route.predicate;

import com.muxin.gateway.core.route.exchange.HttpServerExchange;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.util.*;

@Slf4j
public class RemoteAddrPredicate implements Predicate {

    public static final String TYPE = "RemoteAddr";

    private final List<CidrBlock> cidrBlocks;
    private final Map<String, Object> config;

    public RemoteAddrPredicate(String... sources) {
        this(Arrays.asList(sources));
    }

    public RemoteAddrPredicate(List<String> sources) {
        if (sources == null || sources.isEmpty()) {
            throw new IllegalArgumentException("IP地址段不能为空");
        }
        this.cidrBlocks = new ArrayList<>();
        this.config = new HashMap<>();
        this.config.put("sources", sources);
        
        for (String source : sources) {
            cidrBlocks.add(new CidrBlock(source));
        }
    }

    public RemoteAddrPredicate(PredicateDefinition definition) {
        if (definition == null) {
            throw new IllegalArgumentException("PredicateDefinition不能为空");
        }
        this.cidrBlocks = new ArrayList<>();
        this.config = definition.getConfig() != null ? definition.getConfig() : new HashMap<>();
        
        List<String> sources = parseSources(definition);
        for (String source : sources) {
            cidrBlocks.add(new CidrBlock(source));
        }
    }

    @SuppressWarnings("unchecked")
    private List<String> parseSources(PredicateDefinition definition) {
        Object sourcesObj = definition.getConfigValue("sources");
        if (sourcesObj == null) {
            throw new IllegalArgumentException("RemoteAddrPredicate必须配置sources参数");
        }

        List<String> result = new ArrayList<>();
        if (sourcesObj instanceof String) {
            String str = ((String) sourcesObj).trim();
            if (str.contains(",")) {
                for (String s : str.split(",")) {
                    String trimmed = s.trim();
                    if (!trimmed.isEmpty()) {
                        result.add(trimmed);
                    }
                }
            } else {
                result.add(str);
            }
        } else if (sourcesObj instanceof List) {
            for (Object item : (List<?>) sourcesObj) {
                if (item != null) {
                    result.add(item.toString().trim());
                }
            }
        }

        if (result.isEmpty()) {
            throw new IllegalArgumentException("sources参数解析后为空");
        }

        return result;
    }

    @Override
    public boolean test(HttpServerExchange exchange) {
        if (exchange == null) {
            log.warn("[RemoteAddrPredicate] exchange为空");
            return false;
        }

        String clientIp = getClientIp(exchange);
        if (clientIp == null || clientIp.isEmpty()) {
            log.debug("[RemoteAddrPredicate] 无法获取客户端IP");
            return false;
        }

        try {
            InetAddress address = InetAddress.getByName(clientIp);
            byte[] bytes = address.getAddress();

            for (CidrBlock block : cidrBlocks) {
                if (block.contains(bytes)) {
                    log.debug("[RemoteAddrPredicate] 客户端IP {} 匹配 CIDR {}", clientIp, block.original);
                    return true;
                }
            }

            log.debug("[RemoteAddrPredicate] 客户端IP {} 不匹配任何地址段", clientIp);
            return false;
        } catch (Exception e) {
            log.warn("[RemoteAddrPredicate] 解析IP地址失败: {}", e.getMessage());
            return false;
        }
    }

    private String getClientIp(HttpServerExchange exchange) {
        String xForwardedFor = exchange.header("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = exchange.header("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return null;
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public String getName() {
        return "RemoteAddrPredicate";
    }

    @Override
    public Map<String, Object> getConfig() {
        return config;
    }

    private static class CidrBlock {
        private final String original;
        private final byte[] network;
        private final byte[] mask;
        private final int prefixLength;

        CidrBlock(String cidr) {
            this.original = cidr;
            
            String[] parts = cidr.split("/");
            String ip = parts[0];
            prefixLength = parts.length > 1 ? Integer.parseInt(parts[1]) : 32;
            
            try {
                InetAddress addr = InetAddress.getByName(ip);
                network = addr.getAddress();
                mask = calculateMask(prefixLength, network.length);
            } catch (Exception e) {
                throw new IllegalArgumentException("无效的CIDR格式: " + cidr, e);
            }
        }

        private byte[] calculateMask(int prefix, int length) {
            byte[] m = new byte[length];
            int remaining = prefix;
            for (int i = 0; i < length; i++) {
                if (remaining >= 8) {
                    m[i] = (byte) 0xFF;
                    remaining -= 8;
                } else if (remaining > 0) {
                    m[i] = (byte) (0xFF << (8 - remaining));
                    remaining = 0;
                } else {
                    m[i] = 0;
                }
            }
            return m;
        }

        boolean contains(byte[] address) {
            if (address.length != network.length) {
                return false;
            }
            
            for (int i = 0; i < address.length; i++) {
                if ((address[i] & mask[i]) != (network[i] & mask[i])) {
                    return false;
                }
            }
            return true;
        }
    }

    public static class Factory implements PredicateFactory {

        @Override
        public Predicate createPredicate(PredicateDefinition definition) {
            return new RemoteAddrPredicate(definition);
        }

        @Override
        public String getSupportedPredicateName() {
            return TYPE;
        }

        @Override
        public void validateConfig(PredicateDefinition definition) {
            Object sources = definition.getConfigValue("sources");
            if (sources == null) {
                throw new IllegalArgumentException("RemoteAddrPredicate 必须配置 sources 参数");
            }
        }
    }
}