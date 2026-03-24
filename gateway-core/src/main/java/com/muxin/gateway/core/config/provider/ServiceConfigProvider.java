package com.muxin.gateway.core.config.provider;

import com.muxin.gateway.core.route.ServiceDefinition;

import java.util.List;
import java.util.Optional;

public interface ServiceConfigProvider {

    List<ServiceDefinition> getServices();

    Optional<ServiceDefinition> getService(String serviceId);

    void refresh();

    void addChangeListener(ConfigChangeListener listener);

    void removeChangeListener(ConfigChangeListener listener);

    String getSource();
}