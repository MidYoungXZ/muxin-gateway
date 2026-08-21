package com.muxin.gateway.config;

import com.muxin.gateway.admin.entity.GwServiceNode;
import com.muxin.gateway.admin.mapper.ServiceNodeMapper;
import com.muxin.gateway.admin.service.ConfigRefreshService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class NodeHealthCheckTask {

    private final ServiceNodeMapper serviceNodeMapper;
    private final ConfigRefreshService configRefreshService;
    private final HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();

    @Scheduled(fixedDelay = 1000)
    public void checkNodes() {
        LocalDateTime now = LocalDateTime.now();
        List<GwServiceNode> dueNodes = new ArrayList<>();
        for (GwServiceNode node : serviceNodeMapper.selectAll()) {
            if (!Boolean.TRUE.equals(node.getHealthCheckEnabled()) || !Integer.valueOf(1).equals(node.getStatus())
                    || !isDue(node, now)) {
                continue;
            }
            dueNodes.add(node);
            if (dueNodes.size() == 20) break;
        }
        List<CompletableFuture<Boolean>> checks = dueNodes.stream()
                .map(this::check)
                .toList();
        CompletableFuture.allOf(checks.toArray(CompletableFuture[]::new)).join();
        boolean availabilityChanged = false;
        for (int i = 0; i < dueNodes.size(); i++) {
            GwServiceNode node = dueNodes.get(i);
            Integer result = checks.get(i).join() ? 1 : 0;
            availabilityChanged |= !result.equals(node.getLastCheckResult());
            node.setLastCheckResult(result);
            node.setLastCheckTime(now);
            serviceNodeMapper.update(node);
        }
        if (availabilityChanged) {
            configRefreshService.refreshServices();
        }
    }

    private boolean isDue(GwServiceNode node, LocalDateTime now) {
        int interval = node.getHealthCheckInterval() == null ? 30 : Math.max(1, node.getHealthCheckInterval());
        return node.getLastCheckTime() == null || !node.getLastCheckTime().plusSeconds(interval).isAfter(now);
    }

    private CompletableFuture<Boolean> check(GwServiceNode node) {
        try {
            String path = node.getHealthCheckPath();
            URI uri = new URI("http", null, node.getAddress(), node.getPort(),
                    path == null || path.isBlank() ? "/health" : path, null, null);
            int timeout = node.getHealthCheckTimeout() == null ? 5 : Math.max(1, node.getHealthCheckTimeout());
            List<Integer> expected = node.getHealthCheckExpectedStatus();
            return httpClient.sendAsync(HttpRequest.newBuilder(uri)
                            .timeout(Duration.ofSeconds(timeout)).GET().build(), HttpResponse.BodyHandlers.discarding())
                    .handle((response, error) -> {
                        if (error != null) {
                            log.debug("[NodeHealthCheck] 节点检查失败: {} - {}", node.getNodeId(), error.getMessage());
                            return false;
                        }
                        return expected == null || expected.isEmpty()
                                ? response.statusCode() == 200 : expected.contains(response.statusCode());
                    });
        } catch (Exception e) {
            log.debug("[NodeHealthCheck] 节点检查失败: {} - {}", node.getNodeId(), e.getMessage());
            return CompletableFuture.completedFuture(false);
        }
    }
}
