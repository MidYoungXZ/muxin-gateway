package com.muxin.gateway.config.provider;

import com.muxin.gateway.admin.entity.GwServiceNode;
import com.muxin.gateway.admin.mapper.ServiceNodeMapper;
import com.muxin.gateway.core.config.provider.ConfigChangedEvent;
import com.muxin.gateway.core.config.provider.ConfigChangeListener;
import com.muxin.gateway.core.config.provider.ServiceConfigProvider;
import com.muxin.gateway.core.route.AddressDefinition;
import com.muxin.gateway.core.route.ServiceDefinition;
import com.muxin.gateway.core.route.ServiceType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class DatabaseServiceConfigProvider implements ServiceConfigProvider {

    private static final String SOURCE = "DATABASE";

    private final ServiceNodeMapper serviceNodeMapper;
    private final List<ConfigChangeListener> listeners = new CopyOnWriteArrayList<>();
    private volatile List<ServiceDefinition> cachedServices = new ArrayList<>();
    private volatile Map<String, ServiceDefinition> serviceMap = new HashMap<>();
    private volatile boolean refreshing = false;

    @Override
    public List<ServiceDefinition> getServices() {
        if (cachedServices.isEmpty() && !refreshing) {
            refresh();
        }
        return Collections.unmodifiableList(cachedServices);
    }

    @Override
    public Optional<ServiceDefinition> getService(String serviceId) {
        if (serviceMap.isEmpty() && !refreshing) {
            refresh();
        }
        return Optional.ofNullable(serviceMap.get(serviceId));
    }

    @Override
    public void refresh() {
        if (refreshing) {
            return;
        }
        refreshing = true;
        try {
            if (log.isInfoEnabled()) {
                log.info("Refreshing service configuration from database");
            }

            List<GwServiceNode> nodes = serviceNodeMapper.selectAll()
                    .stream()
                    .filter(n -> n.getStatus() != null && n.getStatus() == 1
                            && !Integer.valueOf(0).equals(n.getLastCheckResult()))
                    .collect(Collectors.toList());

            Map<String, List<GwServiceNode>> groupedNodes = nodes.stream()
                    .collect(Collectors.groupingBy(GwServiceNode::getServiceName));

            List<ServiceDefinition> newServices = new ArrayList<>();
            Map<String, ServiceDefinition> newServiceMap = new HashMap<>();

            for (Map.Entry<String, List<GwServiceNode>> entry : groupedNodes.entrySet()) {
                String serviceName = entry.getKey();
                List<GwServiceNode> serviceNodes = entry.getValue();

                ServiceDefinition service = convertToServiceDefinition(serviceName, serviceNodes);
                newServices.add(service);
                newServiceMap.put(serviceName, service);
            }

            cachedServices = newServices;
            serviceMap = newServiceMap;

            ConfigChangedEvent event = new ConfigChangedEvent(
                    ConfigChangedEvent.ChangeType.SERVICE_REFRESH_ALL,
                    newServices.stream().map(ServiceDefinition::getId).toList(),
                    SOURCE
            );
            notifyListeners(event);

            if (log.isInfoEnabled()) {
                log.info("Loaded {} services ({} nodes) from database", cachedServices.size(), nodes.size());
            }
        } finally {
            refreshing = false;
        }
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

    private ServiceDefinition convertToServiceDefinition(String serviceName, List<GwServiceNode> nodes) {
        List<AddressDefinition> addresses = nodes.stream()
                .map(this::convertToAddressDefinition)
                .collect(Collectors.toList());

        return ServiceDefinition.builder()
                .id(serviceName)
                .name(serviceName)
                .type(ServiceType.STATIC)
                .supportedProtocols(List.of("HTTP"))
                .addresses(addresses)
                .build();
    }

    private AddressDefinition convertToAddressDefinition(GwServiceNode node) {
        String uri = String.format("http://%s:%d", node.getAddress(), node.getPort());

        return AddressDefinition.builder()
                .uri(uri)
                .weight(node.getWeight() != null ? node.getWeight() : 100)
                .build();
    }

    private void notifyListeners(ConfigChangedEvent event) {
        for (ConfigChangeListener listener : listeners) {
            try {
                listener.onServiceConfigChanged(event);
            } catch (Exception e) {
                log.error("Error notifying listener: {}", listener, e);
            }
        }
    }
}
