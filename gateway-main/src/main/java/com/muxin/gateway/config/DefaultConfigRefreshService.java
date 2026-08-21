package com.muxin.gateway.config;

import com.muxin.gateway.admin.service.ConfigRefreshService;
import com.muxin.gateway.core.config.provider.RouteConfigProvider;
import com.muxin.gateway.core.config.provider.ServiceConfigProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Slf4j
@Service
@RequiredArgsConstructor
public class DefaultConfigRefreshService implements ConfigRefreshService {

    private final RouteConfigProvider routeConfigProvider;
    private final ServiceConfigProvider serviceConfigProvider;

    @Override
    public void refreshAll() {
        log.info("[ConfigRefreshService] Refreshing all configurations...");
        
        afterCommit(() -> {
            serviceConfigProvider.refresh();
            routeConfigProvider.refresh();
        });
        
        log.info("[ConfigRefreshService] All configurations refreshed successfully");
    }

    @Override
    public void refreshRoutes() {
        log.info("[ConfigRefreshService] Refreshing route configuration...");
        afterCommit(routeConfigProvider::refresh);
        log.info("[ConfigRefreshService] Route configuration refreshed successfully");
    }

    @Override
    public void refreshServices() {
        log.info("[ConfigRefreshService] Refreshing service configuration...");
        afterCommit(serviceConfigProvider::refresh);
        log.info("[ConfigRefreshService] Service configuration refreshed successfully");
    }

    @Override
    public String getConfigSource() {
        return String.format("RouteProvider: %s, ServiceProvider: %s",
                routeConfigProvider.getSource(),
                serviceConfigProvider.getSource());
    }

    private void afterCommit(Runnable refresh) {
        if (!TransactionSynchronizationManager.isActualTransactionActive()) {
            refresh.run();
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                refresh.run();
            }
        });
    }
}
