package com.muxin.gateway.core.config.provider;

import java.time.Instant;
import java.util.List;

public class ConfigChangedEvent {

    public enum ChangeType {
        ROUTE_ADDED,
        ROUTE_UPDATED,
        ROUTE_DELETED,
        ROUTE_REFRESH_ALL,
        SERVICE_ADDED,
        SERVICE_UPDATED,
        SERVICE_DELETED,
        SERVICE_REFRESH_ALL
    }

    private final ChangeType changeType;
    private final List<String> affectedIds;
    private final Instant timestamp;
    private final String source;

    public ConfigChangedEvent(ChangeType changeType, List<String> affectedIds, String source) {
        this.changeType = changeType;
        this.affectedIds = affectedIds;
        this.timestamp = Instant.now();
        this.source = source;
    }

    public ChangeType getChangeType() {
        return changeType;
    }

    public List<String> getAffectedIds() {
        return affectedIds;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public String getSource() {
        return source;
    }

    public boolean isRouteChange() {
        return changeType.name().startsWith("ROUTE");
    }

    public boolean isServiceChange() {
        return changeType.name().startsWith("SERVICE");
    }
}