package com.muxin.gateway.refactory;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 默认路由实现
 * 
 * @author muxin
 */
public class DefaultRoute implements UniversalRoute {
    
    private final String id;
    private final String name;
    private final String description;
    private final int order;
    private final List<Protocol> supportedProtocols;
    private final List<UniversalPredicate> predicates;
    private final List<UniversalFilter> filters;
    private final RouteTarget target;
    private final Map<String, Object> metadata;
    
    private volatile boolean enabled;
    
    private DefaultRoute(Builder builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.description = builder.description;
        this.order = builder.order;
        this.supportedProtocols = Collections.unmodifiableList(builder.supportedProtocols);
        this.predicates = Collections.unmodifiableList(builder.predicates);
        this.filters = Collections.unmodifiableList(builder.filters);
        this.target = builder.target;
        this.metadata = Collections.unmodifiableMap(new HashMap<>(builder.metadata));
        this.enabled = builder.enabled;
    }
    
    @Override
    public String getId() {
        return id;
    }
    
    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public String getDescription() {
        return description;
    }
    
    @Override
    public int getOrder() {
        return order;
    }
    
    @Override
    public boolean isEnabled() {
        return enabled;
    }
    
    /**
     * 设置路由启用状态
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    @Override
    public List<Protocol> getSupportedProtocols() {
        return supportedProtocols;
    }
    
    @Override
    public List<UniversalPredicate> getPredicates() {
        return predicates;
    }
    
    @Override
    public List<UniversalFilter> getFilters() {
        return filters;
    }
    
    @Override
    public RouteTarget getTarget() {
        return target;
    }
    
    @Override
    public Map<String, Object> getMetadata() {
        return metadata;
    }
    
    @Override
    public boolean matches(UniversalRequestContext context) {
        if (!enabled) {
            return false;
        }
        
        // 检查协议支持
        Protocol inboundProtocol = context.getInboundProtocol();
        if (inboundProtocol != null && !supportedProtocols.contains(inboundProtocol)) {
            return false;
        }
        
        // 所有断言必须都匹配（AND关系）
        for (UniversalPredicate predicate : predicates) {
            try {
                if (!predicate.test(context)) {
                    return false;
                }
            } catch (Exception e) {
                System.err.println("路由断言执行失败: " + predicate.getName() + ", 错误: " + e.getMessage());
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * 获取匹配得分（用于优先级排序）
     */
    public int getMatchScore(UniversalRequestContext context) {
        if (!matches(context)) {
            return -1;
        }
        
        int score = 0;
        
        // 断言数量越多，匹配越精确，得分越高
        score += predicates.size() * 10;
        
        // order值越小，优先级越高，得分越高
        score += (1000 - order);
        
        return score;
    }
    
    @Override
    public String toString() {
        return "DefaultRoute{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", order=" + order +
                ", enabled=" + enabled +
                ", protocols=" + supportedProtocols.size() +
                ", predicates=" + predicates.size() +
                ", filters=" + filters.size() +
                '}';
    }
    
    /**
     * 路由构建器
     */
    public static class Builder {
        private String id;
        private String name;
        private String description = "";
        private int order = 0;
        private boolean enabled = true;
        private final List<Protocol> supportedProtocols = new CopyOnWriteArrayList<>();
        private final List<UniversalPredicate> predicates = new CopyOnWriteArrayList<>();
        private final List<UniversalFilter> filters = new CopyOnWriteArrayList<>();
        private RouteTarget target;
        private final Map<String, Object> metadata = new HashMap<>();
        
        public Builder(String id, String name) {
            this.id = id;
            this.name = name;
        }
        
        public Builder description(String description) {
            this.description = description;
            return this;
        }
        
        public Builder order(int order) {
            this.order = order;
            return this;
        }
        
        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }
        
        public Builder protocol(Protocol protocol) {
            this.supportedProtocols.add(protocol);
            return this;
        }
        
        public Builder protocols(Protocol... protocols) {
            this.supportedProtocols.addAll(Arrays.asList(protocols));
            return this;
        }
        
        public Builder predicate(UniversalPredicate predicate) {
            this.predicates.add(predicate);
            return this;
        }
        
        public Builder predicates(UniversalPredicate... predicates) {
            this.predicates.addAll(Arrays.asList(predicates));
            return this;
        }
        
        public Builder filter(UniversalFilter filter) {
            this.filters.add(filter);
            return this;
        }
        
        public Builder filters(UniversalFilter... filters) {
            this.filters.addAll(Arrays.asList(filters));
            return this;
        }
        
        public Builder target(RouteTarget target) {
            this.target = target;
            return this;
        }
        
        public Builder metadata(String key, Object value) {
            this.metadata.put(key, value);
            return this;
        }
        
        public Builder metadata(Map<String, Object> metadata) {
            this.metadata.putAll(metadata);
            return this;
        }
        
        public DefaultRoute build() {
            if (id == null || name == null) {
                throw new IllegalArgumentException("路由ID和名称不能为空");
            }
            if (target == null) {
                throw new IllegalArgumentException("路由目标不能为空");
            }
            if (supportedProtocols.isEmpty()) {
                throw new IllegalArgumentException("至少需要支持一种协议");
            }
            
            return new DefaultRoute(this);
        }
    }
} 