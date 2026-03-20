package com.muxin.gateway.core.plus.route;

import com.muxin.gateway.core.plus.route.filter.Filter;
import com.muxin.gateway.core.plus.route.predicate.Predicate;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
public class DefaultRouteManager implements RouteManager {

    private final AtomicReference<Route> defaultRoute = new AtomicReference<>();
    private final Map<String, Route> routes = new ConcurrentHashMap<>();
    private volatile List<Route> sortedRoutes = Collections.emptyList();
    private final Map<String, RouteService> services = new ConcurrentHashMap<>();

    @Override
    public Route matchRoute(RequestContext ctx) {
        if (ctx == null || ctx.exchange() == null) {
            return defaultRoute.get();
        }

        for (Route route : sortedRoutes) {
            if (route.matches(ctx)) {
                ctx.setMatchedRoute(route);
                return route;
            }
        }
        return defaultRoute.get();
    }

    public void addRoute(Route route) {
        Objects.requireNonNull(route, "路由不能为空");
        route.validate();
        routes.put(route.getId(), route);
        if (route.getService() != null) {
            services.put(route.getId(), route.getService());
        }
        refreshCache();
        log.info("[DefaultRouteManager] 添加路由: {}", route.getId());
    }

    public void addRoutes(List<Route> routeList) {
        if (routeList == null) return;
        routeList.forEach(this::addRoute);
    }

    public void updateRoute(Route route) {
        Objects.requireNonNull(route, "路由不能为空");
        route.validate();
        routes.put(route.getId(), route);
        refreshCache();
        log.info("[DefaultRouteManager] 更新路由: {}", route.getId());
    }

    public Route deleteRoute(String routeId) {
        Route removed = routes.remove(routeId);
        if (removed != null) {
            services.remove(routeId);
            refreshCache();
            log.info("[DefaultRouteManager] 删除路由: {}", routeId);
        }
        return removed;
    }

    private void refreshCache() {
        List<Route> list = new ArrayList<>(routes.values());
        list.sort(Comparator.comparingInt(Route::getOrder));
        sortedRoutes = Collections.unmodifiableList(list);
    }

    @Override
    public Route insert(Route route) {
        addRoute(route);
        return route;
    }

    @Override
    public void deleteById(String id) {
        deleteRoute(id);
    }

    @Override
    public Route selectById(String id) {
        return routes.get(id);
    }

    @Override
    public Collection<Route> selectAll() {
        return new ArrayList<>(routes.values());
    }

    @Override
    public void setDefaultRoute(Route route) {
        defaultRoute.set(Objects.requireNonNull(route, "默认路由不能为空"));
    }

    @Override
    public Route getDefaultRoute() {
        return defaultRoute.get();
    }

    public int getRouteCount() {
        return routes.size();
    }

    @Override
    public void init() {
        refreshCache();
    }

    @Override
    public void start() {
        refreshCache();
    }

    @Override
    public void shutdown() {
        routes.clear();
        services.clear();
        sortedRoutes = Collections.emptyList();
    }
}