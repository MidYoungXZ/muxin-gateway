package com.muxin.gateway.core.config.provider;

import com.muxin.gateway.core.config.GatewayConfigLoader;
import com.muxin.gateway.core.config.GatewayRouteConfig;
import com.muxin.gateway.core.route.RouteDefinition;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class YamlRouteConfigProvider implements RouteConfigProvider {

    private static final String SOURCE = "YAML";

    private final GatewayConfigLoader configLoader;
    private final String configFile;
    private final List<ConfigChangeListener> listeners = new CopyOnWriteArrayList<>();
    private final List<RouteDefinition> routes = new CopyOnWriteArrayList<>();
    private final AtomicBoolean watching = new AtomicBoolean(false);

    private WatchService watchService;
    private ExecutorService watchExecutor;
    private Path configPath;

    public YamlRouteConfigProvider() {
        this("gateway-routes.yml");
    }

    public YamlRouteConfigProvider(String configFile) {
        this.configFile = configFile;
        this.configLoader = new GatewayConfigLoader();
        loadConfig();
    }

    private void loadConfig() {
        try {
            GatewayRouteConfig config = configLoader.loadConfig(configFile);
            List<RouteDefinition> newRoutes = config.getRoutes() != null ? config.getRoutes() : List.of();
            routes.clear();
            routes.addAll(newRoutes);
            if (log.isDebugEnabled()) {
                log.debug("Loaded {} routes from YAML config", routes.size());
            }
        } catch (Exception e) {
            log.error("Failed to load route config from YAML: {}", configFile, e);
        }
    }

    @Override
    public List<RouteDefinition> getRoutes() {
        return Collections.unmodifiableList(routes);
    }

    @Override
    public Optional<RouteDefinition> getRoute(String routeId) {
        return routes.stream()
                .filter(r -> r.getId().equals(routeId))
                .findFirst();
    }

    @Override
    public void refresh() {
        if (log.isInfoEnabled()) {
            log.info("Refreshing route configuration from YAML: {}", configFile);
        }
        List<String> oldIds = routes.stream().map(RouteDefinition::getId).toList();
        loadConfig();
        List<String> newIds = routes.stream().map(RouteDefinition::getId).toList();

        ConfigChangedEvent event = new ConfigChangedEvent(
                ConfigChangedEvent.ChangeType.ROUTE_REFRESH_ALL,
                newIds,
                SOURCE
        );
        notifyListeners(event);
    }

    @Override
    public void addChangeListener(ConfigChangeListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeChangeListener(ConfigChangeListener listener) {
        listeners.remove(listener);
    }

    @Override
    public String getSource() {
        return SOURCE;
    }

    public void startWatching() {
        if (!watching.compareAndSet(false, true)) {
            return;
        }

        try {
            String configDir = System.getProperty("config.dir", ".");
            Path dirPath = Paths.get(configDir).toAbsolutePath();

            if (!Files.exists(dirPath)) {
                dirPath = Paths.get("src/main/resources").toAbsolutePath();
            }

            configPath = dirPath.resolve(configFile);
            Path watchDir = configPath.getParent();

            if (!Files.exists(watchDir)) {
                if (log.isWarnEnabled()) {
                    log.warn("Config directory does not exist, file watching disabled: {}", watchDir);
                }
                watching.set(false);
                return;
            }

            watchService = FileSystems.getDefault().newWatchService();
            watchDir.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);

            watchExecutor = Executors.newSingleThreadExecutor(r -> {
                Thread t = new Thread(r, "yaml-config-watcher");
                t.setDaemon(true);
                return t;
            });

            watchExecutor.submit(this::watchLoop);

            if (log.isInfoEnabled()) {
                log.info("Started watching config file: {}", configPath);
            }
        } catch (IOException e) {
            log.error("Failed to start file watching", e);
            watching.set(false);
        }
    }

    public void stopWatching() {
        if (!watching.compareAndSet(true, false)) {
            return;
        }

        if (watchExecutor != null) {
            watchExecutor.shutdown();
        }
        if (watchService != null) {
            try {
                watchService.close();
            } catch (IOException e) {
                log.error("Failed to close watch service", e);
            }
        }
        if (log.isInfoEnabled()) {
            log.info("Stopped watching config file");
        }
    }

    private void watchLoop() {
        while (watching.get()) {
            try {
                WatchKey key = watchService.take();
                for (WatchEvent<?> event : key.pollEvents()) {
                    if (event.kind() == StandardWatchEventKinds.OVERFLOW) {
                        continue;
                    }

                    Path changedFile = (Path) event.context();
                    if (configFile.equals(changedFile.toString())) {
                        if (log.isInfoEnabled()) {
                            log.info("Config file changed, refreshing: {}", changedFile);
                        }
                        Thread.sleep(100);
                        refresh();
                    }
                }
                key.reset();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (ClosedWatchServiceException e) {
                break;
            }
        }
    }

    private void notifyListeners(ConfigChangedEvent event) {
        for (ConfigChangeListener listener : listeners) {
            try {
                listener.onRouteConfigChanged(event);
            } catch (Exception e) {
                log.error("Error notifying listener: {}", listener, e);
            }
        }
    }
}