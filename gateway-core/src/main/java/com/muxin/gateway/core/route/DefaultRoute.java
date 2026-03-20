package com.muxin.gateway.core.route;

import com.muxin.gateway.core.route.filter.Filter;
import com.muxin.gateway.core.route.filter.FilterType;
import com.muxin.gateway.core.route.loadbalance.LoadBalanceStrategy;
import com.muxin.gateway.core.route.predicate.PathPredicate;
import com.muxin.gateway.core.route.predicate.Predicate;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Getter
public class DefaultRoute implements Route {

    private final String id;
    private final String name;
    private final int order;
    private final boolean enabled;
    private final List<Predicate> predicates;
    private final List<Filter> filters;
    private final RouteService service;
    private final LoadBalanceStrategy loadBalanceStrategy;
    private final TimeoutConfig timeoutConfig;

    private final List<Filter> preFilters;
    private final List<Filter> postFilters;
    private final PathPredicate pathPredicate;
    private final int stripPrefixCount;

    private DefaultRoute(String id, String name, int order, boolean enabled,
                         List<Predicate> predicates, List<Filter> filters,
                         RouteService service, LoadBalanceStrategy loadBalanceStrategy,
                         TimeoutConfig timeoutConfig) {
        this.id = Objects.requireNonNull(id, "路由ID不能为空");
        this.name = name != null ? name : id;
        this.order = order;
        this.enabled = enabled;
        this.predicates = predicates != null ? predicates : Collections.emptyList();
        this.filters = filters != null ? filters : Collections.emptyList();
        this.service = Objects.requireNonNull(service, "目标服务不能为空");
        this.loadBalanceStrategy = Objects.requireNonNull(loadBalanceStrategy, "负载均衡策略不能为空");
        this.timeoutConfig = timeoutConfig;

        this.preFilters = initFilters(FilterType.PRE);
        this.postFilters = initFilters(FilterType.POST);
        this.pathPredicate = findPathPredicate();
        this.stripPrefixCount = pathPredicate != null ? pathPredicate.getStripPrefixCount() : 0;
    }

    public static Builder builder() {
        return new Builder();
    }

    private List<Filter> initFilters(FilterType type) {
        return filters.stream()
                .filter(f -> f.getType() == type && f.isEnabled())
                .sorted(Comparator.comparingInt(Filter::getOrder))
                .toList();
    }

    private PathPredicate findPathPredicate() {
        return predicates.stream()
                .filter(p -> p instanceof PathPredicate)
                .map(p -> (PathPredicate) p)
                .findFirst()
                .orElse(null);
    }

    @Override
    public void validate() {
        Objects.requireNonNull(id, "路由ID不能为空");
        Objects.requireNonNull(service, "目标服务不能为空");
        Objects.requireNonNull(loadBalanceStrategy, "负载均衡策略不能为空");
        if (predicates.isEmpty()) {
            throw new IllegalArgumentException("断言列表不能为空");
        }
    }

    @Override
    public boolean matches(RequestContext ctx) {
        if (!enabled || ctx == null || ctx.exchange() == null) return false;
        for (Predicate p : predicates) {
            if (!p.test(ctx.exchange())) return false;
        }
        return true;
    }

    @Override
    public String getDescription() {
        return name;
    }

    @Override
    public Map<String, Object> getMetadata() {
        return Collections.emptyMap();
    }

    @Override
    public long getTimeout(TimeoutType type) {
        return timeoutConfig != null ? timeoutConfig.get(type) : TimeoutConfig.getDefault(type);
    }

    public static class Builder {
        private String id;
        private String name;
        private String description;
        private int order;
        private boolean enabled = true;
        private List<Predicate> predicates;
        private List<Filter> filters;
        private RouteService service;
        private LoadBalanceStrategy loadBalanceStrategy;
        private TimeoutConfig timeoutConfig;

        public Builder id(String id) { this.id = id; return this; }
        public Builder name(String name) { this.name = name; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder order(int order) { this.order = order; return this; }
        public Builder enabled(boolean enabled) { this.enabled = enabled; return this; }
        public Builder predicates(List<Predicate> predicates) { this.predicates = predicates; return this; }
        public Builder filters(List<Filter> filters) { this.filters = filters; return this; }
        public Builder service(RouteService service) { this.service = service; return this; }
        public Builder loadBalanceStrategy(LoadBalanceStrategy strategy) { this.loadBalanceStrategy = strategy; return this; }
        public Builder timeoutConfig(TimeoutConfig config) { this.timeoutConfig = config; return this; }
        public Builder metadata(Map<String, Object> metadata) { return this; }

        public DefaultRoute build() {
            return new DefaultRoute(id, name != null ? name : description, order, enabled, predicates, filters, service, loadBalanceStrategy, timeoutConfig);
        }
    }
}