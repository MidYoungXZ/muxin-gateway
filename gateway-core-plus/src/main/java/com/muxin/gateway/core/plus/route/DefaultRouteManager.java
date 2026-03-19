package com.muxin.gateway.core.plus.route;

import com.muxin.gateway.core.plus.route.filter.Filter;
import com.muxin.gateway.core.plus.route.loadbalance.LoadBalanceStrategy;
import com.muxin.gateway.core.plus.route.predicate.Predicate;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Slf4j
public class DefaultRouteManager implements RouteManager {

    private final AtomicReference<Route> defaultRoute = new AtomicReference<>();
    private final Map<String, Route> routeStorage = new ConcurrentHashMap<>();
    private volatile List<Route> sortedRoutes = Collections.emptyList();
    private final Map<String, RouteService> routeServiceStorage = new ConcurrentHashMap<>();
    private final Map<String, ServiceDefinition> serviceStorage = new ConcurrentHashMap<>();
    private final Map<String, Predicate> predicateStorage = new ConcurrentHashMap<>();
    private final Map<String, Filter> filterStorage = new ConcurrentHashMap<>();
    private final ReentrantReadWriteLock cacheLock = new ReentrantReadWriteLock();

    @Override
    public Route matchRoute(RequestContext context) {
        if (context == null || context.exchange() == null) {
            log.warn("[DefaultRouteManager] 请求上下文为空，返回默认路由");
            return defaultRoute.get();
        }

        List<Route> routes = sortedRoutes;
        if (routes == null || routes.isEmpty()) {
            log.warn("[DefaultRouteManager] 路由列表为空，返回默认路由");
            return defaultRoute.get();
        }

        for (Route route : routes) {
            if (route.matches(context)) {
                log.debug("[DefaultRouteManager] 匹配路由: {} -> {}", context.requestId(), route.getId());
                return route;
            }
        }

        log.debug("[DefaultRouteManager] 未匹配到路由，使用默认路由: {}", context.requestId());
        return defaultRoute.get();
    }

    public void addRoute(Route route) {
        if (route == null) {
            throw new IllegalArgumentException("路由不能为空");
        }

        try {
            cacheLock.writeLock().lock();
            route.validate();
            routeStorage.put(route.getId(), route);
            routeServiceStorage.put(route.getService().serviceDefinition().getId(), route.getService());

            for (Predicate predicate : route.getPredicates()) {
                predicateStorage.put(predicate.getName(), predicate);
            }

            for (Filter filter : route.getFilters()) {
                filterStorage.put(filter.getName(), filter);
            }

            refreshCache();
            log.info("[DefaultRouteManager] 路由添加成功: {} -> {}", route.getId(), route.getName());

        } catch (Exception e) {
            log.error("[DefaultRouteManager] 路由添加失败: {}", route.getId(), e);
            throw e;
        } finally {
            cacheLock.writeLock().unlock();
        }
    }

    public void addRoutes(List<Route> routes) {
        if (routes == null || routes.isEmpty()) {
            return;
        }

        for (Route route : routes) {
            addRoute(route);
        }

        log.info("[DefaultRouteManager] 批量添加路由: {} 个", routes.size());
    }

    public void updateRoute(Route route) {
        Objects.requireNonNull(route, "路由不能为空");

        try {
            cacheLock.writeLock().lock();

            if (!routeStorage.containsKey(route.getId())) {
                log.warn("[DefaultRouteManager] 路由不存在，创建新路由: {}", route.getId());
                addRoute(route);
                return;
            }

            route.validate();
            routeStorage.put(route.getId(), route);
            refreshCache();

            log.info("[DefaultRouteManager] 路由更新成功: {} -> {}", route.getId(), route.getName());

        } catch (Exception e) {
            log.error("[DefaultRouteManager] 路由更新失败: {}", route.getId(), e);
            throw e;
        } finally {
            cacheLock.writeLock().unlock();
        }
    }

    public Route deleteRoute(String routeId) {
        if (routeId == null) {
            return null;
        }

        try {
            cacheLock.writeLock().lock();

            Route removedRoute = routeStorage.remove(routeId);
            if (removedRoute != null) {
                refreshCache();
                log.info("[DefaultRouteManager] 路由删除成功: {}", routeId);
            } else {
                log.debug("[DefaultRouteManager] 未找到要删除的路由: {}", routeId);
            }

            return removedRoute;

        } catch (Exception e) {
            log.error("[DefaultRouteManager] 路由删除失败: {}", routeId, e);
            throw e;
        } finally {
            cacheLock.writeLock().unlock();
        }
    }

    @Override
    public Route insert(Route entity) {
        addRoute(entity);
        return entity;
    }

    @Override
    public void deleteById(String id) {
        deleteRoute(id);
    }

    @Override
    public Route selectById(String id) {
        return routeStorage.get(id);
    }

    @Override
    public Collection<Route> selectAll() {
        return listRoutes();
    }

    public List<Route> listRoutes() {
        return new ArrayList<>(routeStorage.values());
    }

    public Map<String, RouteService> listRouteServices() {
        return new HashMap<>(routeServiceStorage);
    }

    public Map<String, Predicate> listPredicates() {
        return new HashMap<>(predicateStorage);
    }

    public Map<String, Filter> listFilters() {
        return new HashMap<>(filterStorage);
    }

    public Map<String, ServiceDefinition> listServiceDefinitions() {
        return new HashMap<>(serviceStorage);
    }

    private void refreshCache() {
        List<Route> routeList = new ArrayList<>(routeStorage.values());
        routeList.sort(Comparator.comparingInt(Route::getOrder));

        sortedRoutes = Collections.unmodifiableList(routeList);
        log.debug("[DefaultRouteManager] 路由缓存已刷新: {} 个路由", routeList.size());
    }

    public void addRouteService(RouteService routeService) {
        if (routeService == null) {
            throw new IllegalArgumentException("RouteService不能为空");
        }
        ServiceDefinition serviceDef = routeService.serviceDefinition();
        routeServiceStorage.put(serviceDef.getId(), routeService);
        serviceStorage.put(serviceDef.getId(), serviceDef);

        log.info("[DefaultRouteManager] 路由服务添加成功: {} - {}", serviceDef.getId(), serviceDef.getName());
    }

    public RouteService getRouteService(String serviceId) {
        return routeServiceStorage.get(serviceId);
    }

    public void registerPredicate(Predicate predicate) {
        if (predicate == null) {
            throw new IllegalArgumentException("断言不能为空");
        }
        predicateStorage.put(predicate.getName(), predicate);
    }

    public Predicate getPredicate(String name) {
        return predicateStorage.get(name);
    }

    public void registerFilter(Filter filter) {
        if (filter == null) {
            throw new IllegalArgumentException("过滤器不能为空");
        }
        filterStorage.put(filter.getName(), filter);
    }

    public Filter getFilter(String name) {
        return filterStorage.get(name);
    }

    public void registerServiceDefinition(ServiceDefinition serviceDefinition) {
        if (serviceDefinition == null) {
            throw new IllegalArgumentException("服务定义不能为空");
        }
        serviceStorage.put(serviceDefinition.getId(), serviceDefinition);

        log.info("[DefaultRouteManager] 服务定义注册成功: {} - {}", serviceDefinition.getId(), serviceDefinition.getName());
    }

    public ServiceDefinition getServiceDefinition(String serviceId) {
        return serviceStorage.get(serviceId);
    }

    public int getRouteCount() {
        return routeStorage.size();
    }

    public boolean existsRoute(String routeId) {
        return routeStorage.containsKey(routeId);
    }

    public boolean existsService(String serviceId) {
        return serviceStorage.containsKey(serviceId);
    }

    public void setDefaultRoute(Route route) {
        defaultRoute.set(Objects.requireNonNull(route, "默认路由不能为空"));
        log.info("[DefaultRouteManager] 设置默认路由: {} -> {}", route.getId(), route.getName());
    }

    public Route getDefaultRoute() {
        return defaultRoute.get();
    }

    public void clearRoutes() {
        routeStorage.clear();
        sortedRoutes = Collections.emptyList();
        log.info("[DefaultRouteManager] 清除所有路由");
    }

    @Override
    public void init() {
        refreshCache();
        log.info("[DefaultRouteManager] 路由管理器初始化完成");
    }

    @Override
    public void start() {
        refreshCache();
    }

    @Override
    public void shutdown() {
        clearRoutes();
        routeServiceStorage.clear();
        predicateStorage.clear();
        filterStorage.clear();
        serviceStorage.clear();

        log.info("[DefaultRouteManager] 路由管理器已关闭");
    }

    public void validate() {
        if (sortedRoutes == null) {
            throw new IllegalStateException("sortedRoutes未初始化");
        }
    }

    @Override
    public String toString() {
        return "DefaultRouteManager{" +
                "routeCount=" + routeStorage.size() +
                ", predicateCount=" + predicateStorage.size() +
                ", filterCount=" + filterStorage.size() +
                ", serviceCount=" + routeServiceStorage.size() +
                ", hasDefault=" + (defaultRoute.get() != null) +
                '}';
    }
}
