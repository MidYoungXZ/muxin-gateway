package com.muxin.gateway.admin.service;

public interface ConfigRefreshService {
    
    void refreshAll();
    
    void refreshRoutes();
    
    void refreshServices();
    
    String getConfigSource();
}