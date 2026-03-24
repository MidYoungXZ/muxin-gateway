package com.muxin.gateway.core.config.provider;

public interface ConfigChangeListener {

    void onRouteConfigChanged(ConfigChangedEvent event);

    void onServiceConfigChanged(ConfigChangedEvent event);
}